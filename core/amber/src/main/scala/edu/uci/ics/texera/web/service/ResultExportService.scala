package edu.uci.ics.texera.web.service

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets
import java.util
import java.util.concurrent.{Executors, ThreadPoolExecutor}
import com.github.tototoshi.csv.CSVWriter
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.Lists
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{File, FileList, Permission}
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.{Spreadsheet, SpreadsheetProperties, ValueRange}
import edu.uci.ics.amber.engine.common.model.tuple.Tuple
import edu.uci.ics.amber.engine.common.virtualidentity.OperatorIdentity
import edu.uci.ics.amber.engine.common.Utils.retry
import edu.uci.ics.texera.web.model.jooq.generated.tables.pojos.User
import edu.uci.ics.texera.web.model.websocket.request.ResultExportRequest
import edu.uci.ics.texera.web.model.websocket.response.ResultExportResponse
import edu.uci.ics.texera.web.resource.GoogleResource
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.DatasetResource.createNewDatasetVersionByAddingFiles
import edu.uci.ics.texera.web.resource.dashboard.user.dataset.utils.PathUtils
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowVersionResource
import edu.uci.ics.texera.workflow.common.storage.OpResultStorage
import edu.uci.ics.texera.workflow.operators.sink.storage.SinkStorageReader
import org.jooq.types.UInteger

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.annotation.tailrec
import scala.collection.mutable
import scala.jdk.CollectionConverters.SeqHasAsJava

object ResultExportService {
  final val UPLOAD_BATCH_ROW_COUNT = 10000
  final val RETRY_ATTEMPTS = 7
  final val BASE_BACK_OOF_TIME_IN_MS = 1000
  final val WORKFLOW_RESULT_FOLDER_NAME = "workflow_results"
  final val pool: ThreadPoolExecutor =
    Executors.newFixedThreadPool(3).asInstanceOf[ThreadPoolExecutor]
}

class ResultExportService(opResultStorage: OpResultStorage, wId: UInteger) {
  import ResultExportService._

  private val cache = new mutable.HashMap[String, String]

  def exportResult(
      user: User,
      request: ResultExportRequest
  ): ResultExportResponse = {
    // retrieve the file link saved in the session if exists
    if (cache.contains(request.exportType)) {
      return ResultExportResponse(
        "success",
        s"Link retrieved from cache ${cache(request.exportType)}"
      )
    }

    // By now the workflow should finish running
    val operatorWithResult: SinkStorageReader =
      opResultStorage.get(OperatorIdentity(request.operatorId))
    if (operatorWithResult == null) {
      return ResultExportResponse("error", "The workflow contains no results")
    }

    // convert the ITuple into tuple
    val results: Iterable[Tuple] = operatorWithResult.getAll
    val attributeNames = results.head.getSchema.getAttributeNames

    // handle the request according to export type
    request.exportType match {
      case "google_sheet" =>
        handleGoogleSheetRequest(cache, request, results, attributeNames)
      case "csv" =>
        handleCSVRequest(user, request, results, attributeNames)
      case "data" =>
        handleDataRequest(user, request, results)
      case _ =>
        ResultExportResponse("error", s"Unknown export type: ${request.exportType}")
    }
  }

  private def handleCSVRequest(
      user: User,
      request: ResultExportRequest,
      results: Iterable[Tuple],
      headers: List[String]
  ): ResultExportResponse = {
    val stream = new ByteArrayOutputStream()
    val writer = CSVWriter.open(stream)
    writer.writeRow(headers)
    results.foreach { tuple =>
      writer.writeRow(tuple.getFields.toIndexedSeq)
    }
    writer.close()
    val latestVersion =
      WorkflowVersionResource.getLatestVersion(UInteger.valueOf(request.workflowId))
    val timestamp = LocalDateTime
      .now()
      .truncatedTo(ChronoUnit.SECONDS)
      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    val fileName = s"${request.workflowName}-v$latestVersion-${request.operatorName}-$timestamp.csv"

    // add files to datasets
    request.datasetIds.foreach(did => {
      val datasetPath = PathUtils.getDatasetPath(UInteger.valueOf(did))
      val filePath = datasetPath.resolve(fileName)
      createNewDatasetVersionByAddingFiles(
        UInteger.valueOf(did),
        user,
        Map(filePath -> new ByteArrayInputStream(stream.toByteArray))
      )
    })

    ResultExportResponse(
      "success",
      s"File saved to User Dashboard as $fileName to Datasets ${request.datasetIds.mkString(",")}"
    )
  }

  private def handleGoogleSheetRequest(
      exportCache: mutable.HashMap[String, String],
      request: ResultExportRequest,
      results: Iterable[Tuple],
      header: List[String]
  ): ResultExportResponse = {
    // create google sheet
    val sheetService: Sheets = GoogleResource.getSheetService
    val sheetId: String =
      createGoogleSheet(sheetService, request.workflowName)
    if (sheetId == null) {
      return ResultExportResponse("error", "Fail to create google sheet")
    }

    val driveService: Drive = GoogleResource.getDriveService
    moveToResultFolder(driveService, sheetId)

    // allow user to access this sheet in the service account
    val sharePermission: Permission = new Permission()
      .setType("anyone")
      .setRole("reader")
    driveService
      .permissions()
      .create(sheetId, sharePermission)
      .execute()

    // upload the content asynchronously to avoid long waiting on the user side.
    pool
      .submit(() =>
        {
          uploadHeader(sheetService, sheetId, header)
          uploadResult(sheetService, sheetId, results)
        }.asInstanceOf[Runnable]
      )

    // generate success response
    val link = s"https://docs.google.com/spreadsheets/d/$sheetId/edit"
    val message: String =
      s"Google sheet created. The results may be still uploading. You can access the sheet $link"
    // save the file link in the session cache
    exportCache(request.exportType) = link
    ResultExportResponse("success", message)
  }

  /**
    * create the google sheet and return the sheet Id
    */
  private def createGoogleSheet(sheetService: Sheets, workflowName: String): String = {
    val createSheetRequest = new Spreadsheet()
      .setProperties(new SpreadsheetProperties().setTitle(workflowName))
    val targetSheet: Spreadsheet = sheetService.spreadsheets
      .create(createSheetRequest)
      .setFields("spreadsheetId")
      .execute
    targetSheet.getSpreadsheetId
  }

  private def handleDataRequest(
      user: User,
      request: ResultExportRequest,
      results: Iterable[Tuple]
  ): ResultExportResponse = {
    val rowIndex = request.rowIndex
    val columnIndex = request.columnIndex
    val filename = request.filename

    // Validate that the requested row and column exist
    if (rowIndex >= results.size || columnIndex >= results.head.getFields.size) {
      return ResultExportResponse("error", s"Invalid row or column index")
    }

    val selectedRow = results.toSeq(rowIndex)
    val field: Any = selectedRow.getField(columnIndex)

    // Convert the field to a byte array, regardless of its type
    val dataBytes: Array[Byte] = field match {
      case data: Array[Byte] => data
      case data: String      => data.getBytes(StandardCharsets.UTF_8)
      case data              => data.toString.getBytes(StandardCharsets.UTF_8)
    }

    // Save the data file
    val fileStream = new ByteArrayInputStream(dataBytes)

    request.datasetIds.foreach { did =>
      val datasetPath = PathUtils.getDatasetPath(UInteger.valueOf(did))
      val filePath = datasetPath.resolve(filename)
      createNewDatasetVersionByAddingFiles(
        UInteger.valueOf(did),
        user,
        Map(filePath -> fileStream)
      )
    }

    ResultExportResponse(
      "success",
      s"Data file $filename saved to Datasets ${request.datasetIds.mkString(",")}"
    )
  }

  /**
    * move the workflow results to a specific folder
    */
  @tailrec
  private def moveToResultFolder(
      driveService: Drive,
      sheetId: String,
      retry: Boolean = true
  ): Unit = {
    val folderId = retrieveResultFolderId(driveService)
    try {
      driveService
        .files()
        .update(sheetId, null)
        .setAddParents(folderId)
        .execute()
    } catch {
      case exception: GoogleJsonResponseException =>
        if (retry) {
          // This exception maybe caused by the full deletion of the target folder and
          // the cached folder id is obsolete.
          //  * note: by full deletion, the folder has to be deleted from trash as well.
          // In this case, try again.
          moveToResultFolder(driveService, sheetId, retry = false)
        } else {
          // if the exception continues to show up then just throw it normally.
          throw exception
        }
    }
  }

  private def retrieveResultFolderId(driveService: Drive): String =
    synchronized {
      val folderResult: FileList = driveService
        .files()
        .list()
        .setQ(
          s"mimeType = 'application/vnd.google-apps.folder' and name='$WORKFLOW_RESULT_FOLDER_NAME'"
        )
        .setSpaces("drive")
        .execute()

      if (folderResult.getFiles.isEmpty) {
        val fileMetadata: File = new File()
        fileMetadata.setName(WORKFLOW_RESULT_FOLDER_NAME)
        fileMetadata.setMimeType("application/vnd.google-apps.folder")
        val targetFolder: File = driveService.files.create(fileMetadata).setFields("id").execute
        targetFolder.getId
      } else {
        folderResult.getFiles.get(0).getId
      }
    }

  /**
    * upload the result header to the google sheet
    */
  private def uploadHeader(
      sheetService: Sheets,
      sheetId: String,
      header: List[AnyRef]
  ): Unit = {
    uploadContent(sheetService, sheetId, List(header.asJava).asJava)
  }

  /**
    * upload the result body to the google sheet
    */
  private def uploadResult(
      sheetService: Sheets,
      sheetId: String,
      result: Iterable[Tuple]
  ): Unit = {
    val content: util.List[util.List[AnyRef]] =
      Lists.newArrayListWithCapacity(UPLOAD_BATCH_ROW_COUNT)
    // use for loop to avoid copying the whole result at the same time
    for (tuple: Tuple <- result) {

      val tupleContent: util.List[AnyRef] =
        tuple.getFields
          .map(convertUnsupported)
          .toArray
          .toList
          .asJava
      content.add(tupleContent)

      if (content.size() == UPLOAD_BATCH_ROW_COUNT) {
        uploadContent(sheetService, sheetId, content)
        content.clear()
      }
    }

    if (!content.isEmpty) {
      uploadContent(sheetService, sheetId, content)
    }
  }

  /**
    * convert the tuple content into the type the Google Sheet API supports
    */
  private def convertUnsupported(content: Any): AnyRef = {
    content match {

      // if null, use empty string to represent.
      case null => ""

      // Google Sheet API supports String and number(long, int, double and so on)
      case _: String | _: Number => content.asInstanceOf[AnyRef]

      // convert all the other type into String
      case _ => content.toString
    }

  }

  /**
    * upload the content to the google sheet
    * The type of content is java list because the google API is in java
    */
  private def uploadContent(
      sheetService: Sheets,
      sheetId: String,
      content: util.List[util.List[AnyRef]]
  ): Unit = {
    val body: ValueRange = new ValueRange().setValues(content)
    val range: String = "A1"
    val valueInputOption: String = "RAW"

    // using retry logic here, to handle possible API errors, i.e., rate limit exceeded.
    retry(attempts = RETRY_ATTEMPTS, baseBackoffTimeInMS = BASE_BACK_OOF_TIME_IN_MS) {
      sheetService.spreadsheets.values
        .append(sheetId, range, body)
        .setValueInputOption(valueInputOption)
        .execute
    }

  }

}

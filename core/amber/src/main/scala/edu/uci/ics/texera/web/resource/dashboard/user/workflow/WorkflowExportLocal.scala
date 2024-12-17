package edu.uci.ics.texera.web.resource.dashboard.user.workflow

import edu.uci.ics.amber.core.storage.result.ResultStorage
import edu.uci.ics.amber.core.tuple.Tuple
import edu.uci.ics.amber.virtualidentity.{OperatorIdentity, WorkflowIdentity}
import edu.uci.ics.texera.web.auth.SessionUser
import edu.uci.ics.texera.web.resource.dashboard.user.workflow.WorkflowAccessResource.hasReadAccess
import org.jooq.types.UInteger

import java.io.{IOException, OutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.annotation.security.RolesAllowed
import javax.ws.rs._
import javax.ws.rs.core.{MediaType, Response, StreamingOutput}

case class OperatorOutputExportRequest(
                                        workflowId: Int,
                                        operatorIds: List[String]
                                      )

@Path("/workflow/export")
@Produces(Array("application/zip"))
@RolesAllowed(Array("REGULAR", "ADMIN"))
class WorkflowExportLocal {

  /**
   * This endpoint streams operator outputs as a ZIP file.
   *
   * @param user authenticated session user
   * @param request JSON body with workflowId and operatorIds
   * @return a ZIP file containing CSV files for each operator's output
   */
  @POST
  @Path("/operator-outputs")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def exportOperatorOutputsAsZip(
                                  @Auth user: SessionUser,
                                  request: OperatorOutputExportRequest
                                ): Response = {

    val uid = user.getUid
    val workflowId = request.workflowId
    val opIds = request.operatorIds

    // Check if the user has read access to the workflow
    if (!hasReadAccess(UInteger.valueOf(workflowId), uid)) {
      throw new ForbiddenException("User has no read access to this workflow's results.")
    }

    // Construct workflow identity
    val workflowIdentity = WorkflowIdentity(workflowId.toString)

    // Retrieve results for each operator
    val operatorResults = opIds.map { opId =>
      val opIdentity = OperatorIdentity(opId)
      val virtualDoc = ResultStorage.getOpResultStorage(workflowIdentity).get(opIdentity)
      if (virtualDoc != null) {
        val results = virtualDoc.get().to(Iterable)
        (opId, results)
      } else {
        (opId, Iterable.empty[Tuple])
      }
    }

    // Stream the zip file
    val streamingOutput = new StreamingOutput {
      override def write(outputStream: OutputStream): Unit = {
        val zipOut = new ZipOutputStream(outputStream)
        try {
          operatorResults.foreach { case (opId, results) =>
            if (results.nonEmpty) {
              val csvBytes = resultsToCsvBytes(results)
              val entryName = s"$opId.csv"
              val zipEntry = new ZipEntry(entryName)
              zipOut.putNextEntry(zipEntry)
              zipOut.write(csvBytes)
              zipOut.closeEntry()
            }
          }
        } catch {
          case e: IOException =>
            throw new WebApplicationException("Error writing ZIP content.", e)
        } finally {
          zipOut.close()
        }
      }
    }

    Response.ok(streamingOutput)
      .header("Content-Disposition", s"attachment; filename=workflow-$workflowId-operators.zip")
      .build()
  }

  /**
   * Convert a collection of tuples to CSV bytes.
   */
  private def resultsToCsvBytes(results: Iterable[Tuple]): Array[Byte] = {
    if (results.isEmpty) {
      return Array.emptyByteArray
    }

    val schema = results.head.getSchema
    val headers = schema.getAttributeNames
    val sb = new StringBuilder
    // write header
    sb.append(headers.mkString(",")).append("\n")

    // write rows
    results.foreach { tuple =>
      val fields = tuple.getFields.map { field =>
        val str = if (field == null) "" else field.toString
        if (str.contains(",") || str.contains("\"")) {
          "\"" + str.replace("\"", "\"\"") + "\""
        } else str
      }
      sb.append(fields.mkString(",")).append("\n")
    }

    sb.toString().getBytes("UTF-8")
  }
}

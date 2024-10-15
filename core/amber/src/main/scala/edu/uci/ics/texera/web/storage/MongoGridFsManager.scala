package edu.uci.ics.texera.web.storage

import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import com.mongodb.client.gridfs.{GridFSBucket, GridFSBuckets, GridFSDownloadStream}
import com.mongodb.client.model.Sorts
import org.bson.Document
import org.bson.types.ObjectId

import java.io.InputStream
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Try

class MongoGridFsManager(opId: String, database: MongoDatabase) {

  // Create a GridFS bucket with the name as opId
  private val gridFSBucket: GridFSBucket = GridFSBuckets.create(database, opId)

  /**
    * Inserts a single file into the GridFS bucket with the provided InputStream.
    *
    * @param inputStream The InputStream of the file to insert.
    * @return The ObjectId of the inserted file.
    */
  def insertOne(inputStream: InputStream): ObjectId = {
    gridFSBucket.uploadFromStream(new ObjectId().toHexString, inputStream, new GridFSUploadOptions())
  }

  /**
    * Inserts multiple files into the GridFS bucket using an iterator of InputStreams.
    *
    * @param inputStreams An iterator of InputStreams, each representing a file to insert.
    * @return A list of ObjectIds for the inserted files.
    */
  def insertMany(inputStreams: Iterator[InputStream]): List[ObjectId] = {
    inputStreams.map { inputStream =>
      val objectId = gridFSBucket.uploadFromStream(new ObjectId().toHexString, inputStream, new GridFSUploadOptions())
      inputStream.close() // Ensure each InputStream is closed after insertion
      objectId
    }.toList
  }

  /**
    * Returns an iterator of (ObjectId, GridFSDownloadStream) for each file in the GridFS bucket,
    * sorted in ascending order of ObjectId. The caller is responsible for closing each InputStream.
    *
    * @return Iterator of (ObjectId, GridFSDownloadStream) in ascending order by ObjectId
    */
  def iterateFiles: Iterator[(ObjectId, GridFSDownloadStream)] = {
    gridFSBucket.find().sort(Sorts.ascending("_id")).iterator().asScala.map { file =>
      (file.getObjectId, gridFSBucket.openDownloadStream(file.getObjectId))
    }
  }

  /**
    * Deletes a file by its ObjectId.
    * @param fileId The ObjectId of the file to delete.
    * @return Boolean indicating if the file was successfully deleted.
    */
  def deleteFile(fileId: ObjectId): Boolean = {
    Try {
      gridFSBucket.delete(fileId)
    }.isSuccess
  }
}
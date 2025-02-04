package edu.uci.ics.amber.storage.result.iceberg

import edu.uci.ics.amber.core.storage.model.BufferedItemWriter
import edu.uci.ics.amber.core.storage.{DocumentFactory, VFSURIFactory}
import edu.uci.ics.amber.core.storage.result.IcebergTableSchema
import edu.uci.ics.amber.core.tuple.Tuple
import edu.uci.ics.amber.core.virtualidentity.{
  ExecutionIdentity,
  OperatorIdentity,
  WorkflowIdentity
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IcebergDocumentConsoleMessagesSpec extends AnyFlatSpec with Matchers {

  "IcebergDocument" should "write and read console messages successfully" in {
    // Create a unique URI for the test document
    val uri = VFSURIFactory.createConsoleMessagesURI(
      WorkflowIdentity(0),
      ExecutionIdentity(0),
      OperatorIdentity("test_operator")
    )

    // Create the document
    val document = DocumentFactory.createDocument(uri, IcebergTableSchema.consoleMessagesSchema)
    val writer: BufferedItemWriter[Tuple] =
      document.writer("console_messages_test").asInstanceOf[BufferedItemWriter[Tuple]]
    writer.open()

    // Create and write console messages
    val messages: List[Tuple] = List(
      new Tuple(IcebergTableSchema.consoleMessagesSchema, Array("First console message")),
      new Tuple(IcebergTableSchema.consoleMessagesSchema, Array("Second console message"))
    )

    messages.foreach(message => writer.putOne(message))
    writer.close()

    // Read the messages back
    val retrievedMessages: List[Tuple] = document.get().toList.asInstanceOf[List[Tuple]]

    // Verify the messages
    retrievedMessages should contain theSameElementsAs messages
  }
}

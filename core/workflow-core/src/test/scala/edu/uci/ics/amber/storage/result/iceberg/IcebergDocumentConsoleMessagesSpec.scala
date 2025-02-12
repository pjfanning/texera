package edu.uci.ics.amber.storage.result.iceberg

import edu.uci.ics.amber.core.storage.{DocumentFactory, VFSURIFactory}
import edu.uci.ics.amber.core.storage.model._
import edu.uci.ics.amber.core.storage.result.ResultSchema
import edu.uci.ics.amber.core.tuple.{Schema, Tuple}
import edu.uci.ics.amber.core.virtualidentity._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll

import scala.util.{Try, Using}
import java.net.URI
import scala.util.Using.Releasable

class IcebergDocumentConsoleMessagesSpec
    extends AnyFlatSpec
    with Matchers
    with VirtualDocumentSpec[Tuple]
    with BeforeAndAfterAll {

  private val amberSchema: Schema = ResultSchema.consoleMessagesSchema
  var uri: URI = _

  override def beforeEach(): Unit = {
    uri = VFSURIFactory.createConsoleMessagesURI(
      WorkflowIdentity(0),
      ExecutionIdentity(0),
      OperatorIdentity("test_operator")
    )
    DocumentFactory.createDocument(uri, amberSchema)
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  override def generateSampleItems(): List[Tuple] =
    List(
      new Tuple(amberSchema, Array("First console message")),
      new Tuple(amberSchema, Array("Second console message")),
      new Tuple(amberSchema, Array("Third console message"))
    )

  implicit val bufferedItemWriterReleasable: Releasable[BufferedItemWriter[Tuple]] =
    (resource: BufferedItemWriter[Tuple]) => resource.close()

  "IcebergDocument" should "write and read console messages successfully" in {
    Using.resource(document.writer("console_messages_test")) { writer =>
      writer.open()
      generateSampleItems().foreach(writer.putOne)
      writer.close()
    }

    val retrievedMessages = Try(document.get().toList.collect { case t: Tuple => t }).getOrElse(Nil)
    retrievedMessages should contain theSameElementsAs generateSampleItems()
  }

  override def getDocument: VirtualDocument[Tuple] = {
    DocumentFactory.openDocument(uri)._1 match {
      case doc: VirtualDocument[Tuple] => doc
      case _                           => fail("Failed to open document as VirtualDocument[Tuple]")
    }
  }
}

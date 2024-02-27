package edu.uci.ics.texera.workflow.common.tuple.schema

import com.fasterxml.jackson.annotation.{JsonCreator, JsonIgnore, JsonProperty}
import com.google.common.base.Preconditions.checkNotNull

import scala.collection.mutable

case class Schema @JsonCreator() (
    @JsonProperty(value = "attributes", required = true) attributes: List[Attribute[_]]
) extends Serializable {

  require(attributes != null, "Attributes cannot be null")

  val attributeIndex: Map[String, Int] =
    attributes.view.map(_.attributeName.toLowerCase).zipWithIndex.toMap

  def this(attrs: Attribute[_]*) = {
    this(attrs.toList)
  }

  @JsonProperty(value = "attributes")
  def getAttributes: List[Attribute[_]] = attributes

  @JsonIgnore
  def getAttributeNames: List[String] = attributes.map(_.attributeName)

  def getIndex(attributeName: String): Int = {
    if (!containsAttribute(attributeName)) {
      throw new RuntimeException(s"$attributeName is not contained in the schema")
    }
    attributeIndex(attributeName.toLowerCase)
  }

  def getAttribute(attributeName: String): Attribute[_] = attributes(getIndex(attributeName))

  @JsonIgnore
  def containsAttribute(attributeName: String): Boolean =
    attributeIndex.contains(attributeName.toLowerCase)

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (attributes == null) 0 else attributes.hashCode)
    result = prime * result + (if (attributeIndex == null) 0 else attributeIndex.hashCode)
    result
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case that: Schema =>
        this.attributes == that.attributes && this.attributeIndex == that.attributeIndex
      case _ => false
    }

  override def toString: String = s"Schema[$attributes]"

  def getPartialSchema(indices: Array[Int]): Schema = {
    val partialAttributes = indices.map(attributes(_)).toList
    Schema(partialAttributes)
  }
}

object Schema {
  def builder(): Builder = Builder()

  case class Builder(private var attributes: List[Attribute[_]] = List.empty) {
    private val attributeNames: mutable.Set[String] = mutable.Set.empty

    def add(attribute: Attribute[_]): Builder = {
      require(attribute != null, "Attribute cannot be null")
      checkAttributeNotExists(attribute.attributeName)
      attributes ::= attribute
      attributeNames += attribute.attributeName.toLowerCase
      this
    }

    def add[T](attributeName: String): Builder = {
      add(Attribute(attributeName))
      this
    }

    def add(attributes: Iterable[Attribute[_]]): Builder = {
      attributes.foreach(add)
      this
    }

    def add(attributes: Attribute[_]*): Builder = {
      attributes.foreach(add)
      this
    }

    def add(schema: Schema): Builder = {
      checkNotNull(schema)
      add(schema.getAttributes)
      this
    }

    def build(): Schema = Schema(attributes.reverse)

    /**
      * Removes an attribute from the schema builder if it exists.
      *
      * @param attribute , the name of the attribute
      * @return this Builder object
      */
    def removeIfExists(attribute: String): Builder = {
      checkNotNull(attribute)
      attributes = attributes.filter((attr: Attribute[_]) => !attr.attributeName.equalsIgnoreCase(attribute))
      attributeNames.remove(attribute.toLowerCase)
      this
    }

    /**
      * Removes the attributes from the schema builder if they exist.
      *
      * @param attributes , the names of the attributes
      * @return this Builder object
      */
    def removeIfExists(attributes: Iterable[String]): Builder = {
      checkNotNull(attributes)
      attributes.foreach((attr: String) => checkNotNull(attr))
      attributes.foreach((attr: String) => this.removeIfExists(attr))
      this
    }

    /**
      * Removes the attributes from the schema builder if they exist.
      *
      * @param attributes , the names of the attributes
      * @return this Builder object
      */
    def removeIfExists(attributes: String*): Builder = {
      checkNotNull(attributes)
      this.removeIfExists(attributes)
      this
    }

    /**
      * Removes an attribute from the schema builder.
      * Fails if the attribute does not exist.
      *
      * @param attribute , the name of the attribute
      * @return this Builder object
      */
    def remove(attribute: String): Builder = {
      checkNotNull(attribute)
      checkAttributeExists(attribute)
      removeIfExists(attribute)
      this
    }

    /**
      * Removes the attributes from the schema builder.
      * Fails if an attributes does not exist.
      */
    def remove(attributes: Iterable[String]): Builder = {
      checkNotNull(attributes)
      attributes.foreach(attrName => checkNotNull(attrName))
      attributes.foreach(this.checkAttributeExists)
      this.removeIfExists(attributes)
      this
    }

    /**
      * Removes the attributes from the schema builder.
      * Fails if an attributes does not exist.
      *
      * @param attributes
      * @return the builder itself
      */
    def remove(attributes: String*): Builder = {
      checkNotNull(attributes)
      this.remove(attributes)
      this
    }

    private def checkAttributeNotExists(attributeName: String): Unit = {
      if (attributeNames.contains(attributeName.toLowerCase)) {
        throw new RuntimeException(s"Attribute $attributeName already exists in the schema")
      }
    }

    private def checkAttributeExists(attributeName: String): Unit = {
      if (!attributeNames.contains(attributeName.toLowerCase)) {
        throw new RuntimeException(s"Attribute $attributeName does not exist in the schema")
      }
    }
  }
}

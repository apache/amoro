package com.netease.arctic.spark.sql.catalyst.plans

import org.apache.spark.sql.catalyst.analysis.Resolver
import org.apache.spark.sql.catalyst.plans.logical.{Command, LogicalPlan, V2CreateTablePlan}
import org.apache.spark.sql.connector.catalog.{Identifier, TableCatalog}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.{ArrayType, MapType, StructField, StructType}

case class CreateArcticTableAsSelect(catalog: TableCatalog,
                                     tableName: Identifier,
                                     partitioning: Seq[Transform],
                                     query: LogicalPlan,
                                     validateQuery: LogicalPlan,
                                     properties: Map[String, String],
                                     writeOptions: Map[String, String],
                                     ignoreIfExists: Boolean) extends Command with V2CreateTablePlan {

  override def tableSchema: StructType = query.schema

  override def children: Seq[LogicalPlan] = Seq(query, validateQuery)

  override lazy val resolved: Boolean = childrenResolved && {
    // the table schema is created from the query schema, so the only resolution needed is to check
    // that the columns referenced by the table's partitioning exist in the query schema
    val references = partitioning.flatMap(_.references).toSet
    references.map(_.fieldNames).forall(findNestedField(query.schema, _).isDefined)
  }

  override def withPartitioning(rewritten: Seq[Transform]): V2CreateTablePlan = {
    this.copy(partitioning = rewritten)
  }

  def findNestedField(struct: StructType,
                      fieldNames: Seq[String],
                      includeCollections: Boolean = false,
                      resolver: Resolver = _ == _): Option[(Seq[String], StructField)] = {
    def prettyFieldName(nameParts: Seq[String]): String = {
      nameParts.map(quoteIfNeeded).mkString(".")
    }

    def quoteIfNeeded(part: String): String = {
      if (part.contains(".") || part.contains("`")) {
        s"`${part.replace("`", "``")}`"
      } else {
        part
      }
    }


    def findField(
                   struct: StructType,
                   searchPath: Seq[String],
                   normalizedPath: Seq[String]): Option[(Seq[String], StructField)] = {
      searchPath.headOption.flatMap { searchName =>
        val found = struct.fields.filter(f => resolver(searchName, f.name))
        if (found.length > 1) {
          val names = found.map(f => prettyFieldName(normalizedPath :+ f.name))
            .mkString("[", ", ", " ]")
          throw new UnsupportedOperationException(
            s"Ambiguous field name: ${prettyFieldName(normalizedPath :+ searchName)}. Found " +
              s"multiple columns that can match: $names")
        } else if (found.isEmpty) {
          None
        } else {
          val field = found.head
          (searchPath.tail, field.dataType, includeCollections) match {
            case (Seq(), _, _) =>
              Some(normalizedPath -> field)

            case (names, struct: StructType, _) =>
              findField(struct, names, normalizedPath :+ field.name)

            case (_, _, false) =>
              None // types nested in maps and arrays are not used

            case (Seq("key"), MapType(keyType, _, _), true) =>
              // return the key type as a struct field to include nullability
              Some((normalizedPath :+ field.name) -> StructField("key", keyType, nullable = false))

            case (Seq("key", names@_*), MapType(struct: StructType, _, _), true) =>
              findField(struct, names, normalizedPath ++ Seq(field.name, "key"))

            case (Seq("value"), MapType(_, valueType, isNullable), true) =>
              // return the value type as a struct field to include nullability
              Some((normalizedPath :+ field.name) ->
                StructField("value", valueType, nullable = isNullable))

            case (Seq("value", names@_*), MapType(_, struct: StructType, _), true) =>
              findField(struct, names, normalizedPath ++ Seq(field.name, "value"))

            case (Seq("element"), ArrayType(elementType, isNullable), true) =>
              // return the element type as a struct field to include nullability
              Some((normalizedPath :+ field.name) ->
                StructField("element", elementType, nullable = isNullable))

            case (Seq("element", names@_*), ArrayType(struct: StructType, _), true) =>
              findField(struct, names, normalizedPath ++ Seq(field.name, "element"))

            case _ =>
              None
          }
        }
      }
    }

    findField(struct, fieldNames, Nil)
  }
}

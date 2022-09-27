package com.netease.arctic.spark.sql.expressions;

import com.netease.arctic.spark.optimize.Expression;
import com.netease.arctic.spark.optimize.NamedReference;
import com.netease.arctic.spark.optimize.Transform;
import org.apache.spark.annotation.InterfaceStability;
import scala.collection.JavaConverters;

import java.util.Arrays;

@InterfaceStability.Evolving
public class Expressions {
  private Expressions() {
  }

  public static Transform apply(String name, Expression... args) {
    return LogicalExpressions.apply(name,
        JavaConverters.asScalaBufferConverter(Arrays.asList(args)).asScala().toSeq());
  }


  public static NamedReference column(String name) {
    return LogicalExpressions.parseReference(name);
  }


  public static Transform bucket(int numBuckets, String... columns) {
    NamedReference[] references = Arrays.stream(columns)
        .map(Expressions::column)
        .toArray(NamedReference[]::new);
    return LogicalExpressions.bucket(numBuckets, references);
  }

  public static Transform identity(String column) {
    return LogicalExpressions.identity(Expressions.column(column));
  }

}

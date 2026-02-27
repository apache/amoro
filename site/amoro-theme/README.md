# Amoro Theme

The Amoro theme is a theme for use with [Hugo](https://gohugo.io/).
The Amoro theme is copied from [Apache Iceberg Project](https://github.com/apache/iceberg-docs/tree/main/iceberg-theme) and modifications were made as needed.

## Hint Boxes

To add a hint box, use the `hint` shortcode and pass in a hint box variation type. Available
types are `info`, `success`, `warning`,  or `error`.

```
{{< hint info >}}
Here is a message for the hint box!
{{< /hint >}}
```

## Codetabs

To add a tabbed view of different code examples, use the `codetabs`, `addtab`, and `tabcontent` shortcodes directly
within a markdown page.

1. Define a named set of tabs, making sure that the name is unique across the current page.
```
{{% codetabs "LaunchSparkClient" %}}
{{% /codetabs %}}
```

2. Add one or more tabs using the `addtab` shortcode. The arguments to the `addtab` shortcode are tab name, tab group,
and tab type. (see the logic in `iceberg-theme.js` to see how these are used)
```css
{{% codetabs "LaunchSparkClient" %}}
{{% addtab "SparkSQL" "spark-queries" "spark-sql" %}}
{{% addtab "Spark-Shell" "spark-queries" "spark-shell" %}}
{{% addtab "PySpark" "spark-queries" "pyspark" %}}
{{% codetabs "LaunchSparkClient" %}}
{{% /codetabs %}}
```

3. Add content for each tab using the `tabcontent` shortcode.
```
{{% codetabs "LaunchSparkClient" %}}
{{% addtab "SparkSQL" "spark-queries" "spark-sql" %}}
{{% addtab "Spark-Shell" "spark-queries" "spark-shell" %}}
{{% addtab "PySpark" "spark-queries" "pyspark" %}}
{{% tabcontent "spark-sql"  %}}
\```sh
docker exec -it spark-iceberg spark-sql
\```
{{% /tabcontent %}}
{{% tabcontent "spark-shell" %}}
\```sh
docker exec -it spark-iceberg spark-shell
\```
{{% /tabcontent %}}
{{% tabcontent "pyspark" %}}
\```sh
docker exec -it spark-iceberg pyspark
\```
{{% /tabcontent %}}
{{% /codetabs %}}
```

Codetab "groups" are used to coordinate switching the tab view throughout an entire page.
To add a new group, update the code in `iceberg-theme.js`.
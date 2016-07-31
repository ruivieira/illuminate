import java.io.{BufferedWriter, File, FileWriter}

import org.apache.commons.lang3.StringEscapeUtils
import org.pegdown.PegDownProcessor

import scala.io.Source

case class Dependency(name: String, version: String)

case class Project(name: String, dependencies: Seq[Dependency])

case class Block(comment: String, code: String)

// ## The main object

object Illuminate {

  // *Regexp* to match comments
  val comment_matcher = "^\\s*//\\s?"

  def is_comment(line: String): Boolean = {
    comment_matcher.r.findFirstIn(line).isDefined
  }

  def read_resource(path: String): String = {
    val stream = getClass.getResourceAsStream(path)
    scala.io.Source.fromInputStream(stream).getLines().mkString("\n")
  }

  // Read the Javascript from the resources
  def readJavascript(): String = read_resource("/highlight.pack.js")

  // Read the CSS from the resources
  def readCSS(): String = read_resource("/default.css")

  def readLines(filename: String): Iterator[String] = {
    Source.fromFile(filename).getLines()
  }

  def extractBlocks(lines: Iterator[String]): Seq[Block] = {

    val sections = lines.map(_.replaceAll("^\\s+", ""))

    val parts = lines.foldLeft(Seq(Seq.empty[String])) { (acc, line) =>
      if (is_comment(line)) acc :+ Seq(line)
      else acc.init :+ (acc.last :+ line)
    }

    val blocks = parts.map { p =>
      if (p.size > 1) {
        if (is_comment(p.head)) {
          Block(comment = p.head, code = p.drop(1).mkString("\n"))
        } else {
          Block(comment = "", code = p.mkString("\n"))
        }

      } else {
        Block(comment = p.head, code = "")
      }

    }

    blocks.map { block =>
      Block(
          comment = new PegDownProcessor()
            .markdownToHtml(block.comment.replaceAll(comment_matcher, "")),
          code = StringEscapeUtils.escapeHtml4(block.code)
      )

    }

  }

  def create_row(block: Block): String = {

    s"""
      |<tr>
      |    <td class='comment_cell'>${block.comment}</td>
      |    <td class='code_cell'><pre><code class="scala">${block.code}</code></pre></td>
      |</tr>
    """.stripMargin

  }

  def build_html(project: Project, blocks: Seq[Block]): String = {

    val rows_html = blocks.map(create_row).mkString("\n")

    s"""
      |<html>
      |
      | <head>
      |   <title>Illuminate</title>
      |   <script>
            ${readJavascript()}
      |   </script>
      |   <style>
      |     ${readCSS()}
      |   </style>
      | </head>
      |
      | <body>
      |
      |    <table class='content'>
      |
      |       $rows_html
      |
      |    </table>
      |
      | <script>hljs.initHighlightingOnLoad();</script>
      | </body>
      |
      |</html>
    """.stripMargin

  }

  def save_html(output_file: String, html: String) = {
    val file = new File(output_file)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(html)
    bw.close()
  }

  def main(args: Array[String]): Unit = {

    val lines = readLines(
        "/Users/ruivieira/code/scala/illuminate/src/main/scala/illuminate.scala")

    val blocks = extractBlocks(lines)

    val current_dir = new java.io.File(".").getCanonicalPath

    val project = Project("Illuminate", Seq())

    val html = build_html(project, blocks)

    save_html(output_file = s"$current_dir/illuminate.html", html = html)

  }

}

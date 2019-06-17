package comments

import java.util.regex.Pattern

import comments.net.Client

object Comments {
  val CommentRegex: Pattern = Pattern.compile("<CommentsCorrections([^List].*?)>(.*?)</CommentsCorrections>")
  val PMIDRegex: Pattern = Pattern.compile("<PMID.*?>(.*)</PMID>")
  val TitleRegex: Pattern = Pattern.compile("<ArticleTitle>(.*)</ArticleTitle>")


  def extractArticleTitle(xml: String): Option[String] = {
    val title = TitleRegex.matcher(xml)
    if(title.find())
      Some(title.group(1))
    else
      None
  }

  def extractCommentPMID(xml: String): Set[String] = {
    var result = Set[String]()
    val comments = CommentRegex.matcher(xml)
    while(comments.find()) {
      if(comments.group(1).contains("CommentOn")) {
        val pmid = PMIDRegex.matcher(comments.group(2))
        if (pmid.find())
          result += pmid.group(1)
      }
    }
    result
  }

  def processComments(tree: CommentTree, client: Client): CommentTree = {
    val xml = client.fetch(tree.getPMID)
    if(xml.isDefined) {
      val formatted = xml.get.replace("\n", " ")
      tree.title = extractArticleTitle(formatted)
      extractCommentPMID(formatted).foreach(pmid => processComments(tree.addLeaf(pmid), client))
    }
    tree
  }


  def findComments(pmid: String, client: Client): String = {
    processComments(new CommentTree(pmid), client).last_title().mkString("|")
  }

  def main(args: Array[String]): Unit = {
    if(args.length == 3)
      println(findComments("28975607", new Client(null, 3)))
    else
      println(s"""Missing ${3 - args.length} parameters.
                 |Required parameters are: (PMID, EUtils API Key, Requests per second)
                 |Example: java -jar comments.jar 28975607 ABC1234 10""".stripMargin)
  }

}

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
    if(args.length >= 1)
      println(findComments(args(args.length - 1), new Client(if(args.length > 1) args(0) else null)))
    else
      println(s"""Missing parameters!
                 |With a key parameters are: EUtils API Key, PMID
                 |Example: java -jar comments.jar KEY1234 28975607
                 |Without a key parameters are: PMID
                 |Example: java -jar comments.jar 28975607""".stripMargin)
  }

}
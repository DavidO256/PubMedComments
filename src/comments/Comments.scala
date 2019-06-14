package comments

import java.util.regex.Pattern

import comments.net.Client

object Comments {
  val CommentRegex: Pattern = Pattern.compile("<CommentsCorrections([^List].*?)>(.*?)</CommentsCorrections>")
  val RefTypeRegex: Pattern = Pattern.compile("")
  val PMIDRegex: Pattern = Pattern.compile("<PMID.*?>(.*)</PMID>")
  val TitleRegex: Pattern = Pattern.compile("<ArticleTitle>(.*)</ArticleTitle>")


  def extractArticleTitle(xml: String): String = {
    val title = TitleRegex.matcher(xml)
    return if(title.find()) title.group(1) else null
  }

  def extractCommentPMID(xml: String): Set[String] = {
    var result = Set[String]()
    val comments = CommentRegex.matcher(xml)
    while(comments.find()) {
      if(comments.group(1).contains("RefType=\"CommentOn\"")) {
        val pmid = PMIDRegex.matcher(comments.group(2))
        if (pmid.find())
          result += pmid.group(1)
      }
    }
    return result
  }

  def processComments(tree: CommentTree, client: Client): CommentTree = {
    val xml = client.fetch(tree.getPMID).replace("\n", " ")
    tree.title = extractArticleTitle(xml)
    extractCommentPMID(xml).foreach(pmid => processComments(tree.addLeaf(pmid), client))
    return tree
  }


  def findComments(pmid: String, client: Client): String = {
    val comments = processComments(new CommentTree(pmid), client).last_title()
    return if(comments != null) comments.mkString("|") else ""
  }

  def main(args: Array[String]): Unit = {
    println(findComments(args(0), new Client(args(1), Integer.parseInt(args(2)))))
  }

}

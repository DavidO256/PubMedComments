package comments

import scala.collection.mutable
import scala.collection.mutable.Map

class CommentTree(pmid: String) {
  var leaves: List[CommentTree] = List[CommentTree]()
  var title: String = ""

  def getPMID: String = pmid
  def getTitle: String = title

  def addLeaf(pmid: String): CommentTree = {
    val leaf = new CommentTree(pmid)
    leaves +:= leaf
    leaf
  }

  def collectTitles(): mutable.Map[Int, Set[String]] = {
    def collect(tree: CommentTree, titles: mutable.Map[Int, Set[String]], depth: Int): mutable.Map[Int, Set[String]] = {
      if (!titles.contains(depth))
        titles += (depth -> Set[String]())
      if (tree.title != "")
        titles(depth) += tree.title
      tree.leaves.foreach(leaf => collect(leaf, titles, depth + 1))
      titles
    }
    collect(this, mutable.Map[Int, Set[String]](), 0)
  }


  def last_title(): Set[String] = {
    val titles = collectTitles()
    if(titles.nonEmpty)
      return titles(titles.keys.max)
    null
  }

}

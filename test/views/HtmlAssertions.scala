package views

import org.jsoup.nodes.Element
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import scala.collection.JavaConversions._

trait HtmlAssertions {
  self:MustMatchers =>

  def checkListContainsItems(parent:Element, keysToFind:Set[String]) = {
    val texts = parent.select("li").toSet.map((el:Element) => el.text())
    texts must be (keysToFind.map(k => Messages(k)))
    true
  }

  def checkElementTextIncludes(el:Element, keys : String*) = {
    val t = el.text()
    keys.foreach { k =>
      t must include (Messages(k))
    }
    true
  }
}

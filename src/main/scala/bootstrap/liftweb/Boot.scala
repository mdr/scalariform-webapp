package bootstrap.liftweb

import net.liftweb._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util._
import sitemap._
import util._
import Helpers._
import Loc._
import common._
import http._
import mapper._

class Boot {

  def boot {

    LiftRules.useXhtmlMimeType = false

    LiftRules.addToPackages("scalariform.webapp")

    // LiftRules.snippetDispatch.append( Map("Format" -> FormatSnippet))
    
    val entries = List(
      Menu.i("Home") / "index",
      Menu(
        Loc(
          name = "Static",
          link = Link(List("static"), true, "/static/index"),
          text = "Static Content")))

    LiftRules.setSiteMap(SiteMap(entries: _*))

    LiftRules.ajaxStart = Full(() ⇒ LiftRules.jsArtifacts.show("ajax-loader").cmd)

    LiftRules.ajaxEnd = Full(() ⇒ LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

  }
}

package scalariform.webapp.lib

import net.liftweb.common._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util._
import net.liftweb.util.BindPlus._
import net.liftweb.util.Helpers._
import scala.xml._
import JE._
import JsCmds._

object ExtraAjaxWidgets {

  def ajaxTextareaOnKeyUp(value: String, func: String ⇒ JsCmd, attrs: (String, String)*): Elem =
    ajaxTextareaOnKeyUp_*(value, Empty, SFuncHolder(func), attrs: _*)

  def ajaxTextareaOnKeyUp(value: String, jsFunc: Call, func: String ⇒ JsCmd, attrs: (String, String)*): Elem =
    ajaxTextareaOnKeyUp_*(value, Full(jsFunc), SFuncHolder(func), attrs: _*)

  private def deferCall(data: JsExp, jsFunc: Call): Call =
    Call(jsFunc.function, (jsFunc.params ++ List(AnonFunc(makeAjaxCall(data)))): _*)

  private def ajaxTextareaOnKeyUp_*(value: String, jsFunc: Box[Call], func: AFuncHolder, attrs: (String, String)*): Elem = {
    val raw = (funcName: String, value: String) ⇒ JsRaw("'" + funcName + "=' + encodeURIComponent(" + value + ".value)")
    val key = formFuncName

    fmapFunc(contextFuncBuilder(func)) { funcName ⇒
      (attrs.foldLeft(<textarea>{ value }</textarea>)(_ % _)) %
        ("onkeyup" -> (jsFunc match {
          case Full(f) ⇒ JsCrVar(key, JsRaw("this")) & deferCall(raw(funcName, key), f)
          case _ ⇒ makeAjaxCall(raw(funcName, "this"))
        }))
    }
  }
}
package code.snippet
import scalariform.formatter._
import scalariform.parser._
import scalariform.formatter.preferences._
import scala.xml._
import java.util.Date
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.util.BindPlus._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.jquery.JqJsCmds.DisplayMessage
import net.liftweb.http.js.JsCmd
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.S._
import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.js._
import net.liftweb.http.js.AjaxInfo
import JE._
import JsCmds._

class FormatSnippet {


  def format(xhtml: NodeSeq) = {

    def format(s: String): Either[String, String] = 
      try {
        Right(ScalaFormatter.format(s, FormattingPreferences()))
      } catch {
        case e: ScalaParserException => Left(e.toString())
      }
    
    def onInput(s: String): JsCmd = {
      format(s) match {
        case Left(error) =>
          SetHtml("output", Text(error))
        case Right(formattedSource) =>
          val output = <pre class="brush: scala">{ formattedSource }</pre>
          SetHtml("output", output) & Run("SyntaxHighlighter.highlight()")
      }
    }
   
    val inputArea = ajaxTextareaOnKeyUp("", onInput)
    xhtml.bind("formatter", "inputArea" -> inputArea)
    
  }

  def ajaxTextareaOnKeyUp(value: String, func: String => JsCmd, attrs: (String, String)*): Elem = 
  ajaxTextareaOnKeyUp_*(value, Empty, SFuncHolder(func), attrs: _*)

  def ajaxTextareaOnKeyUp(value: String, jsFunc: Call, func: String => JsCmd, attrs: (String, String)*): Elem = 
  ajaxTextareaOnKeyUp_*(value, Full(jsFunc), SFuncHolder(func), attrs: _*)

  private def deferCall(data: JsExp, jsFunc: Call): Call =
    Call(jsFunc.function, (jsFunc.params ++ List(AnonFunc(makeAjaxCall(data)))): _*)


  private def ajaxTextareaOnKeyUp_*(value: String, jsFunc: Box[Call], func: AFuncHolder, attrs: (String, String)*): Elem = {
    val raw = (funcName: String, value: String) => JsRaw("'" + funcName + "=' + encodeURIComponent(" + value + ".value)")
    val key = formFuncName

    fmapFunc(contextFuncBuilder(func)) {
      funcName =>
      (attrs.foldLeft(<textarea>{value}</textarea>)(_ % _)) %
      ("onkeyup" -> (jsFunc match {
              case Full(f) => JsCrVar(key, JsRaw("this")) & deferCall(raw(funcName, key), f)
              case _ => makeAjaxCall(raw(funcName, "this"))
            })
        )
    }
  }


}

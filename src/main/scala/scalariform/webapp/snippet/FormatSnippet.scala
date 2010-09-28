package scalariform.webapp.snippet

import net.liftweb.http.SessionVar
import net.liftweb.http.StatefulSnippet
import net.liftweb.http.DispatchSnippet
import net.liftweb.common._
import net.liftweb.http.SHtml._
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.util._
import net.liftweb.util.BindPlus._
import net.liftweb.util.Helpers._

import scalariform.formatter._
import scalariform.formatter.preferences._
import scalariform.parser._
import scalariform.webapp.lib.ExtraAjaxWidgets._

import scala.xml._
import PartialFunction.condOpt

import FormatSnippet._

class FormatSnippet {

  private var inputOpt: Option[String] = None

  def format(xhtml: NodeSeq) = {
    val inputArea = ajaxTextareaOnKeyUp("", s ⇒ {
      inputOpt = Some(s)
      doFormat(s)
    })
    xhtml.bind(NS, "inputArea" -> inputArea)
  }

  private def currentPreferences = FormattingPrefs.is

  private def setPreferences(prefs: FormattingPreferences) = FormattingPrefs(prefs)

  private def makePreferenceWidget(preference: PreferenceDescriptor[_]): NodeSeq = preference match {

    case BooleanPref(typedPreference) ⇒
      val currentValue = currentPreferences(typedPreference)
      def onChange(b: Boolean) = {
        setPreferences(currentPreferences.setPreference(typedPreference, b))
        inputOpt map doFormat getOrElse Noop
      }
      ajaxCheckbox(currentValue, onChange)

    case IntegerPref(typedPreference, _, _) ⇒
      val currentValue = currentPreferences(typedPreference)
      def onChange(s: String) = {
        val n = Integer.parseInt(s)
        setPreferences(currentPreferences.setPreference(typedPreference, n))
        inputOpt map doFormat getOrElse Noop
      }
      ajaxText(currentValue.toString, onChange)

  }

  private def makePreferencesWidgets(preferencesTemplate: NodeSeq): NodeSeq =
    AllPreferences.preferences flatMap { preference ⇒
      preferencesTemplate.bind(NS,
        "prefKey" -> preference.key,
        "prefName" -> preference.description,
        "prefDefault" -> preference.defaultValue.toString,
        "prefWidget" -> makePreferenceWidget(preference))
    }

  def preferences(xhtml: NodeSeq) = xhtml.bind(NS, "preferences" -> makePreferencesWidgets _)

  private def format(s: String): Either[String, String] =
    try {
      Right(ScalaFormatter.format(s, FormattingPrefs.is))
    } catch {
      case e: ScalaParserException ⇒ Left(e.toString())
    }

  private def doFormat(s: String): JsCmd =
    format(s) match {
      case Left(error) ⇒
        SetHtml("output", Text(error))
      case Right(formattedSource) ⇒
        val output = <pre class="brush: scala">{ formattedSource }</pre>
        SetHtml("output", output) & Run("SyntaxHighlighter.highlight()")
    }

}

object FormatSnippet {

  private val NS = "formatter"

  object BooleanPref {
    def unapply(descriptor: PreferenceDescriptor[_]): Option[PreferenceDescriptor[Boolean]] = condOpt(descriptor.preferenceType) {
      case prefType@BooleanPreference ⇒ prefType.cast(descriptor)
    }
  }
  object IntegerPref {
    def unapply(descriptor: PreferenceDescriptor[_]): Option[(PreferenceDescriptor[Int], Int, Int)] = condOpt(descriptor.preferenceType) {
      case prefType@IntegerPreference(min, max) ⇒ (prefType.cast(descriptor), min, max)
    }
  }

  object FormattingPrefs extends SessionVar[FormattingPreferences](FormattingPreferences())

}
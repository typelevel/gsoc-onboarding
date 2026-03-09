package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.SignallingRef
import calico.html.io.{*, given}
import calico.syntax.*
import scala.util.Random
import org.scalajs.dom

val hrishabhxcode: Contributor = Contributor("hrishabhxcode"):

  val formulas = List(
    "Euler Identity: e^(iπ) + 1 = 0",
    "Fourier Transform: F(ω) = ∫ f(t)e^{-iωt} dt",
    "Bayes Theorem: P(A|B) = P(B|A)P(A)/P(B)",
    "Gradient Descent: θ = θ - α∇J(θ)",
    "Shannon Capacity: C = B log2(1 + S/N)"
  )

  def randomFormula: String =
    Random.shuffle(formulas).head

  def copyToClipboard(text: String): IO[Unit] = 
    IO.delay(dom.window.navigator.clipboard.writeText(text))

  for
    current <- SignallingRef[IO].of("Click generate to see a formula").toResource
    history <- SignallingRef[IO].of(List.empty[String]).toResource

    el <- div(
      styleAttr := "font-family: sans-serif; max-width: 500px; margin: 20px auto; padding: 20px; border: 2px solid #673ab7; border-radius: 12px;",

      h2("📐 CS Formula Lab"),

      div(
        styleAttr := "background: #f4f4f9; padding: 15px; border-radius: 8px; margin-bottom: 20px;",
        h3(current),
        button(
          "📋 Copy",
          onClick --> (_.foreach(_ => current.get.flatMap(copyToClipboard)))
        )
      ),

      div(
        styleAttr := "display: flex; gap: 10px; margin-bottom: 20px;",
        button(
          "Generate & Record",
          styleAttr := "flex: 2; cursor: pointer; padding: 10px; background: #673ab7; color: white; border: none; border-radius: 6px;",
          onClick --> (_.foreach { _ => 
            val next = randomFormula
            current.set(next) *> history.update(h => (next :: h).take(5))
          })
        ),
        button(
          "🗑 Clear",
          styleAttr := "flex: 1; cursor: pointer; padding: 10px; background: #e57373; color: white; border: none; border-radius: 6px;",
          onClick --> (_.foreach(_ => history.set(List.empty)))
        )
      ),

      h4("Recent History:"),
      ul(
        children <-- history.map(_.map(text => li(text)))
      ),

      
      div(styleAttr := "border-top: 1px solid #eee; margin: 20px 0;"),
      
      p("Hi! I'm @hrishabhxcode from NIT Nagaland."),
      p("I agree to follow the Typelevel Code of Conduct & GSoC AI policy.")
    )
  yield el
package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*

val `omgupta-JUS`: Contributor = Contributor("omgupta-JUS"):
  SignallingRef[IO].of(0).toResource.flatMap { count =>
    div(
      cls := "p-4 border rounded shadow-sm bg-light",
      h3("Hello, I'm @omgupta-JUS \uD83D\uDC4B"),
      p(
        "I am excited to apply for GSoC 2026! ",
        "I agree to follow the Typelevel CoC and GSoC AI policy."
      ),
      div(
        cls := "mt-3",
        button(
          cls := "btn btn-primary",
          "Click me! Count: ",
          count.map(_.toString),
          onClick --> (_.foreach(_ => count.update(_ + 1)))
        )
      )
    )
  }

package gsoc
package contributors

import cats.effect.*

import fs2.concurrent.*
import fs2.dom.HtmlElement

import calico.html.io.{*, given}
import calico.syntax.*

val LSUDOKO: Contributor = Contributor("LSUDOKO"):
  SignallingRef[IO].of(false).toResource.flatMap { revealed =>
    div(
      p(
        "Hello, I'm ",
        span(
          styleAttr := "color: #2563eb; font-weight: bold",
          "@LSUDOKO"
        ),
        " on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
      ),
      p("I am contributing to cats-collections for GSoC 2026!"),
      revealed.map(r =>
        if r then p(
          styleAttr := "margin-top: 10px; padding: 10px; background-color: #f0f9ff; border-radius: 5px",
          "Fun fact: I love solving puzzles and building amazing things with Scala!"
        ) else div(s"")
      ),
      button(
        styleAttr := "margin-top: 10px; padding: 8px 16px; background-color: #2563eb; color: white; border: none; border-radius: 5px; cursor: pointer",
        onClick --> (_.foreach(_ => revealed.update(!_))),
        revealed.map(r =>
          if r then "Hide info" else "Click to reveal something cool!"
        )
      )
    )
  }

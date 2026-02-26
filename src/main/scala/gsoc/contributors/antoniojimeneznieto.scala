package gsoc
package contributors

import cats.effect.*

import fs2.concurrent.*
import fs2.dom.HtmlElement

import calico.html.io.{*, given}
import calico.syntax.*

val antoniojimeneznieto = Contributor("antoniojimeneznieto"):
    SignallingRef[IO].of(false).toResource.flatMap { revealed =>
      div(
        p(
          "I am ",
          revealed.map(r => if r then "mediumseagreen" else "inherit").changes.map { color =>
            span(
              styleAttr := s"color: $color; font-weight: bold",
              "@antoniojimeneznieto"
            )
          },
          " on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
        ),
        button(
          onClick --> (_.foreach(_ => revealed.update(!_))),
          revealed.map(r =>
            if r then "Hide my favorite color" else "Click to learn my favorite color!")
        )
      )
    }

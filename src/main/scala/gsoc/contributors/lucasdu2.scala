package gsoc
package contributors

import cats.effect.*
import cats.effect.std.Random

import fs2.concurrent.*
import fs2.dom.HtmlElement

import calico.html.io.{*, given}
import calico.syntax.*

// A very simple component that changes the color of my name.
val lucasdu2: Contributor = Contributor("lucasdu2"):
  SignallingRef[IO].of(false).toResource.flatMap { change =>
    div(
      p(
        "I am ",
        change.map(r => if r then "blue" else "green").changes.map { color =>
          span(
            styleAttr := s"color: $color; font-weight: bold",
            "@lucasdu2"
          )
        },
        " on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
      ),
      button(
        onClick --> (_.foreach(_ => change.update(!_))),
        change.map(r => "Change color! :grug-brain:")
      )
    )
  }

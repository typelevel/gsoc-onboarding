package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*

val `krrishverma1805-web`: Contributor = Contributor("krrishverma1805-web"):
  SignallingRef[IO].of(0).toResource.flatMap { answer =>
    div(
      p(
        "Hi, I'm ",
        span(
          styleAttr := "color: #6B4FBB; font-weight: bold",
          "@krrishverma1805-web"
        ),
        " on GitHub. I agree to follow the Typelevel Code of Conduct and the GSoC AI policy."
      ),
      p("I am contributing to cats-collections for GSoC 2026!"),
      p(
        styleAttr := "font-weight: bold",
        "Quiz: Which cats-collections data structure supports O(1) merge?"
      ),
      div(
        button(
          styleAttr := "margin: 5px; padding: 8px 16px; cursor: pointer;",
          onClick --> (_.foreach(_ => answer.update(_ => 1))),
          "Heap"
        ),
        button(
          styleAttr := "margin: 5px; padding: 8px 16px; cursor: pointer;",
          onClick --> (_.foreach(_ => answer.update(_ => 2))),
          "PairingHeap"
        ),
        button(
          styleAttr := "margin: 5px; padding: 8px 16px; cursor: pointer;",
          onClick --> (_.foreach(_ => answer.update(_ => 3))),
          "Diet"
        )
      ),
      answer.map {
        case 2 =>
          div(
            p(
              styleAttr := "color: green; font-weight: bold",
              "Correct! PairingHeap supports O(1) combine (merge). I contributed docs for it in cats-collections PR #803!"
            )
          )
        case 1 =>
          div(
            p(
              styleAttr := "color: #cc0000",
              "Not quite! Heap uses O(log N) merge. Hint: think PairingHeap!"
            )
          )
        case 3 =>
          div(
            p(
              styleAttr := "color: #cc0000",
              "Not quite! Diet is a Discrete Interval Encoding Tree. Hint: think PairingHeap!"
            )
          )
        case _ =>
          div(p(""))
      }
    )
  }

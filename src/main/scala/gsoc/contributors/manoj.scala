package gsoc
package contributors

import calico.html.io.{*, given}
import calico.syntax.*
import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement

val `manoj-apj`: Contributor = Contributor("manoj-apj"):
  SignallingRef[IO].of(List.empty[String]).toResource.flatMap { items =>
    SignallingRef[IO].of("").toResource.flatMap { inputText =>
      div(
        styleAttr := "font-family: system-ui, sans-serif;",
        h3("Immutable List Explorer"),
        p(
          i("@manoj-apj"),
          " — I agree to follow the Typelevel Code of Conduct and GSoC AI policy."
        ),
        p("Linked lists have O(1) prepend but O(n) append. Build your list and feel the difference!"),
        div(
          input.withSelf { self =>
            (
              typ := "text",
              placeholder := "enter element",
              value <-- inputText,
              onInput --> (_.foreach(_ => self.value.get.flatMap(inputText.set)))
            )
          },
          button(
            "Prepend · O(1)",
            onClick --> (_.foreach(_ =>
              inputText.get.flatMap { v =>
                if v.trim.nonEmpty then items.update(v.trim :: _) *> inputText.set("")
                else IO.unit
              }))
          ),
          button(
            "Append · O(n)",
            onClick --> (_.foreach(_ =>
              inputText.get.flatMap { v =>
                if v.trim.nonEmpty then items.update(_ :+ v.trim) *> inputText.set("")
                else IO.unit
              }))
          ),
          button(
            "Drop Head",
            onClick --> (_.foreach(_ => items.update(l => if l.nonEmpty then l.tail else l)))
          ),
          button(
            "Clear",
            onClick --> (_.foreach(_ => items.set(Nil)))
          )
        ),
        p(
          styleAttr := "font-family: monospace;",
          "List: ",
          items.map {
            case Nil => "(empty)"
            case l => l.mkString("[ ", " :: ", " :: Nil ]")
          }
        ),
        p("Head (O(1)): ", items.map(_.headOption.fold("—")(h => s"\"$h\""))),
        p("Size: ", items.map(_.size.toString)),
        p(
          styleAttr :=
            "background: #f0f4ff; padding: 10px 14px; border-radius: 6px; margin-top: 12px;",
          "💡 Have ideas to make immutable lists faster? ",
          a(
            href := "https://discord.gg/dcAFxD8S",
            target := "_blank",
            styleAttr := "color: #3355cc; font-weight: bold;",
            "Discuss in #summer-of-code on Typelevel Discord →"
          )
        )
      )
    }
  }

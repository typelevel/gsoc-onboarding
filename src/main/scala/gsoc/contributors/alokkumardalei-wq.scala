package gsoc
package contributors

import cats.effect.*

import fs2.concurrent.*
import fs2.dom.HtmlElement

import calico.html.io.{*, given}
import calico.syntax.*

val `alokkumardalei-wq`: Contributor = Contributor("alokkumardalei-wq"):
  SignallingRef[IO].of(false).toResource.flatMap { revealed =>
    div(
      styleTag(
        """|@keyframes heartbeat {
           |  0% { transform: scale(1); }
           |  15% { transform: scale(1.3); }
           |  30% { transform: scale(1); }
           |  45% { transform: scale(1.3); }
           |  60% { transform: scale(1); }
           |  100% { transform: scale(1); }
           |}
           |.heart {
           |  display: inline-block;
           |  animation: heartbeat 1.5s infinite;
           |  color: red;
           |}
        """.stripMargin
      ),
      p(
        "I am ",
        revealed.map(r => if r then "Scala" else "inherit").changes.map { color =>
          span(
            styleAttr := s"color: $color; font-weight: bold",
            "@alokkumardalei-wq"
          )
        },
        " on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
      ),
      revealed.map(r =>
        if r then
          p(
            styleAttr := "font-size: 1.5em; font-weight: bold; text-align: center; margin: 20px 0;",
            span(cls := "heart", "❤️"),
            " I love Typelevel ",
            span(cls := "heart", "❤️")
          )
        else div("")
      ),
      button(
        onClick --> (_.foreach(_ => revealed.update(!_))),
        revealed.map(r =>
          if r then "Hide"
          else "Click me to share my love for Typelevel!"
        )
      )
    )
  }

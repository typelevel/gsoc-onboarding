package gsoc
package contributors

import cats.effect.*
import cats.effect.syntax.all.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*
import scala.concurrent.duration.*

val etvinh: Contributor = Contributor("etvinh"):
  SignallingRef[IO].of(0).toResource.flatMap { frame =>
    val smallHeart = Vector(
      "  ###   ###  ",
      " ##### ##### ",
      " ########### ",
      "  #########  ",
      "   #######   ",
      "    #####    ",
      "     ###     ",
      "      #      ",
    )

    val bigHeart = Vector(
      " ####   #### ",
      "###### ######",
      "#############",
      " ########### ",
      "  #########  ",
      "   #######   ",
      "    #####    ",
      "     ###     ",
      "      #      ",
    )
    
    // lub, deflate, dub, deflate, pause
    val frames = Vector(bigHeart, smallHeart, bigHeart, smallHeart, smallHeart)
    val delays = Vector(120, 120, 120, 120, 800)

    val ticker = frame.get.flatMap { f =>
      IO.sleep(delays(f).millis) >> frame.update(f => (f + 1) % frames.size)
    }.foreverM.background

    ticker.flatMap { _ =>
      div(
        styleAttr := """
          font-family: arial;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 16px;
          padding: 20px;
        """,
        frame.map(f => frames(f)).changes.map { lines =>
          pre(
            styleAttr := """
              font-size: 20px;
              line-height: 1.3;
              margin: 0;
              color: rgb(219, 12, 84);
              transition: font-size 0.1s;
            """,
            lines.mkString("\n")
          )
        },
        p(
          "Hello, I'm @etvinh on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
        ),
        p("Excited to start building <3")
      )
    }
  }
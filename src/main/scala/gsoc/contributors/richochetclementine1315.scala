package gsoc
package contributors

import cats.effect.*
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.*
import fs2.dom.*
import calico.html.io.{*, given}
import calico.syntax.*
import scala.concurrent.duration.DurationInt

val richochetclementine1315: Contributor = Contributor("richochetclementine1315"):

  SignallingRef[IO].of(0).toResource.flatMap { count =>

    def evolve(n: Int): String = n match
      case n if n < 10  => "🥚"
      case n if n < 25  => "🐣"
      case n if n < 50  => "🐥"
      case n if n < 100 => "🐔"
      case n if n < 200 => "🦅"
      case _            => "🐉"

    def levelName(n: Int): String = n match
      case n if n < 10  => "Just an egg..."
      case n if n < 25  => "Hatching!"
      case n if n < 50  => "Baby chick!"
      case n if n < 100 => "Full chicken!"
      case n if n < 200 => "Eagle mode!"
      case _            => "DRAGON UNLOCKED 🔥"

    def nextAt(n: Int): String = n match
      case n if n < 10  => s"${10 - n} clicks to hatch"
      case n if n < 25  => s"${25 - n} clicks to grow"
      case n if n < 50  => s"${50 - n} clicks to mature"
      case n if n < 100 => s"${100 - n} clicks to evolve"
      case n if n < 200 => s"${200 - n} clicks to ascend"
      case _            => "MAX LEVEL!"

    val autoClicker: Resource[IO, Unit] =
      Stream
        .awakeEvery[IO](2.second)
        .evalMap(_ => count.update(_ + 1))
        .compile
        .drain
        .background
        .void

    autoClicker *> div(
      styleAttr := """
        font-family: monospace;
        width: 320px;
        padding: 24px;
        background: #1e1e2e;
        border-radius: 16px;
        border: 2px solid #cba6f7;
        color: #cdd6f4;
        text-align: center;
      """,
      h3(styleAttr := "color:#cba6f7; margin: 0 0 4px;", "🎮 Evolution Clicker"),
      p(styleAttr := "color:#6c7086; font-size:12px; margin:0 0 16px;", "Click to evolve your creature!  I agree to follow the Typelevel Code of Conduct and the GSoC AI policy"),

      // Big clickable emoji
      div(
        styleAttr := """
          font-size: 80px;
          cursor: pointer;
          user-select: none;
          margin: 16px 0;
          transition: transform 0.1s;
        """,
        count.map(evolve),
        onClick --> (_.foreach(_ => count.update(_ + 1)))
      ),

      // Level name
      p(styleAttr := "font-size: 18px; font-weight: bold; margin: 4px 0;", count.map(levelName)),

      // Click count
      p(styleAttr := "font-size: 14px; color: #89b4fa; margin: 4px 0;", count.map(n => s"Clicks: $n")),

      // Progress hint
      p(styleAttr := "font-size: 12px; color:#6c7086; margin: 4px 0;", count.map(nextAt)),

      // Auto clicker note
      p(styleAttr := "font-size: 11px; color:#45475a; margin-top: 16px;", "✨ Auto-clicking every 2s via fs2 Stream")
    )
  }
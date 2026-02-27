package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*

val `im-vedant`: Contributor = Contributor("im-vedant"):
  val questions: List[(String, List[String], Int)] = List(
    (
      "What does IO in Cats Effect represent?",
      List(
        "File I/O operations",
        "An effect that suspends side effects",
        "An async callback wrapper",
        "A monad transformer"
      ),
      1
    ),
    (
      "flatMap is also known as in FP?",
      List("map", "bind", "fold", "traverse"),
      1
    ),
    (
      "Which library provides SignallingRef?",
      List("Cats Effect", "Circe", "FS2", "http4s"),
      2
    )
  )

  // state: (questionIndex, score, selectedOptionIndex)
  SignallingRef[IO].of((0, 0, Option.empty[Int])).toResource.flatMap { state =>
    div(
      p(
        "Hello, I'm ",
        span(styleAttr := "color: #7c3aed; font-weight: bold", "@im-vedant"),
        " on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
      ),
      h3("Typelevel FP Trivia"),
      state.map {
        case (qIdx, score, selected) =>
          if qIdx >= questions.length then
            val msg =
              if score == questions.length then "Perfect score! You're a Typelevel wizard!"
              else if score >= 2 then "Great job! Keep exploring the ecosystem!"
              else if score >= 1 then "Keep learning — the FP journey is worth it!"
              else "Time to dive into the Typelevel docs!"
            div(
              p(
                styleAttr := "font-weight: bold; font-size: 1.2em",
                s"Score: $score / ${questions.length}"
              ),
              p(msg),
              button(
                styleAttr :=
                  "background: #7c3aed; color: white; padding: 8px 16px; border-radius: 4px; cursor: pointer; border: none; margin-top: 8px",
                onClick --> (_.foreach(_ => state.set((0, 0, None)))),
                "Play again"
              )
            )
          else
            val (question, options, correct) = questions(qIdx)
            div(
              p(
                styleAttr := "font-weight: bold",
                s"Q${qIdx + 1} / ${questions.length}: $question"),
              div(
                options.zipWithIndex.map {
                  case (opt, idx) =>
                    val bg = selected match
                      case None => "#e5e7eb"
                      case Some(_) if idx == correct => "#bbf7d0"
                      case Some(s) if idx == s => "#fecaca"
                      case _ => "#e5e7eb"
                    button(
                      styleAttr :=
                        s"background: $bg; margin: 4px 0; padding: 8px 14px; border-radius: 4px; cursor: pointer; border: 1px solid #ccc; display: block; width: 100%",
                      onClick --> (_.foreach { _ =>
                        selected match
                          case Some(_) => IO.unit
                          case None =>
                            val newScore = if idx == correct then score + 1 else score
                            state.set((qIdx, newScore, Some(idx)))
                      }),
                      opt
                    )
                }
              ),
              selected.fold(div("")) { _ =>
                button(
                  styleAttr :=
                    "background: #7c3aed; color: white; padding: 8px 16px; border-radius: 4px; cursor: pointer; border: none; margin-top: 8px",
                  onClick --> (_.foreach(_ => state.set((qIdx + 1, score, None)))),
                  if qIdx + 1 >= questions.length then "See results" else "Next question"
                )
              }
            )
      }
    )
  }

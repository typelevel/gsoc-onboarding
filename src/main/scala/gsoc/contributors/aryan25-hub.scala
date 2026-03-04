package gsoc
package contributors

import cats.effect.*
import cats.syntax.all.*
import fs2.concurrent.SignallingRef
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*

import scala.concurrent.duration.*
import scala.util.Random

val `aryan25-hub`: Contributor = Contributor("aryan25-hub"):
  enum Phase:
    case Idle
    case Waiting
    case ClickNow(readyAtMs: Long)

  final case class Model(
      phase: Phase,
      bestMs: Option[Long],
      lastMs: Option[Long],
      message: String
  )

  def nowMs: IO[Long] = IO.realTime.map(_.toMillis)

  def randomDelay: IO[FiniteDuration] =
    IO.delay((800 + Random.nextInt(1400)).millis) // 0.8s .. 2.2s

  def startRound(state: SignallingRef[IO, Model]): IO[Unit] =
    for
      _ <- state.update(
        _.copy(
          phase = Phase.Waiting,
          lastMs = None,
          message = "Wait for it..."
        ))
      d <- randomDelay
      _ <- IO.sleep(d)
      t <- nowMs
      _ <- state.update(
        _.copy(
          phase = Phase.ClickNow(t),
          message = "NOW! Click!"
        ))
    yield ()

  def click(state: SignallingRef[IO, Model]): IO[Unit] =
    state.get.flatMap {
      case Model(Phase.ClickNow(t0), best, _, _) =>
        for
          t1 <- nowMs
          ms = (t1 - t0).max(0L)
          newBest = best.fold(ms)(b => b.min(ms))
          _ <- state.set(
            Model(
              phase = Phase.Idle,
              bestMs = Some(newBest),
              lastMs = Some(ms),
              message = s"Nice! Reaction time: ${ms}ms"
            ))
        yield ()

      case Model(Phase.Waiting, best, _, _) =>
        // clicked too early
        state.set(
          Model(
            phase = Phase.Idle,
            bestMs = best,
            lastMs = None,
            message = "Too soon! Try again."
          ))

      case _ =>
        IO.unit
    }

  def buttonLabel(m: Model): String =
    m.phase match
      case Phase.Idle => "Start"
      case Phase.Waiting => "Wait..."
      case Phase.ClickNow(_) => "NOW!"

  def isDisabled(m: Model): Boolean =
    m.phase match
      case Phase.Idle => false
      case Phase.Waiting => false // allow early click (it will fail you)
      case Phase.ClickNow(_) => false

  SignallingRef[IO]
    .of(
      Model(
        phase = Phase.Idle,
        bestMs = None,
        lastMs = None,
        message = "Click Start to play."
      )
    )
    .toResource
    .flatMap { state =>
      div(
        h2("@aryan25-hub"),
        p("I agree to follow the Typelevel Code of Conduct and the Typelevel GSoC AI Policy."),
        p("Mini-game: Reaction Time"),
        state.map(m => p(m.message)),
        button(
          onClick --> (_.foreach(_ =>
            state.get.flatMap {
              case m if m.phase == Phase.Idle => startRound(state)
              case _ => click(state)
            })),
          state.map(buttonLabel)
        ),
        state.map { m =>
          val best = m.bestMs.fold("—")(_.toString + "ms")
          val last = m.lastMs.fold("—")(_.toString + "ms")
          div(
            p(s"Last: $last"),
            p(s"Best: $best")
          )
        }
      )
    }

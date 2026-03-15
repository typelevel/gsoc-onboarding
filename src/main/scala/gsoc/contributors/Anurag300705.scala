package gsoc
package contributors

import calico.*
import calico.html.io.{*, given}
import cats.effect.*
import fs2.concurrent.*
import fs2.dom.*
import scala.util.Random

val Anurag300705 = Contributor("Anurag300705"):
  import RandomNumberGuesser.*
  SignallingRef[IO].of(freshState).toResource.flatMap { state =>
    val submit: IO[Unit] = state.get.flatMap { s =>
      if s.won then IO.unit
      else if s.attempts >= maxAttempts then
        state.update(
          _.copy(
            won = true,
            message = s"Game Over! The number was ${s.secret}"
          ))
      else
        parseGuess(s.input) match
          case None =>
            state.update(
              _.copy(
                input = "",
                message = "Please enter a valid integer."
              ))
          case Some(guess) =>
            val updatedGuesses = guess :: s.guesses
            if guess == s.secret then
              state.update(
                _.copy(
                  hint = Hint.Correct,
                  attempts = s.attempts + 1,
                  guesses = updatedGuesses,
                  won = true,
                  input = "",
                  message = s"🎉 Correct! You got it in ${s.attempts + 1} attempts."
                ))
            else if guess < s.secret then
              state.update(
                _.copy(
                  hint = Hint.TooLow,
                  attempts = s.attempts + 1,
                  guesses = updatedGuesses,
                  input = "",
                  message = "Too low, try again."
                ))
            else if guess > max then
              state.update(
                _.copy(
                  hint = Hint.OverBound,
                  attempts = s.attempts + 1,
                  guesses = updatedGuesses,
                  input = "",
                  message =
                    s"This is more than $max, please enter a number between 1 and $max"
                ))
            else
              state.update(
                _.copy(
                  hint = Hint.TooHigh,
                  attempts = s.attempts + 1,
                  guesses = updatedGuesses,
                  input = "",
                  message = "Too high, try again."
                ))
    }

    div(
      p(
        styleAttr := "font-size: 0.9em; color: gray",
        "Hello, I'm @Anurag300705 on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
      ),

      p(styleAttr := "font-weight: bold", s"Guess a number between 1 and $max"),

      state.map(_.message).changes.map { msg =>
        p(styleAttr := "margin: 8px 0", msg)
      },

      state.map(_.hint).map {
        case Hint.NoGuess => p("Make your guess!")
        case Hint.TooLow => p(styleAttr := "color: red", "↑ Too low, try again.")
        case Hint.TooHigh => p(styleAttr := "color: blue", "↓ Too high, try again.")
        case Hint.OverBound =>
          p(styleAttr := "color: indigo", "OverBound, please try within the limits.")
        case Hint.Correct =>
          p(styleAttr := "color: green; font-weight: bold", "✓ Correct!")
      },

      state.map(s => s"Attempts: ${s.attempts} / $maxAttempts").changes.map { t =>
        p(styleAttr := "font-size: 0.85em; color: gray", t)
      },

      state
        .map(s => s"Previous guesses: ${s.guesses.reverse.mkString(", ")}")
        .changes
        .map(t => p(styleAttr := "color: darkorange", t)),

      input.withSelf { self =>
        (
          typ := "number",
          disabled <-- state.map(_.won),
          value <-- state.map(_.input),
          onInput --> (_.foreach { _ =>
            self.value.get.flatMap(e => state.update(_.copy(input = e)))
          }),
          onKeyDown --> (_.foreach { ev =>
            if ev.key == "Enter" then submit else IO.unit
          })
        )
      },

      button(
        disabled <-- state.map(_.won),
        onClick --> (_.foreach(_ => submit)),
        state.map(s => if s.won then "✓ Done" else "Guess").changes
      ),

      button(
        onClick --> (_.foreach(_ => state.set(freshState))),
        "Reset Game"
      )
    )
  }

private object RandomNumberGuesser:
  val max = 20
  val maxAttempts = 7

  enum Hint:
    case NoGuess
    case TooLow
    case TooHigh
    case OverBound
    case Correct

  case class GameState(
      secret: Int,
      input: String,
      hint: Hint,
      attempts: Int,
      guesses: List[Int],
      won: Boolean,
      message: String
  )

  def freshState: GameState =
    GameState(
      secret = Random.nextInt(max) + 1,
      input = "",
      hint = Hint.NoGuess,
      attempts = 0,
      guesses = List(),
      won = false,
      message = s"I'm thinking of a number between 1 and $max. Can you guess it?"
    )

  def parseGuess(str: String): Option[Int] =
    str.trim.toIntOption
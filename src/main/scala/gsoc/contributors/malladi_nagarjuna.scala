package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*
import cats.syntax.flatMap.*
import scala.concurrent.duration.*
import scala.util.Random

private enum SnakeDir:
  case Up, Down, Left, Right

private case class SnakeGameState(
    snake: List[(Int, Int)],
    food: (Int, Int),
    dir: SnakeDir,
    gameOver: Boolean,
    started: Boolean,
    score: Int
)

val malladinagarjuna2: Contributor = Contributor("malladinagarjuna2"):
  val gridSize = 15

  def randomFood(): (Int, Int) =
    (Random.nextInt(gridSize), Random.nextInt(gridSize))

  val initial = SnakeGameState(
    snake = List((7, 7), (6, 7), (5, 7)),
    food = (10, 10),
    dir = SnakeDir.Right,
    gameOver = false,
    started = false,
    score = 0
  )

  def move(s: SnakeGameState): SnakeGameState =
    if !s.started || s.gameOver then s
    else
      val (hx, hy) = s.snake.head
      val newHead = s.dir match
        case SnakeDir.Up => (hx, hy - 1)
        case SnakeDir.Down => (hx, hy + 1)
        case SnakeDir.Left => (hx - 1, hy)
        case SnakeDir.Right => (hx + 1, hy)
      val (nx, ny) = newHead
      if nx < 0 || nx >= gridSize || ny < 0 || ny >= gridSize || s.snake.contains(newHead)
      then s.copy(gameOver = true)
      else if newHead == s.food then
        s.copy(snake = newHead :: s.snake, food = randomFood(), score = s.score + 1)
      else s.copy(snake = newHead :: s.snake.dropRight(1))

  def changeDir(current: SnakeDir, key: String): SnakeDir =
    import SnakeDir.*
    key match
      case "ArrowUp" if current != Down => Up
      case "ArrowDown" if current != Up => Down
      case "ArrowLeft" if current != Right => Left
      case "ArrowRight" if current != Left => Right
      case _ => current

  SignallingRef[IO].of(initial).toResource.flatMap { state =>
    val gameLoop = fs2
      .Stream
      .fixedRate[IO](200.millis)
      .evalMap(_ => state.update(move))
      .compile
      .drain
      .background

    gameLoop >> div(
      tabIndex := 0,
      styleAttr := "outline: none; font-family: monospace;",
      onKeyDown --> (_.foreach { e =>
        e.key match
          case "ArrowUp" =>
            e.preventDefault *> state
              .update(s => s.copy(started = true, dir = changeDir(s.dir, "ArrowUp")))
          case "ArrowDown" =>
            e.preventDefault *> state
              .update(s => s.copy(started = true, dir = changeDir(s.dir, "ArrowDown")))
          case "ArrowLeft" =>
            e.preventDefault *> state
              .update(s => s.copy(started = true, dir = changeDir(s.dir, "ArrowLeft")))
          case "ArrowRight" =>
            e.preventDefault *> state
              .update(s => s.copy(started = true, dir = changeDir(s.dir, "ArrowRight")))
          case _ => IO.unit
      }),
      p(
        "Hello, I'm @malladinagarjuna2 on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."
      ),
      p(
        styleAttr := "font-size: 13px; color: #888; margin: 4px 0;",
        "Click here & use arrow keys to play Snake!"
      ),
      h3("Snake Game"),
      pre(
        styleAttr := "font-size: 14px; line-height: 1.1;",
        state.map { s =>
          val grid = (0 until gridSize)
            .map { y =>
              (0 until gridSize).map { x =>
                if s.snake.head == (x, y) then "\uD83D\uDFE2"
                else if s.snake.tail.contains((x, y)) then "\uD83D\uDFE9"
                else if s.food == (x, y) then "\uD83D\uDD34"
                else "\u2B1B"
              }.mkString
            }
            .mkString("\n")
          s"Score: ${s.score}\n$grid" +
            (if s.gameOver then "\nGame Over! Click Restart."
             else if !s.started then "\nPress an arrow key to start!"
             else "")
        }
      ),
      button(
        "Restart",
        styleAttr := "margin-top: 8px; padding: 6px 16px; cursor: pointer;",
        onClick --> (_.foreach(_ => state.set(initial)))
      )
    )
  }

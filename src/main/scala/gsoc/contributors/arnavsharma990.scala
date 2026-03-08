package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*

private object ArnavTicTacToe:
  enum Player:
    case X, O
    def symbol: String = this match
      case X => "✕"
      case O => "○"
    def next: Player = this match
      case X => O
      case O => X

  case class State(
      board: Vector[Option[Player]],
      turn: Player,
      result: Option[String]
  )

  val fresh: State = State(Vector.fill(9)(None), Player.X, None)

  private val lines: List[(Int, Int, Int)] = List(
    (0, 1, 2),
    (3, 4, 5),
    (6, 7, 8),
    (0, 3, 6),
    (1, 4, 7),
    (2, 5, 8),
    (0, 4, 8),
    (2, 4, 6)
  )

  def checkResult(board: Vector[Option[Player]]): Option[String] =
    lines
      .collectFirst {
        case (a, b, c) if board(a).isDefined && board(a) == board(b) && board(b) == board(c) =>
          board(a).fold("")(p => s"${p.symbol} wins!")
      }
      .orElse(Option.when(board.forall(_.isDefined))("It's a draw!"))

  def isWinningCell(board: Vector[Option[Player]], idx: Int): Boolean =
    lines.exists {
      case (a, b, c) =>
        List(a, b, c).contains(idx) &&
        board(a).isDefined && board(a) == board(b) && board(b) == board(c)
    }

val arnavsharma990: Contributor = Contributor("arnavsharma990"):
  import ArnavTicTacToe.*

  SignallingRef[IO].of(fresh).toResource.flatMap { state =>
    def play(idx: Int): IO[Unit] =
      state.update { s =>
        if s.board(idx).isDefined || s.result.isDefined then s
        else
          val newBoard = s.board.updated(idx, Some(s.turn))
          State(newBoard, s.turn.next, checkResult(newBoard))
      }

    div(
      p(
        "I am ",
        strong("@arnavsharma990"),
        " on GitHub. I agree to follow the Typelevel Code of Conduct and Typelevel GSoC AI Policy."
      ),
      p(b("Tic-Tac-Toe — challenge a friend!")),
      state
        .map(s => s.result.getOrElse(s"${s.turn.symbol}'s turn"))
        .changes
        .map(msg => p(styleAttr := "font-weight:bold;font-size:1.1em", msg)),
      div(
        styleAttr := "display:inline-grid;grid-template-columns:repeat(3,64px);gap:4px;margin:8px 0",
        (0 until 9).toList.map { idx =>
          button(
            styleAttr <-- state.map { s =>
              val bg =
                if s.result.isDefined && isWinningCell(s.board, idx) then "#2ecc71"
                else "#ecf0f1"
              val clr = s.board(idx) match
                case Some(Player.X) => "#e74c3c"
                case Some(Player.O) => "#3498db"
                case None => "#333"
              s"width:64px;height:64px;font-size:1.8em;font-weight:bold;cursor:pointer;background:$bg;color:$clr;border:1px solid #bdc3c7;border-radius:4px;display:flex;align-items:center;justify-content:center"
            },
            state.map(s => s.board(idx).fold("")(_.symbol)),
            onClick --> (_.foreach(_ => play(idx)))
          )
        }
      ),
      button(
        styleAttr := "margin-top:8px;padding:6px 18px;font-size:1em;cursor:pointer",
        onClick --> (_.foreach(_ => state.set(fresh))),
        "New Game 🔄"
      )
    )
  }

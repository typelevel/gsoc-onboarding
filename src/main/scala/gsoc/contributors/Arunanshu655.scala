package gsoc
package contributors

import cats.effect.*
import cats.effect.unsafe.implicits.global
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*
import cats.syntax.all.*
import org.scalajs.dom.HTMLSelectElement

import scala.concurrent.duration.*

val Arunanshu655: Contributor = Contributor("Arunanshu655"):

  case class State(
      input: String,
      array: List[Int],
      algorithm: String
  )

  def parseInput(str: String): List[Int] =
    str.split(",").toList.flatMap(s => s.trim.toIntOption)

  def bubbleAnimated(
      arr: List[Int],
      state: SignallingRef[IO, State]
  ): IO[Unit] =
    val a = arr.toArray
    val n = a.length
    def bubblePass(i: Int): IO[Unit] =
      if i >= n then IO.unit
      else
        def swapPass(j: Int): IO[Unit] =
          if j >= n - i - 1 then IO.unit
          else if a(j) > a(j + 1) then
            val t = a(j)
            a(j) = a(j + 1)
            a(j + 1) = t
            for
              _ <- state.update(_.copy(array = a.toList))
              _ <- IO.sleep(300.millis)
              _ <- swapPass(j + 1)
            yield ()
          else swapPass(j + 1)
        for
          _ <- swapPass(0)
          _ <- bubblePass(i + 1)
        yield ()
    bubblePass(0)

  def insertionAnimated(
      arr: List[Int],
      state: SignallingRef[IO, State]
  ): IO[Unit] =
    val a = arr.toArray
    def insertPass(i: Int): IO[Unit] =
      if i >= a.length then IO.unit
      else
        val key = a(i)
        def shiftPass(j: Int): IO[Unit] =
          if j < 0 || a(j) <= key then
            a(j + 1) = key
            for
              _ <- state.update(_.copy(array = a.toList))
              _ <- IO.sleep(300.millis)
              _ <- insertPass(i + 1)
            yield ()
          else
            a(j + 1) = a(j)
            for
              _ <- state.update(_.copy(array = a.toList))
              _ <- IO.sleep(150.millis)
              _ <- shiftPass(j - 1)
            yield ()
        shiftPass(i - 1)
    insertPass(1)

  def selectionAnimated(
      arr: List[Int],
      state: SignallingRef[IO, State]
  ): IO[Unit] =
    val a = arr.toArray
    def selectPass(i: Int): IO[Unit] =
      if i >= a.length then IO.unit
      else
        def findMin(j: Int, min: Int): Int =
          if j >= a.length then min
          else findMin(j + 1, if a(j) < a(min) then j else min)
        val minIdx = findMin(i + 1, i)
        if minIdx == i then selectPass(i + 1)
        else
          val t = a(i)
          a(i) = a(minIdx)
          a(minIdx) = t
          for
            _ <- state.update(_.copy(array = a.toList))
            _ <- IO.sleep(300.millis)
            _ <- selectPass(i + 1)
          yield ()
    selectPass(0)

  def runSort(state: SignallingRef[IO, State]): IO[Unit] =
    for
      s <- state.get
      arr = parseInput(s.input)
      _ <- s.algorithm match
        case "Bubble" => bubbleAnimated(arr, state)
        case "Insertion" => insertionAnimated(arr, state)
        case "Selection" => selectionAnimated(arr, state)
        case _ => IO.pure(())
    yield ()

  val colors =
    List("#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2")

  SignallingRef[IO].of(State("", List.empty, "Bubble")).toResource.flatMap { state =>
    div(
      styleAttr := "padding:30px;font-family:system-ui,sans-serif;background:#f5f5f5;border-radius:8px;",
      p(
        styleAttr := "margin:0 0 20px 0;font-size:1.1em;color:#555;",
        "Hola, I'm @Arunanshu655 on GitHub. I agree to follow the Typelevel Code of Conduct and the Typelevel GSoC AI Policy."
      ),
      h3(
        styleAttr := "margin:0 0 20px 0;font-size:1.8em;color:#333;",
        "Simple Sorting Visualizer"
      ),
      input.withSelf { self =>
        (
          placeholder := "Enter numbers like 5,3,8,1(more number numbers for better visuals)",
          styleAttr := "padding:12px;margin-bottom:15px;width:100%;box-sizing:border-box;font-size:1em;border:2px solid #ddd;border-radius:4px;",
          value <-- state.map(_.input),
          onInput --> (_.foreach(_ =>
            self.value.get.flatMap(v => state.update(s => s.copy(input = v)))))
        )
      },
      div(styleAttr := "height:12px;"),
      select.withSelf { self =>
        (
          styleAttr := "padding:12px;margin-right:12px;margin-bottom:15px;font-size:1.05em;border:2px solid #ddd;border-radius:4px;cursor:pointer;",
          option(value := "Bubble", "Bubble Sort"),
          option(value := "Insertion", "Insertion Sort"),
          option(value := "Selection", "Selection Sort"),
          value <-- state.map(_.algorithm),
          onChange --> (_.foreach(_ =>
            self.value.get.flatMap(v => state.update(s => s.copy(algorithm = v)))))
        )
      },
      button(
        styleAttr := "padding:14px 28px;margin-bottom:15px;cursor:pointer;font-size:1.1em;background:#333;color:white;border:none;border-radius:4px;font-weight:bold;",
        "Sort",
        onClick --> (_.foreach(_ => runSort(state)))
      ),
      div(
        styleAttr := "margin-top:30px;display:flex;align-items:flex-end;gap:12px;justify-content:center;flex-wrap:wrap;",
        children <-- state.map(_.array.mapWithIndex {
          case (n, idx) =>
            div(
              styleAttr := "display:flex;flex-direction:column;align-items:center;gap:8px;",
              div(
                styleAttr := s"height:${n * 12}px;width:32px;background:${colors(idx % colors.length)};transition:all 0.15s ease;border-radius:2px;box-shadow:0 2px 4px rgba(0,0,0,0.1);"
              ),
              div(
                styleAttr := "font-size:15px;font-weight:bold;color:#333;min-width:32px;text-align:center;",
                n.toString
              )
            )
        })
      )
    )
  }

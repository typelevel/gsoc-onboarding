package gsoc
package contributors

import cats.effect.*
import cats.syntax.all.*

import fs2.concurrent.*
import fs2.dom.HtmlElement

import calico.html.io.{*, given}
import calico.syntax.*

import fs2.*

val jarmuszz = Contributor("jarmuszz") {
  for
    defaultCode =
      """# Helpers
        |: join-text space swap join join ;
        |: handle @ swap join ;
        |: period . join ;

        |# Shorthands
        |: username jarmuszz ;
        |: whoami username handle on GitHub join-text join-text ;
        |: coc Typelevel CoC join-text ;
        |: ai GSoC AI policy join-text join-text ;

        |: introduction I am whoami join-text 2 repeat period ;
        |: agreement I agree to follow the coc and ai join-text 7 repeat period ;

        |introduction agreement join-text""".stripMargin
    threethRef <- SignallingRef[IO].of(Threeth.init(defaultCode).parse.eval).toResource
    clicker = (label: String, updater: Threeth.State => Threeth.State, style: String) =>
      button(
        label,
        styleAttr := style,
        onClick --> (_.foreach { _ => threethRef.update(updater) })
      )
    body <-
      div(
        textArea.withSelf(self =>
          (
            defaultCode,
            onInput --> (_.foreach { _ =>
              for
                code <- self.value.get
                _ <- threethRef.update(_.copy(code = code))
              yield ()
            })
          )),
        div(
          clicker("rerun", _.reset.parse.eval, "background-color: #dc3431; color: white;"),
          clicker("parse", _.parse, ""),
          clicker("eval", _.eval, ""),
          clicker("reset", _.reset, "")
        ),
        threethRef.map { threeth =>
          div(
            styleAttr := "display: flex; flex-direction: column",
            h3(s"Stack top: ${threeth.stack.headOption.getOrElse("Stack is empty")}"),
            code(s"tokens: ${threeth.tokens.toString}"),
            code(s"stack: ${threeth.stack.toString}"),
            code(s"error: ${threeth.error.toString}"),
            hr(""),
            code(
              pre("""synatx:
                    |  x                 -- push x onto the stack
                    |  verb              -- call verb
                    |  : name words... ; -- compile a verb named `name` consisting of `words...`
                    |builtins:
                    |  dup   ( a   -- a a )    -- duplictes the topmost item
                    |  join  ( a b -- ab )     -- concateneates two topmost items
                    |  swap  ( a b -- b a )    -- swaps two topmost elements
                    |  space (     -- space )  -- pushes a literal space character
                    |macros:
                    |  repeat ( verb n -- verb...)   -- repeats `verb` n times""".stripMargin))
          )
        }
      )
  yield body
}

object Threeth {
  opaque type Stack = List[String]

  extension (stack: Stack) {
    def push(x: String) = stack.appended(x)
    def headOption = stack.headOption

    def pop: Either[String, (String, Stack)] = stack match
      case head :: tail => Right(head, tail)
      case Nil => Left("Not enough elements on the stack")

    def pop2: Either[String, (String, String, Stack)] = stack match
      case x :: y :: tail => Right(x, y, tail)
      case _ => Left("Not enough elements on the stack")
  }

  private type Verb = Stack => Either[String, Stack]
  private type Token = String | Verb
  private type Macro = List[Token] => Either[String, List[Token]]

  private val builtins = Map[String, Verb](
    "dup" -> (_.pop.map { (x, tail) => x +: x +: tail }),
    "join" -> (_.pop2.map { (x, y, tail) => tail.prepended(y ++ x) }),
    "swap" -> (_.pop2.map { (x, y, tail) => y +: x +: tail }),
    "space" -> (stack => Right(stack.prepended(" ")))
  )

  private val macros = Map[String, Macro](
    "repeat" -> (
      _.reverse match
        case (n: String) :: (name: Verb) :: tail =>
          n.toIntOption.toRight("Not a number").map(List.fill(_)(name) ++ tail).map(_.reverse)
        case s =>
          Left(s"Bad signature: ${s}")
    )
  )

  def init(code: String) = State(builtins, List.empty, code, List.empty, None)

  case class State(
      declarations: Map[String, Verb],
      stack: Stack,
      code: String,
      tokens: List[Token],
      error: Option[String]
  ) {
    def reset = State(builtins, List.empty, this.code, List.empty, None)

    /**
     * Parses source code into a token list
     */
    def parse: State = {
      def parseSimple(
          previous: List[Token],
          str: String,
          declarations: Map[String, Verb]): Either[String, List[Token]] =
        declarations.get(str) match
          case Some(verb) => Right(previous.appended(verb))
          case None =>
            macros.get(str) match
              case Some(m) => m(previous)
              case None => Right(previous.appended(str))

      // Compiles a list of tokens into a [[Verb]]
      def compile(tokens: List[Token], declarations: Map[String, Verb]): Verb =
        (stack: Stack) =>
          tokens.foldLeft(Right(stack): Either[String, Stack]) { (acc, token) =>
            token match
              case word: String => acc.map(_.prepended(word))
              case verb: Verb => acc.flatMap(verb)
          }

      // Parses a verb declaration
      def parseDeclaration(
          acc: List[Token],
          rhs: List[String],
          declarations: Map[String, Verb]
      ): Either[String, (Verb, List[String])] = rhs match
        case ";" :: tail => Right((compile(acc, declarations), tail))
        case ":" :: tail => Left("New declaration found before the last one ended")
        case head :: tail =>
          parseSimple(acc, head, declarations).flatMap(parseDeclaration(_, tail, declarations))
        case Nil => Left("End of declaration marker not found")

      // Main parsing loop
      def go(
          lhs: List[Token],
          rhs: List[String],
          declarations: Map[String, Verb]
      ): Either[String, (List[Token], Map[String, Verb])] = rhs match
        case ":" :: name :: tail =>
          parseDeclaration(List.empty, tail, declarations).flatMap { (verb, rest) =>
            go(lhs, rest, declarations + (name -> verb))
          }
        case head :: tail =>
          parseSimple(lhs, head, declarations).flatMap { tokens =>
            go(tokens, tail, declarations)
          }
        case Nil => Right((lhs, declarations))

      // separated words
      val strings = Stream(this.code)
        .through(fs2.text.lines)
        .filterNot(_.startsWith("#"))
        .flatMap { line => Stream.emits(line.split("\\s+")).filterNot(_.isBlank).map(_.trim) }
        .compile
        .toList

      // run the parser and update the state accordingly
      go(List.empty, strings, this.declarations) match
        case Left(err) => this.copy(error = Some(err))
        case Right((tokens, decls)) => this.copy(tokens = tokens, declarations = decls)
    }

    /**
     * Evaluates tokens
     */
    def eval: State = this.tokens.foldLeft(this) { (acc, token) =>
      token match
        case word: String =>
          acc.copy(stack = acc.stack.prepended(word))
        case verb: Verb =>
          verb(acc.stack) match
            case Left(err) => acc.copy(error = Some(err))
            case Right(stack) => acc.copy(stack = stack)
    }
  }
}

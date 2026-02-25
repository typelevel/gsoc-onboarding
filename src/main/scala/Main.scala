import cats.implicits.*
import cats.effect.*

import contributors.*

import calico.IOWebApp
import calico.html.io.{*, given}
import calico.syntax.*
import calico.unsafe.given

import fs2.concurrent.*
import fs2.dom.HtmlElement

import org.http4s.dom.FetchClientBuilder

val participants: List[(String, Resource[IO, HtmlElement[IO]])] = List(
  "antoniojimenez" -> AntonioJimenez.component,
  "armanbilge" -> ArmanBilge.component,
  "djspiewak" -> DanielSpiewak.component,
  "valencik" -> AndrewValencik.component
)

object Main extends IOWebApp:
  def render: Resource[IO, HtmlElement[IO]] =
    val handles = participants.map(_._1)
    val client = FetchClientBuilder[IO].create

    SignallingRef[IO].of[Option[String]](None).toResource.flatMap { validationMsg =>

      val validate: IO[Unit] =
        Validation.validate(handles, client).flatMap(validationMsg.set)

      Resource.eval(validate) *>
        div(
          h1("Calico GSoC Contributors"),
          div(
            styleAttr <-- validationMsg.map {
              case None =>
                "color: green; padding: 8px; border: 1px solid green; margin-bottom: 12px"
              case Some(_) =>
                "color: red; padding: 8px; border: 1px solid red; margin-bottom: 12px"
            },
            validationMsg.map(_.getOrElse("Order is correct!"))
          ),
          button(
            onClick --> (_.foreach(_ => validate)),
            "Verify order"
          ),
          ul(participants.map { (handle, _) => li(a(href := s"#$handle", s"@$handle")) }),
          participants.map { (handle, component) => div(idAttr := handle, component) }
        )
    }

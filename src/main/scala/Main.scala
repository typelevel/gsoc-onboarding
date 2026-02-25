import cats.implicits.*
import cats.effect.*

import calico.IOWebApp
import calico.html.io.{*, given}
import calico.syntax.*
import calico.unsafe.given

import fs2.concurrent.*
import fs2.dom.HtmlElement

import io.circe.*
import io.circe.syntax.*

import org.http4s.*
import org.http4s.circe.*
import org.http4s.dom.*
import org.http4s.implicits.uri
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

import org.typelevel.ci.CIStringSyntax

val contributors: List[(String, Resource[IO, HtmlElement[IO]])] = List(
  "antoniojimenez" -> AntonioJimenez.component
)

final case class OutOfOrder(shouldBeBefore: String, butIsAfter: String) derives Decoder
final case class ValidationResponse(valid: Boolean, outOfOrder: Option[OutOfOrder])
    derives Decoder

object Main extends IOWebApp:
  val validationRequest =
    val body = contributors.map(_._1).asJson.noSpaces

    Request[IO](Method.POST, uri"https://cloudflare-typelevel.org")
      .withEntity(body)
      .putHeaders(Header.Raw(ci"Content-Type", "application/json"))

  def render: Resource[IO, HtmlElement[IO]] =
    val handles = contributors.map(_._1)
    val client = FetchClientBuilder[IO].create

    SignallingRef[IO].of[Option[String]](None).toResource.flatMap { validationMsg =>

      val validate: IO[Unit] =
        client
          .expect[ValidationResponse](validationRequest)
          .attempt
          .flatMap:
            case Right(ValidationResponse(true, _)) =>
              validationMsg.set(None)

            case Right(ValidationResponse(_, Some(OutOfOrder(before, after)))) =>
              validationMsg.set(Some(s"$before should be before $after"))

            case Right(_) =>
              validationMsg.set(Some("Order is invalid"))

            case Left(_) =>
              validationMsg.set(Some("Could not reach validation API"))

      Resource.eval(validate) *>
        div(
          h1("Calico GSoC Contributors"),
          div(
            styleAttr <-- validationMsg.map {
              case None => "display: none"
              case Some(_) =>
                "color: red; padding: 8px; border: 1px solid red; margin-bottom: 12px"
            },
            validationMsg.map(_.getOrElse(""))
          ),
          button(
            onClick --> (_.foreach(_ => validate)),
            "Verify order"
          ),
          ul(
            contributors.map { (handle, _) => li(a(href := s"#$handle", s"@$handle")) }
          ),
          contributors.map { (handle, component) => div(idAttr := handle, component) }
        )
    }

package gsoc

import cats.effect.*

import fs2.concurrent.*

import io.circe.*
import io.circe.syntax.*

import org.http4s.*
import org.http4s.syntax.all.*
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dom.*

final case class OutOfOrder(shouldBeBefore: String, butIsAfter: String) derives Decoder
final case class ValidationResponse(valid: Boolean, outOfOrder: Option[OutOfOrder])
    derives Decoder

object Validation:
  val apiUrl = uri"https://gsoc.typelevel.org/onboard"

  def request(handles: List[String]): Request[IO] =
    Request[IO](Method.POST, apiUrl).withEntity(handles.asJson)

  def validate(handles: List[String], client: Client[IO]): IO[Option[String]] =
    client
      .expect[ValidationResponse](request(handles))
      .attempt
      .map:
        case Right(ValidationResponse(true, _)) => None
        case Right(ValidationResponse(_, Some(OutOfOrder(before, after)))) =>
          Some(s"$before should be before $after")
        case Right(_) => Some("Order is invalid")
        case Left(_) => Some("Could not reach validation API")

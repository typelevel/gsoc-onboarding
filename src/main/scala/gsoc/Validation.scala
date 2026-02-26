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

final case class OutOfOrder(shouldBeBefore: String, butIsAfter: String) derives Decoder:
  override def toString = s"$shouldBeBefore should be before $butIsAfter"
final case class ValidationResponse(valid: Boolean, outOfOrder: Option[OutOfOrder])
    derives Decoder:
  override def toString =
    if valid then "Order is correct!" else outOfOrder.fold("Order is incorrect")(_.toString)

object Validation:
  val apiUrl = uri"https://gsoc.typelevel.org/onboard"

  def request(handles: List[String]): Request[IO] =
    Request[IO](Method.POST, apiUrl).withEntity(handles.asJson)

  def validate(
      handles: List[String],
      client: Client[IO]): IO[Either[String, ValidationResponse]] =
    client
      .expect[ValidationResponse](request(handles))
      .map(Right(_))
      .handleError(_ => Left("Could not reach validation API."))

package gsoc

import cats.effect.*
import fs2.dom.*

case class Contributor(handle: String, component: Resource[IO, HtmlElement[IO]])

object Contributor:
  def apply(handle: String)(component: Resource[IO, HtmlElement[IO]])(using DummyImplicit): Contributor =
    Contributor(handle, component)

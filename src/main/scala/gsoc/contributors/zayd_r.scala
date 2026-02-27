package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*


val zayd_r: Contributor = Contributor("zayd_r"):
  val interserItems = List(
    "- A faster immutable list datatype",
    "- Native I/O backend for FS2 JVM",
    "- Serverless integrations for Feral (but no experience in serverless)"
  )

  SignallingRef[IO].of(false).toResource.flatMap { active =>
    div(
      cls := "contributor-card",
      h3("Zayd-R"),
      p( "I agree to follow the Typelevel Code of Conduct and the GSoC AI policy."),
      p("Interested in: Cats, FS2, and Typelevel ecosystem."),
      h2("Ideas Iam Interested in:"),
      li(interserItems.map(li(_)))
    )
  }


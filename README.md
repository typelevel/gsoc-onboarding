# Typelevel GSoC 2026 Onboarding

Welcome! This project is part of the onboarding process for [Google Summer of Code 2026](https://summerofcode.withgoogle.com/) applicants interested in contributing to [Typelevel](https://typelevel.org/gsoc/).

The app is built with [Calico](https://www.armanbilge.com/calico/), a purely functional, reactive UI framework for Scala.js powered by [Cats Effect](https://typelevel.org/cats-effect/) and [FS2](https://fs2.io/).

## What you need to do

1. **Set up your environment.** Install [Java 21 or newer](https://adoptium.net/installation) and [sbt](https://www.scala-sbt.org/download). We also recommend trying the [Metals](https://scalameta.org/metals/) IDE.

2. **Run the app locally.** Compile and start the development server (see [Running locally](#running-locally) below) to make sure everything works before making changes.

3. **Create your own component** in `src/main/scala/gsoc/contributors/` as a new file named after your GitHub handle (e.g., `yourgithubhandle.scala`).

4. **Be creative!** Look at the existing contributors for inspiration — some have made interactive buttons, others reveal hidden info. But you are not limited to buttons. Build whatever you want: a mini game, an animation, a quiz, some generative art... surprise us!

    Your component must also include a statement that **you agree to follow the [Typelevel Code of Conduct][coc] and the [Typelevel GSoC AI Policy][ai]**. Please take a minute to review both documents.

5. **Register your component** by adding it to the `allContributors` list in [all.scala](src/main/scala/gsoc/contributors/all.scala).

    **The order matters!** The list of contributors must follow a specific ordering that is validated by a backend API. You won't know the correct position in advance — use the "Check order" button in the app to test your placement. If the order is wrong, the API will tell you which two entries are out of place. Keep trying different positions until the validation passes!

6. **Test your component** with the development server by clicking on your handle in the list and making sure that it renders and works correctly. Also use the "Check order" button to confirm that your handle is in the correct position in the list.

7. **Open a PR** to this repository adding your component. Congratulations on your first contribution!

## Creating your component

Your file should follow this structure:

```scala
package gsoc
package contributors

import cats.effect.*
import fs2.concurrent.*
import fs2.dom.HtmlElement
import calico.html.io.{*, given}
import calico.syntax.*

val yourgithubhandle: Contributor = Contributor("yourgithubhandle"):
  // Build your component here!
  // It must return a Resource[IO, HtmlElement[IO]]
  div(
    p("Hello, I'm @yourgithubhandle on GitHub. I agree to follow the Typelevel CoC and GSoC AI policy."),
    // ... get creative!
  )
```

A `Contributor` is simply a GitHub handle paired with a Calico component:

```scala
case class Contributor(handle: String, component: Resource[IO, HtmlElement[IO]])
```

For stateful, interactive components you can use `SignallingRef`:

```scala
val yourgithubhandle: Contributor = Contributor("yourgithubhandle"):
  SignallingRef[IO].of(initialState).toResource.flatMap { state =>
    div(
      // Use `state` to build reactive UI
    )
  }
```

## Running locally

Start an sbt shell, then run `serve` to start the local development server and `~fastLinkJS` to compile and watch for changes:

```
$ sbt
sbt:gsoc-onboarding> serve
sbt:gsoc-onboarding> ~fastLinkJS
```
You can run both commands inside the same sbt shell.

- `serve` starts the development server.
- `~fastLinkJS` enables continuous compilation and automatically rebuilds on file changes.

Open the URL printed by the `serve` task in your browser to preview the webapp.

## Useful resources

- [Typelevel Code of Conduct][coc]
- [Typelevel GSoC AI Policy][ai]
- [Calico documentation](https://www.armanbilge.com/calico/)
- [Cats Effect documentation](https://typelevel.org/cats-effect/)
- [Scala.js documentation](https://www.scala-js.org/doc/)

Good luck, and have fun! If you have any questions, please use the [Discussions](https://github.com/typelevel/gsoc-onboarding/discussions/categories/q-a) forum or ask in the [#summer-of-code][invite] channel on our [Discord server](invite). We also encourage you to join one of our [events](https://typelevel.org/gsoc/events).

[coc]: https://typelevel.org/code-of-conduct/
[ai]: https://typelevel.org/gsoc/ai.html
[invite]: https://discord.gg/382Z3w8QTj

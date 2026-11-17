# Sidegloss

**Tap a word. See what it means here.**

Sidegloss is a reading companion for people who read English as a second language. Tap any word or expression in
a text and the meaning it has in that sentence appears beside it, in English or French, written for your level.
Explanations are prepared ahead of time, and the choice of which one to show is made by Kotlin code on the
device, so a tap is instant and works offline.

Built with Kotlin Multiplatform and Compose Multiplatform for Android, desktop, the browser and iOS, with a Ktor
server that shares the same engine. Created for the Kotlin Multiplatform Contest.

## Status

This is the foundation: every module, one shared screen, the first tested piece of the engine, lint, tests and
CI. The features are specified and planned, not built yet.

Verified on 16 November 2026, on Linux:

| Target | What was run | Result |
|---|---|---|
| Android | Debug build installed and launched on a Pixel 7 emulator | The screen renders |
| Web | Development server opened in a browser, and the production bundle built | The page loads and the Compose canvas fills the window with no console errors |
| Desktop | Built, and the root screen rendered in a headless UI test | Passes. The window itself was not opened. |
| Server | Jar started, `/` and `/health` requested | Both answer |
| iOS | Targets declared. Compiling and testing them needs macOS, which the CI workflow provides. | Not run yet |

## Modules

| Module | What it is | Targets |
|---|---|---|
| `shared` | The engine and every stored or transmitted model. Pure Kotlin, no UI. | Android, JVM, iOS, Wasm |
| `composeApp` | Everything the client apps share: screens, state, storage. | Android, JVM, iOS, Wasm |
| `androidApp` | Android launcher | Android |
| `desktopApp` | Desktop launcher | JVM |
| `webApp` | Browser launcher | Wasm |
| `server` | Ktor server. Uses `shared`. | JVM |

## Requirements

- JDK 17 or newer
- An Android SDK for the Android app. Gradle installs the platform it needs.
- A recent Chrome or Firefox for the web app

For Android, point Gradle at the SDK with a `local.properties` file in the repository root:

```properties
sdk.dir=/path/to/Android/Sdk
```

## Run

Desktop:

```bash
./gradlew :desktopApp:run
```

Web, on http://localhost:8080:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Android, on a connected device or a running emulator:

```bash
./gradlew :androidApp:installDebug
```

Server, on http://localhost:8081:

```bash
./gradlew :server:run
```

## Check

```bash
./gradlew spotlessCheck
```

```bash
./gradlew :shared:jvmTest :composeApp:jvmTest :server:test
```

```bash
./gradlew sharedCodeReport
```

## License

[MIT](LICENSE)

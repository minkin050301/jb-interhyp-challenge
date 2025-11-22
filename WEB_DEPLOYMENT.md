### Web deployment (JS/WASM) for DreamBuilder

This project is already configured to build and run as a web app via Kotlin/JS (and optional Wasm). The setup uses Compose Multiplatform for UI and Webpack for bundling.

What’s included
- js browser target with executable binary
- stable bundle name composeApp.js so index.html can reference it
- webMain resources (index.html, styles.css)
- Koin DI initialization for the web entry point
- Convenience Gradle task webDist to assemble a deployable folder

Run locally (development)
- Option A (JS dev server with HMR):
  ./gradlew :composeApp:jsBrowserDevelopmentRun
  The dev server URL will be printed in the Gradle output (typically http://localhost:8080).

- Option B (Wasm preview):
  ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
  Note: Wasm is optional; JS is sufficient for deployment.

Production build
- JS production bundle:
  ./gradlew :composeApp:jsBrowserDistribution
  Output directory: composeApp/build/dist/js/productionExecutable

- Convenience task (copies to a stable path):
  ./gradlew :composeApp:webDist
  Output directory: composeApp/build/dist/web

Deploy to static hosting
Upload the contents of composeApp/build/dist/web (or dist/js/productionExecutable) to any static host, e.g.:
- GitHub Pages
- Netlify
- Vercel (static)
- Firebase Hosting
- S3 + CloudFront

Notes
- The entry HTML is located at composeApp/src/webMain/resources/index.html and references composeApp.js which is produced by the production build due to the configured Webpack outputFileName.
- If you use a SPA-like routing later, ensure your static host rewrites unknown paths to index.html.

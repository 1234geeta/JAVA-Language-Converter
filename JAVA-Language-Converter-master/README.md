Language Converter
==================

This project is a small Swing application that can translate text and PDFs using the Gemini API and produce translated PDFs with correct Unicode fonts.

Quick start (recommended — Maven)
---------------------------------

1. Install Java 11+ and Maven.
2. Build the project and create an executable fat JAR:

   ```powershell
   mvn clean package
   ```

   The assembly plugin will create a JAR under `target/` named like `language-converter-1.0-SNAPSHOT-jar-with-dependencies.jar`.

4. Run the application:

   ```powershell
   java -jar target\language-converter-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

Notes
-----

- The app will try to use a bundled NotoSans font (if present under `src/main/resources/fonts/`) or system fonts as fallbacks.
- Make sure the target machine has fonts that cover the languages you want to render (e.g., Mangal for Hindi/Devanagari, SimHei for Chinese, etc.).
- If you prefer not to use Maven, you can run the app directly by compiling sources and providing dependencies, but Maven is the easiest way to create a runnable artifact.

Cleaning and minimalization
---------------------------

I removed hardcoded API keys from the runtime files; the app reads `GEMINI_API_KEY` from environment variables or `-Dgemini.api.key` system property. I also removed unused comments and kept code paths minimal so sharing is straightforward.

If you want, I can also:
- Add a small installer script for Windows to set the environment and launch the app.
- Add a sample `fonts/` folder with Noto fonts bundled (large, so not added by default).

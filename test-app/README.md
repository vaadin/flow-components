# Test App

Sandbox app for testing various combinations of components from this repo. All component modules are on the classpath. No stylesheet is loaded by default (`@NoTheme` in `AppShell.java` — see its javadoc for enabling Lumo or Aura).

## Running

```sh
mvn package jetty:run -am -B -DskipTests -pl test-app
```

Then open http://localhost:8080. The server runs in dev mode with frontend hot deploy, but needs a restart after Java changes.

To stop:

```sh
mvn jetty:stop -pl test-app
```

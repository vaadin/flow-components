# Reproduction App

A sandbox application for reproducing issues with various combinations of components. It has the same setup as the component integration test modules, but it is not part of the normal build, CI, or releases, and it is never published to Maven.

The module is only included in the Maven reactor when the `repro-app` property is set (`-Drepro-app`).

## Usage

1. Add the component dependencies needed for the reproduction to `pom.xml`, for example:

   ```xml
   <dependency>
       <groupId>com.vaadin</groupId>
       <artifactId>vaadin-grid-flow</artifactId>
       <version>${project.version}</version>
   </dependency>
   ```

2. Replace the content of `ReproView` with the reproduction, or add more views with their own `@Route`.

3. Start the server and open http://localhost:8080:

   ```sh
   mvn package jetty:run -Drepro-app -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl repro-app
   ```

Changes made for a reproduction are not meant to be committed — reset the module when done.

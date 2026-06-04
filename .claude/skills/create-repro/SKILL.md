---
name: create-repro
description: Create a reproduction example
argument-hint: <description or issue link>
allowed-tools: Read, Edit, Write, Bash(git:*), Bash(gh:*), Bash(mvn:*)
---

Read the issue description in $0, extract the steps to reproduce it, and create one or more Flow views in `repro-app` that can be used for manual testing.

## Steps

1. If the description is a link to a Github issue, fetch the issue description with `gh issue view`.

2. Identify components that are involved in the issue and add them to the dependencies in `repro-app/pom.xml`:

   ```xml
   <dependency>
       <groupId>com.vaadin</groupId>
       <artifactId>vaadin-{component}-flow</artifactId>
       <version>${project.version}</version>
   </dependency>
   ```

3. Determine what setup and steps are required to reproduce the issue.

4. Create a minimal snippet containing one or more Flow views and add it to `repro-app`. Replace the content of `ReproView` (route `""`) with the reproduction, or add more views with their own `@Route` in the same package.

   Do not add a class-level Javadoc describing the issue or listing the scenarios. Only add short inline comments next to the relevant code explaining how to trigger the issue. The full steps belong in the chat response (step 5), not in the code.

5. Provide the steps to reproduce the issue, including the command to start the app:

   ```sh
   mvn package jetty:run -Drepro-app -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl repro-app
   ```

   Then open http://localhost:8080.

## Notes

- The `repro-app` module only joins the Maven reactor when the `-Drepro-app` property is set.
- Changes made for a reproduction are not meant to be committed.

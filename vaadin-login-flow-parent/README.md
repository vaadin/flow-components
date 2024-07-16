# Vaadin Login for Flow

Vaadin Login for Flow is a UI component add-on for Vaadin.

### Installing
Add Login to your project
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-login-flow</artifactId>
    <version>${vaadin.login.version}</version>
  </dependency>
</dependencies>
```

### Using Vaadin Login

[<img src="https://raw.githubusercontent.com/vaadin/vaadin-login/master/screenshot.png" width="700" alt="Screenshot of vaadin-login">](https://vaadin.com/components/vaadin-login)

#### Basic use
```java
LoginOverlay component = new LoginOverlay();
```

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-login-flow.git
```

To build and install the project into the local repository run

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```

To compile and run demos locally execute

```
mvn compile
mvn -pl vaadin-login-flow-demo -Pwar jetty:run
```

### License

This program is available under Vaadin Commercial License and Service Terms.


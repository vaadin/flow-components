# AI components for Vaadin Flow

This project provides interfaces for building AI-powered applications with LLM integration
and [Vaadin Flow](https://github.com/vaadin/flow).

## Using the components in a Flow application

To use the components in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-ai-core-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

The AI controllers (`ChartAIController`, `FormAIController`, `GridAIController`)
are part of the commercial `vaadin-ai-extensions-flow` module:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-ai-extensions-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## License

`vaadin-ai-core-flow` is distributed under Apache License 2.0.

`vaadin-ai-extensions-flow` is distributed under
[Vaadin Commercial License and Service Terms](https://vaadin.com/commercial-license-and-service-terms).

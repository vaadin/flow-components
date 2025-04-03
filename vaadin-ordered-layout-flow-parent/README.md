# Layout components for Vaadin Flow

This project contains Component wrapper implementations of Vaadin ordered layout web components for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).
The following web components are covered:
[`<vaadin-horizontal-layout>`](https://github.com/vaadin/web-components/tree/main/packages/horizontal-layout)
[`<vaadin-vertical-layout>`](https://github.com/vaadin/web-components/tree/main/packages/vertical-layout)
[`<vaadin-scroller>`](https://github.com/vaadin/web-components/tree/main/packages/scroller)

This project also contains `FlexLayout`, which is a server side layout component that implements CSS Flexbox.  

## Using the component in a Flow application

To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-ordered-layout-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

## License

Apache License 2.0

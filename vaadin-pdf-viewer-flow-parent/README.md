# PDF Viewer component for Vaadin Flow

This project is the Component wrapper implementation of [`<vaadin-pdf-viewer>`](https://github.com/vaadin/web-components/tree/main/packages/pdf-viewer)
element for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

PDF Viewer is experimental. Its API may change in minor versions. To use it, enable the
`com.vaadin.experimental.pdfViewerComponent` feature flag.

## Using the component in a Flow application

To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-pdf-viewer-flow</artifactId>
    <version>${component.version}</version>
</dependency>
```

Enable the feature flag by adding a `src/main/resources/vaadin-featureflags.properties` file
with the following content:
```
com.vaadin.experimental.pdfViewerComponent=true
```

Show a document served by the application:
```java
PdfViewer viewer = new PdfViewer(
        DownloadHandler.forClassResource(getClass(), "report.pdf"));
viewer.setHeight("600px");
```

## License

This component is distributed under [Vaadin Commercial License and Service Terms](https://vaadin.com/commercial-license-and-service-terms).

To purchase a license, visit http://vaadin.com/pricing

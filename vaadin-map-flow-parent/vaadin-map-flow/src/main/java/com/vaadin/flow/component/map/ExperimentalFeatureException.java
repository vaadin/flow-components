package com.vaadin.flow.component.map;

public class ExperimentalFeatureException extends RuntimeException {
    public ExperimentalFeatureException() {
        super("The map component is currently an experimental feature and needs to be explicitly enabled. The component can be enabled using the Vaadin dev-mode Gizmo, in the experimental features tab, or by adding a `src/main/resources/vaadin-featureflags.properties` file with the following content: `com.vaadin.experimental.mapComponent=true`");
    }
}

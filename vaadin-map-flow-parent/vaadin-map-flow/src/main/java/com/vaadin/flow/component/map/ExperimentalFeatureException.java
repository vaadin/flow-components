package com.vaadin.flow.component.map;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

public class ExperimentalFeatureException extends RuntimeException {
    public ExperimentalFeatureException() {
        super("The map component is currently an experimental feature and needs to be explicitly enabled. The component can be enabled using the Vaadin dev-mode Gizmo, in the experimental features tab, or by adding a `src/main/resources/vaadin-featureflags.properties` file with the following content: `com.vaadin.experimental.mapComponent=true`");
    }
}

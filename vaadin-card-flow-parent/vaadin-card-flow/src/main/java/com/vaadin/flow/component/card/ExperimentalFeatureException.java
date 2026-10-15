/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.card;

/**
 * An exception which is thrown when somebody attempts to use the {@link Card}
 * component without activating the associated feature flag first.
 *
 * @author Vaadin Ltd
 */
public class ExperimentalFeatureException extends RuntimeException {
    public ExperimentalFeatureException() {
        super("The Card component is currently an experimental feature and needs to be "
                + "explicitly enabled. The component can be enabled using Copilot, in the "
                + "experimental features tab, or by adding a "
                + "`src/main/resources/vaadin-featureflags.properties` file with the following content: "
                + "`com.vaadin.experimental.cardComponent=true`");
    }
}

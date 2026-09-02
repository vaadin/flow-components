/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.Component;
import com.vaadin.tests.AbstractConnectorInitializationTest;

class MapConnectorInitializationTest
        extends AbstractConnectorInitializationTest {
    @Override
    protected Component createTestComponent() {
        return new Map();
    }

    @Override
    protected String getConnectorInitExpression() {
        return "window.Vaadin.Flow.mapConnector.init(this)";
    }
}

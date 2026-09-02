/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.JsFunction;

/**
 * An abstract class that provides a test verifying that a component initializes
 * its client-side connector before any call that uses it, which requires
 * registering the initializer in the component's constructor.
 */
public abstract class AbstractConnectorInitializationTest {
    private static final String CONNECTOR_CALL = "this.$connector.scheduledWhileDetached()";

    @RegisterExtension
    protected MockUIExtension ui = new MockUIExtension();

    /**
     * Creates the component under test.
     *
     * @return the component under test
     */
    protected abstract Component createTestComponent();

    /**
     * Returns the JavaScript expression that the component passes to
     * {@link com.vaadin.flow.dom.Element#addJsInitializer(String, Object...)}
     * to initialize its connector.
     *
     * @return the connector initializer expression
     */
    protected abstract String getConnectorInitExpression();

    @Test
    void attach_connectorInitializedBeforeConnectorCalls() {
        Component component = createTestComponent();
        // A connector call from application code before the component is
        // added to a layout, for example grid.scrollToIndex(0) before add(grid)
        component.getElement().executeJs(CONNECTOR_CALL);
        ui.add(component);

        List<String> js = ui.dumpPendingJavaScriptInvocations().stream()
                // Initializer JavaScript is a JsFunction parameter of a
                // generic wrapper expression, not the invocation's expression
                .map(pending -> pending.getInvocation().getParameters().stream()
                        .filter(JsFunction.class::isInstance)
                        .map(parameter -> ((JsFunction) parameter).getBody())
                        .findFirst()
                        .orElseGet(pending.getInvocation()::getExpression))
                .toList();

        int initIndex = js.indexOf(getConnectorInitExpression());
        Assertions.assertTrue(initIndex >= 0,
                "Expected the component to register an initializer running "
                        + getConnectorInitExpression());
        Assertions.assertEquals(List.of(),
                js.subList(0, initIndex).stream().filter(
                        expression -> expression.contains("$connector"))
                        .toList(),
                "These calls reach the connector before it is initialized");
    }
}

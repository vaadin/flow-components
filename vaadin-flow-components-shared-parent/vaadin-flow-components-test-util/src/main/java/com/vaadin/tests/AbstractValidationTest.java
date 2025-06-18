/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.tests;

public abstract class AbstractValidationTest
        extends com.vaadin.flow.testutil.AbstractValidationTest {

    protected int getDeploymentPort() {
        return 8080;
    }
}

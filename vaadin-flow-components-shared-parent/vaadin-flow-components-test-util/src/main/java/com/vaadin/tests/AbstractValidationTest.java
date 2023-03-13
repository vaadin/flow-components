package com.vaadin.tests;

public abstract class AbstractValidationTest
        extends com.vaadin.flow.testutil.AbstractValidationTest {

    protected int getDeploymentPort() {
        return 8080;
    }
}

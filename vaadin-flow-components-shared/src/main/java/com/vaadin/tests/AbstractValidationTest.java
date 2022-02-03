package com.vaadin.tests;

import org.junit.BeforeClass;

public abstract class AbstractValidationTest
        extends com.vaadin.flow.testutil.AbstractValidationTest {

    @BeforeClass
    public static void setupClass() {
        ParallelTest.setupClass();
    }

    protected int getDeploymentPort() {
        return 8080;
    }
}

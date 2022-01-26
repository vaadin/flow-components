package com.vaadin.tests;

import org.junit.BeforeClass;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    @BeforeClass
    public static void setupClass() {
        ParallelTest.setupClass();
    }

    protected int getDeploymentPort() {
        return 8080;
    }

}

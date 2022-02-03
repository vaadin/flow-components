package com.vaadin.tests;

import org.junit.BeforeClass;

public abstract class ComponentDemoTest
        extends com.vaadin.flow.demo.ComponentDemoTest {

    @BeforeClass
    public static void setupClass() {
        ParallelTest.setupClass();
    }

    @Override
    protected int getDeploymentPort() {
        return 8080;
    }
}

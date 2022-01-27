package com.vaadin.tests;

import org.junit.BeforeClass;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class TabbedComponentDemoTest
        extends com.vaadin.flow.demo.TabbedComponentDemoTest {

    @BeforeClass
    public static void setupClass() {
        ParallelTest.setupClass();
    }

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected <T> T waitUntil(ExpectedCondition<T> condition) {
        return super.waitUntil(condition, 120);
    }
}

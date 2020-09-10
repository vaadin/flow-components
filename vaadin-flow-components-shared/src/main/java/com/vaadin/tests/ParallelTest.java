package com.vaadin.tests;

import org.junit.AfterClass;

public abstract class ParallelTest extends com.vaadin.testbench.parallel.ParallelTest {

    private static SharedBrowser browser = new SharedBrowser();

    @Override
    public void setup() throws Exception {
        browser.setup(super::setup, this::getDriver, screenshotOnFailure)
            .ifPresent(this::setDriver);
    }

    protected int getDeploymentPort() {
        return 8080;
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }

}

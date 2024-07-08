/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.tests;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class ComponentDemoTest
        extends com.vaadin.flow.demo.ComponentDemoTest {

    private static SharedBrowser browser = new SharedBrowser();

    @BeforeClass
    public static void setupClass() {
        ParallelTest.setupClass();
    }

    @Override
    public void setup() throws Exception {
        browser.setup(super::setup, this::setDriver, this::getDriver,
                screenshotOnFailure);
    }

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        return browser.getGridBrowsers().orElse(super.getHubBrowsersToTest());
    }

    @Override
    protected int getDeploymentPort() {
        return 8080;
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }
}

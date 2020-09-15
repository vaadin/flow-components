package com.vaadin.tests;

import org.junit.AfterClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;

public abstract class ComponentDemoTest extends com.vaadin.flow.demo.ComponentDemoTest {

    private static SharedBrowser browser = new SharedBrowser();

    @Override
    public void setup() throws Exception {
        browser.setup(super::setup, this::getDriver, screenshotOnFailure)
            .ifPresent(this::setDriver);
    }

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        return browser.getGridBrowsers().orElse(super.getHubBrowsersToTest());
    }

    protected int getDeploymentPort() {
        return 8080;
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }
}

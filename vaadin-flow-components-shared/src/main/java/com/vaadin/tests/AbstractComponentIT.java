package com.vaadin.tests;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

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

    protected int getDeploymentPort() {
        return 8080;
    }

    public TestBenchElement getInShadowRoot(TestBenchElement tbElement,
            String id) {
        return tbElement.$("*").id(id);
    }

    public WebElement getInShadowRoot(TestBenchElement tbElement,
            String attributeName, String attributeValue) {
        return tbElement.$("div").attribute(attributeName, attributeValue)
                .get(0);
    }

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        return browser.getGridBrowsers().orElse(super.getHubBrowsersToTest());
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }
}

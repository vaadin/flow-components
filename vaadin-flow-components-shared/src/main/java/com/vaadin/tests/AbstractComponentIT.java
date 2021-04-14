package com.vaadin.tests;

import java.util.List;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class AbstractComponentIT
    extends com.vaadin.flow.testutil.AbstractComponentIT {

    private static SharedBrowser browser = new SharedBrowser();

    @BeforeClass
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @Override
    public void setup() throws Exception {
        browser.setup(super::setup, this::setDriver, this::getDriver, screenshotOnFailure);
    }

    protected int getDeploymentPort() {
        return 8080;
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

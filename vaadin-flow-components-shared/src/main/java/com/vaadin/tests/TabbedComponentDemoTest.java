package com.vaadin.tests;

import java.io.File;
import java.util.List;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;

public abstract class TabbedComponentDemoTest
        extends com.vaadin.flow.demo.TabbedComponentDemoTest {

    private static SharedBrowser browser = new SharedBrowser();

    @BeforeClass
    public static void setupClass() {
        String driver = System.getProperty("webdriver.chrome.driver");
        if (driver == null || !new File(driver).exists()) {
            WebDriverManager.chromedriver().setup();
        }
    }

    @Override
    public void setup() throws Exception {
        browser.setup(super::setup, this::setDriver, this::getDriver,
                screenshotOnFailure);
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

    @Override
    protected <T> T waitUntil(ExpectedCondition<T> condition) {
        return super.waitUntil(condition, 120);
    }
}

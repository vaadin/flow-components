package com.vaadin.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.List;

public abstract class AbstractValidationTest extends
    com.vaadin.flow.testutil.AbstractValidationTest {

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

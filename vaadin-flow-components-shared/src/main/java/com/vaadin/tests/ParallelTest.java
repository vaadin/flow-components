package com.vaadin.tests;

import java.io.File;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class ParallelTest
        extends com.vaadin.testbench.parallel.ParallelTest {

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

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }

}

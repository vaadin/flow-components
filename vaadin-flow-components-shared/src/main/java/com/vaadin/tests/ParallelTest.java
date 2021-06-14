package com.vaadin.tests;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class ParallelTest
        extends com.vaadin.testbench.parallel.ParallelTest {

    private static SharedBrowser browser = new SharedBrowser();

    @BeforeClass
    public static void setupClass() {
        String sauceKey = System.getProperty("sauce.sauceAccessKey");
        String hubHost = System
                .getProperty("com.vaadin.testbench.Parameters.hubHostname");
        if ((sauceKey == null || sauceKey.isEmpty())
                && (hubHost == null || hubHost.isEmpty())) {
            String driver = System.getProperty("webdriver.chrome.driver");
            if (driver == null || !new File(driver).exists()) {
                WebDriverManager.chromedriver().setup();
            }
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

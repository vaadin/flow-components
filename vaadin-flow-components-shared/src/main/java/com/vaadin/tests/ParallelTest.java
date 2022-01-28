package com.vaadin.tests;

import java.io.File;

import org.junit.BeforeClass;

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class ParallelTest
        extends com.vaadin.testbench.parallel.ParallelTest {

    @BeforeClass
    public static void setupClass() {
        System.gc();
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

    protected int getDeploymentPort() {
        return 8080;
    }

}

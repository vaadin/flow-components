package com.vaadin.tests;

import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.parallel.setup.RemoteDriver;
import com.vaadin.testbench.parallel.setup.SetupDriver;
import org.junit.AfterClass;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Field;

public abstract class AbstractComponentIT
    extends com.vaadin.flow.testutil.AbstractComponentIT {

    private static SharedBrowser browser = new SharedBrowser();

    @Override
    public void setup() throws Exception {
        if(Parameters.getTestsInParallel() != 1)  {
            super.setup();
            return;
        }
        driver = browser.getDriver(() -> {
            super.setup();
            return getDriver();
        });
        screenshotOnFailure.setQuitDriverOnFinish(false);
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }
}

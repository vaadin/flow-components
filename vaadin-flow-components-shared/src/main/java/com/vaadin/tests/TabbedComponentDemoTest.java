package com.vaadin.tests;

import com.vaadin.testbench.Parameters;
import org.junit.AfterClass;

public abstract class TabbedComponentDemoTest extends com.vaadin.flow.demo.TabbedComponentDemoTest {

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

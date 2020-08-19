package com.vaadin.tests;

import com.vaadin.testbench.Parameters;

public abstract class TabbedComponentDemoTest
    extends com.vaadin.flow.demo.TabbedComponentDemoTest {

    private static SharedBrowser browser = SharedBrowser.instance;

    @Override
    public void setup() throws Exception {
        if (Parameters.getTestsInParallel() != 1) {
            super.setup();
            return;
        }
        driver = browser.getDriver(() -> {
            super.setup();
            return getDriver();
        });
        screenshotOnFailure.setQuitDriverOnFinish(false);
    }

}

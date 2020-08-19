package com.vaadin.tests;

import com.vaadin.testbench.Parameters;

public abstract class ParallelTest
    extends com.vaadin.testbench.parallel.ParallelTest {

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

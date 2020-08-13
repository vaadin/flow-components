package com.vaadin.tests;

public abstract class ParallelTest extends com.vaadin.testbench.parallel.ParallelTest {
    @Override
    public void setup() throws Exception {
        driver = SharedBrowser.instance.getDriver(() -> {
            super.setup();
            return getDriver();
        });
        screenshotOnFailure.setQuitDriverOnFinish(false);
    }
}

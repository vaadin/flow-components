package com.vaadin.tests;

public abstract class AbstractComponentIT extends com.vaadin.flow.testutil.AbstractComponentIT {
    @Override
    public void setup() throws Exception {
        driver = SharedBrowser.instance.getDriver(() -> {
            super.setup();
            return getDriver();
        });
        screenshotOnFailure.setQuitDriverOnFinish(false);
    }
}

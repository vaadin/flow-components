package com.vaadin.tests;

public abstract class TabbedComponentDemoTest extends com.vaadin.flow.demo.TabbedComponentDemoTest {
    @Override
    public void setup() throws Exception {
        driver = SharedBrowser.instance.getDriver(() -> {
            super.setup();
            return getDriver();
        });
        screenshotOnFailure.setQuitDriverOnFinish(false);
    }
}

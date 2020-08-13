package com.vaadin.tests;

public abstract class ComponentDemoTest extends com.vaadin.flow.demo.ComponentDemoTest {
    @Override
    public void setup() throws Exception {
        driver = SharedBrowser.instance.getDriver(() -> {
            super.setup();
            return getDriver();
        });
        screenshotOnFailure.setQuitDriverOnFinish(false);
    }
}

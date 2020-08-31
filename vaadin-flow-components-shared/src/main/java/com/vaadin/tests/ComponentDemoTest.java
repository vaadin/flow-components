package com.vaadin.tests;

import com.vaadin.testbench.Parameters;
import org.junit.AfterClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;

public abstract class ComponentDemoTest extends com.vaadin.flow.demo.ComponentDemoTest {

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

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        return browser.getGridBrowsers().orElse(super.getHubBrowsersToTest());
    }

    protected int getDeploymentPort() {
        return 8080;
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }
}

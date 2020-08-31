package com.vaadin.tests;

import com.vaadin.testbench.Parameters;
import org.junit.AfterClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;

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

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        return browser.getGridBrowsers().orElse(super.getHubBrowsersToTest());
    }

    @AfterClass
    public static void runAfterTest() {
        browser.clear();
    }
}

package com.vaadin.tests;

import org.openqa.selenium.chrome.ChromeOptions;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected void updateHeadlessChromeOptions(ChromeOptions options) {
        // Force legacy Chrome headless mode for the time being,
        // as `--headless=new` has an issue that doesn't allow
        // tests to adjust the browser window size with
        // `getDriver().manage().window().setSize(...)`.
        // See more https://github.com/SeleniumHQ/selenium/issues/11706
        options.addArguments("--headless");
    }

}

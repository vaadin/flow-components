package com.vaadin.tests;

import java.util.List;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class AbstractComponentIT
        extends com.vaadin.flow.testutil.AbstractComponentIT {

    protected int getDeploymentPort() {
        return 8080;
    }

    @Override
    protected List<DesiredCapabilities> customizeCapabilities(
            List<DesiredCapabilities> capabilities) {
        // This method is overridden to force the legacy Chrome headless mode
        // for the time being. The new `--headless=new` mode has an issue
        // that doesn't allow tests to adjust the browser window size with
        // `getDriver().manage().window().setSize(...)`, see more:
        // https://github.com/SeleniumHQ/selenium/issues/11706
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu");

        capabilities.stream()
                .filter(cap -> "chrome".equalsIgnoreCase(cap.getBrowserName()))
                .forEach(cap -> cap.setCapability(ChromeOptions.CAPABILITY,
                        chromeOptions));

        return capabilities;
    }

}

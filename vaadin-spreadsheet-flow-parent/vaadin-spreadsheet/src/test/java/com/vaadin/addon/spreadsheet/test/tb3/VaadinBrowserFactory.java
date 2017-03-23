package com.vaadin.addon.spreadsheet.test.tb3;

import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.DefaultBrowserFactory;

public class VaadinBrowserFactory extends DefaultBrowserFactory {
    private static final String FIREFOX_VERSION = "45";
    private static final String CHROME_VERSION = "40";
    private static final String PHANTOM_VERSION = "2";

    @Override
    public DesiredCapabilities create(Browser browser) {
        switch (browser) {
        case IE8:
            return createIE(browser, "8");
        case IE9:
            return createIE(browser, "9");
        case IE10:
            return createIE(browser, "10");
        case IE11:
            return createIE(browser, "11");
        case PHANTOMJS:
            DesiredCapabilities phantom2 = create(browser, PHANTOM_VERSION,
                    Platform.LINUX);
            // Hack for the test cluster
            phantom2.setCapability("phantomjs.binary.path",
                    "/usr/bin/phantomjs2");
            return phantom2;
        case CHROME:
            return create(browser, CHROME_VERSION, Platform.VISTA);
        case FIREFOX:
        default:
            return createFirefox();
        }
    }

    private DesiredCapabilities createIE(Browser browser, String version) {
        DesiredCapabilities capabilities = create(browser, version,
                Platform.WINDOWS);
        capabilities.setCapability(
                InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        return capabilities;
    }

    private DesiredCapabilities createFirefox() {
        DesiredCapabilities capabilities = create(Browser.FIREFOX,
                FIREFOX_VERSION, Platform.WINDOWS);
        capabilities.setCapability(FirefoxDriver.MARIONETTE, false);
        return capabilities;
    }

    @Override
    public DesiredCapabilities create(Browser browser, String version) {
        return create(browser);
    }
}

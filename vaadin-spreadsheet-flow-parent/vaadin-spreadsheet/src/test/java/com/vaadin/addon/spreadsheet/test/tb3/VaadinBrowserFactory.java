package com.vaadin.addon.spreadsheet.test.tb3;

import org.openqa.selenium.Platform;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.DefaultBrowserFactory;

public class VaadinBrowserFactory extends DefaultBrowserFactory {

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
            return create(browser, "1", Platform.LINUX);
        case CHROME:
            return create(browser, "40", Platform.VISTA);
        case FIREFOX:
        default:
            return create(browser, "24", Platform.XP);
        }
    }

    private DesiredCapabilities createIE(Browser browser, String version) {
        DesiredCapabilities capabilities = create(browser, version,
                Platform.WINDOWS);
        capabilities.setCapability(
                InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        return capabilities;
    }

    @Override
    public DesiredCapabilities create(Browser browser, String version) {
        return create(browser);
    }
}

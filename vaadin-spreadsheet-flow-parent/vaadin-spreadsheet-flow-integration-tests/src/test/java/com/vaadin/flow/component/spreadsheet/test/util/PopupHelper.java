/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test.util;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PopupHelper {

    private final WebDriver driver;
    private final String mainWindowHandle;

    public PopupHelper(WebDriver driver) {
        this.driver = driver;
        mainWindowHandle = driver.getWindowHandle();
    }

    /**
     * Switches to the window a link opened, waiting for it to appear.
     * <p>
     * Opening it is a round trip - the click reaches the server, which asks the
     * browser for the window - so reading the handles once and finding nothing
     * simply left the driver on the page it started on, and whatever the test
     * then asked about the popup it asked about the wrong window.
     */
    public void switchToPopup() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> d.getWindowHandles().size() > 1);
        for (String popupHandle : driver.getWindowHandles()) {
            if (!popupHandle.equals(mainWindowHandle)) {
                driver.switchTo().window(popupHandle);
                return;
            }
        }
    }

    /**
     * Closes the popup and goes back.
     * <p>
     * Left open, it is still there for the next test to find, and
     * {@link #switchToPopup()} would take it - showing the previous test's page
     * - instead of waiting for the new one.
     */
    public void backToMainWindow() {
        if (!mainWindowHandle.equals(driver.getWindowHandle())) {
            driver.close();
        }
        driver.switchTo().window(mainWindowHandle);
    }
}

/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test.util;

import java.util.Set;

import org.openqa.selenium.WebDriver;

public class PopupHelper {

    private final WebDriver driver;
    private final String mainWindowHandle;

    public PopupHelper(WebDriver driver) {
        this.driver = driver;
        mainWindowHandle = driver.getWindowHandle();
    }

    public void switchToPopup() {
        Set<String> handleSet = driver.getWindowHandles();

        for (String popupHandle : handleSet) {
            if (!popupHandle.contains(mainWindowHandle)) {
                driver.switchTo().window(popupHandle);
                return;
            }
        }
    }

    public void backToMainWindow() {
        driver.switchTo().window(mainWindowHandle);
    }
}

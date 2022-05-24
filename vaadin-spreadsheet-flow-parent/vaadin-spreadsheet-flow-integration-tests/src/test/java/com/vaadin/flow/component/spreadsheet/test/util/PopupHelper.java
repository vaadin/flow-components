package com.vaadin.flow.component.spreadsheet.test.util;

import org.openqa.selenium.WebDriver;

import java.util.Set;

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

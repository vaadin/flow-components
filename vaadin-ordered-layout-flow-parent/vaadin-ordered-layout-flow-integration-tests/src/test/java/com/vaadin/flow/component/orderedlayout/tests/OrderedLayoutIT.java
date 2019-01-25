package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("ordered-layout-tests")
public class OrderedLayoutIT extends AbstractComponentIT {

    @Test
    public void testHorizontalLayout_spacingTheme() {
        open();
        WebElement hLayout = findElement(By.id("hl-spacing"));
        testSpacing(hLayout);
    }

    @Test
    public void testVerticalLayout_spacingTheme() {
        open();
        WebElement vLayout = findElement(By.id("vl-spacing"));
        testSpacing(vLayout);
    }

    private void testSpacing(WebElement layout) {
        checkThemeChanges(layout, "spacing", false);
        checkThemeChanges(layout, "spacing-xs", true);
        checkThemeChanges(layout, "spacing-s", true);
        checkThemeChanges(layout, "spacing-l", true);
        checkThemeChanges(layout, "spacing-xl", true);
        checkThemeChanges(layout, "spacing", true);
    }

    private void checkThemeChanges(WebElement layoutToCheck, String themeName,
            boolean shouldPresent) {
        layoutToCheck.findElement(By.id(String.format("toggle-%s", themeName)))
                .click();
        if (shouldPresent) {
            waitUntil(dr -> layoutToCheck.getAttribute("theme") != null
                    && layoutToCheck.getAttribute("theme").contains(themeName));
        } else {
            waitUntil(dr -> layoutToCheck.getAttribute("theme") == null
                    || !layoutToCheck.getAttribute("theme")
                            .contains(themeName));
        }
    }
}

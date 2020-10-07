package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-ordered-layout/ordered-layout-tests")
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

    @Test
    public void testGetterSetterFlexWrapFlexLayout() {
        open();
        WebElement fLayout = findElement(By.id("flex-layout"));
        WebElement btnNoWrap = fLayout
                .findElement(By.id("no-wrap"));
        WebElement btnWrap = fLayout
                .findElement(By.id("wrap"));
        WebElement btnWrapReverse = fLayout
                .findElement(By.id("wrap-reverse"));
        WebElement getFlexWrapBtn = fLayout
                .findElement(By.id("wrap-btn"));
        WebElement flexWrapDisplay = findElement(By.id("flex-wrap-display"));

        btnNoWrap.click();
        Assert.assertEquals("nowrap",
                fLayout.getCssValue("flex-wrap"));

        btnWrap.click();
        Assert.assertEquals("wrap",
                fLayout.getCssValue("flex-wrap"));

        btnWrapReverse.click();
        Assert.assertEquals("wrap-reverse",
                fLayout.getCssValue("flex-wrap"));

        getFlexWrapBtn.click();
        Assert.assertEquals("WRAP_REVERSE", flexWrapDisplay.getText());
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

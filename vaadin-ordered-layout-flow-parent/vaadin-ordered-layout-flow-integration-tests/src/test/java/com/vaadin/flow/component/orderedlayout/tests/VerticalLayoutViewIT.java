/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Tests for the VerticalLayout.
 */
@TestPath("vaadin-ordered-layout")
public class VerticalLayoutViewIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void defaultLayout() {
        WebElement vlayout = findElement(By.id("vertical-default-layout"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertTrue(
                "By default layout should contain spacing theme in 'theme' attribute",
                vlayout.getAttribute("theme").contains("spacing"));
        Assert.assertTrue(
                "By default layout should contain margin theme in 'theme' attribute",
                vlayout.getAttribute("theme").contains("margin"));

        checkThemeChanges(vlayout, "spacing", false);
        checkThemeChanges(vlayout, "margin", false);

        Assert.assertNull(
                "After turning off spacing and padding, layout should not contain 'theme' attribute",
                vlayout.getAttribute("theme"));

        checkThemeChanges(vlayout, "margin", true);

        checkThemeChanges(vlayout, "margin", false);

        Assert.assertNull(
                "After turning off everything, layout should not contain 'theme' attribute",
                vlayout.getAttribute("theme"));
    }

    @Test
    public void layoutWithJustifyContent() {
        WebElement vlayout = findElement(
                By.id("vertical-layout-with-justify-content"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("space-between",
                getAlignment(vlayout, "justify-content"));

        RadioButtonGroupElement rbg = $(RadioButtonGroupElement.class)
                .id("vertical-layout-justify-content-radio-button");
        rbg.selectByText(
                FlexComponent.JustifyContentMode.START.name().toLowerCase());
        Assert.assertEquals("flex-start",
                getAlignment(vlayout, "justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.END.name().toLowerCase());
        Assert.assertEquals("flex-end",
                getAlignment(vlayout, "justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.BETWEEN.name().toLowerCase());
        Assert.assertEquals("space-between",
                getAlignment(vlayout, "justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.AROUND.name().toLowerCase());
        Assert.assertEquals("space-around",
                getAlignment(vlayout, "justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.EVENLY.name().toLowerCase());
        Assert.assertEquals("space-evenly",
                getAlignment(vlayout, "justify-content"));
    }

    @Test
    public void layoutWithAlignment() {
        WebElement vlayout = findElement(
                By.id("vertical-layout-with-alignment"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("stretch", getAlignment(vlayout, "align-items"));
        RadioButtonGroupElement rbg = $(RadioButtonGroupElement.class)
                .id("vertical-layout-alignment-radio-button");

        rbg.selectByText(FlexComponent.Alignment.END.name().toLowerCase());
        waitUntil(driver -> "flex-end"
                .equals(getAlignment(vlayout, "align-items")));

        rbg.selectByText(FlexComponent.Alignment.CENTER.name().toLowerCase());
        waitUntil(driver -> "center"
                .equals(getAlignment(vlayout, "align-items")));

        rbg.selectByText(FlexComponent.Alignment.STRETCH.name().toLowerCase());
        waitUntil(driver -> "stretch"
                .equals(getAlignment(vlayout, "align-items")));

        rbg.selectByText(FlexComponent.Alignment.START.name().toLowerCase());
        waitUntil(driver -> "flex-start"
                .equals(getAlignment(vlayout, "align-items")));
    }

    @Test
    public void layoutWithIndividualAlignments() {
        WebElement vlayout = findElement(
                By.id("vertical-layout-with-individual-alignments"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("space-between",
                getAlignment(vlayout, "justify-content"));

        WebElement child = vlayout.findElement(By.id("start-aligned"));
        Assert.assertEquals("flex-start", getAlignment(child, "align-self"));

        child = vlayout.findElement(By.id("center-aligned"));
        Assert.assertEquals("center", getAlignment(child, "align-self"));

        child = vlayout.findElement(By.id("end-aligned"));
        Assert.assertEquals("flex-end", getAlignment(child, "align-self"));

        child = vlayout.findElement(By.id("stretch-aligned"));
        Assert.assertEquals("stretch", getAlignment(child, "align-self"));
    }

    @Test
    public void layoutWithExpandRatios() {
        WebElement vlayout = findElement(
                By.id("vertical-layout-with-expand-ratios"));
        assertBasicFlexPropertiesAreSet(vlayout);

        WebElement child = vlayout.findElement(By.id("ratio-1"));
        Assert.assertEquals("1", child.getCssValue("flex-grow"));

        child = vlayout.findElement(By.id("ratio-2"));
        Assert.assertEquals("2", child.getCssValue("flex-grow"));

        child = vlayout.findElement(By.id("ratio-0.5"));
        Assert.assertEquals("0.5", child.getCssValue("flex-grow"));
    }

    @Test
    public void centerComponent() {
        WebElement vlayout = findElement(By.id("vertical-layout-with-center"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("center", getAlignment(vlayout, "justify-content"));
        WebElement component = vlayout.findElement(By.id("center"));
        Assert.assertEquals("center", getAlignment(component, "align-self"));
    }

    @Test
    public void boxSizing() {
        WebElement vlayout = findElement(
                By.id("vertical-layout-with-box-sizing"));
        Assert.assertEquals("border-box", vlayout.getCssValue("box-sizing"));

        RadioButtonGroupElement rbg = $(RadioButtonGroupElement.class)
                .id("vertical-layout-with-box-sizing-radio-button");
        rbg.selectByText("Content-box");
        Assert.assertEquals("content-box", vlayout.getCssValue("box-sizing"));
    }

    private void assertBasicFlexPropertiesAreSet(WebElement vlayout) {
        Assert.assertEquals("column", vlayout.getCssValue("flex-direction"));
    }

    /**
     * Gets the computed value of an alignment property. Chrome 158 and newer
     * report {@code flex-start} and {@code flex-end} as {@code flow-start} and
     * {@code flow-end}, so those are mapped back to keep the expected values
     * the same in all browser versions.
     */
    private static String getAlignment(WebElement element, String property) {
        return element.getCssValue(property).replaceFirst("^flow-", "flex-");
    }

    private void checkThemeChanges(WebElement layoutToCheck, String themeName,
            boolean shouldPresent) {
        findElement(By.id(String.format("toggle-vertical-%s", themeName)))
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


package com.vaadin.flow.component.orderedlayout.tests;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.ComponentDemoTest;

/**
 * Tests for the HorizontalLayout.
 */
public class HorizontalLayoutViewIT extends ComponentDemoTest {
    @Override
    protected String getTestPath() {
        return "/vaadin-ordered-layout/horizontallayout";
    }

    @Test
    public void defaultLayout() {
        WebElement hLayout = layout.findElement(By.id("default-layout"));
        assertBasicFlexPropertiesAreSet(hLayout);

        Assert.assertTrue(
                "By default layout should contain spacing theme in 'theme' attribute",
                hLayout.getAttribute("theme").contains("spacing"));
        Assert.assertTrue(
                "By default layout should contain margin theme in 'theme' attribute",
                hLayout.getAttribute("theme").contains("margin"));

        checkThemeChanges(hLayout, "spacing", false);
        checkThemeChanges(hLayout, "margin", false);

        Assert.assertNull(
                "After turning off spacing and margin, layout should not contain 'theme' attribute",
                hLayout.getAttribute("theme"));

        checkThemeChanges(hLayout, "padding", true);
        checkThemeChanges(hLayout, "margin", true);

        checkThemeChanges(hLayout, "padding", false);
        checkThemeChanges(hLayout, "margin", false);

        Assert.assertNull(
                "After turning off everything, layout should not contain 'theme' attribute",
                hLayout.getAttribute("theme"));
    }

    @Test
    public void layoutWithJustifyContent() {
        WebElement hLayout = layout
                .findElement(By.id("layout-with-justify-content"));
        assertBasicFlexPropertiesAreSet(hLayout);

        Assert.assertEquals("space-between",
                hLayout.getCssValue("justify-content"));

        RadioButtonGroupElement rbg = $(RadioButtonGroupElement.class)
                .id("horizontal-layout-justify-content-radio-button");

        rbg.selectByText(
                FlexComponent.JustifyContentMode.START.name().toLowerCase());
        Assert.assertEquals("flex-start",
                hLayout.getCssValue("justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.END.name().toLowerCase());
        Assert.assertEquals("flex-end", hLayout.getCssValue("justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.BETWEEN.name().toLowerCase());
        Assert.assertEquals("space-between",
                hLayout.getCssValue("justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.AROUND.name().toLowerCase());
        Assert.assertEquals("space-around",
                hLayout.getCssValue("justify-content"));

        rbg.selectByText(
                FlexComponent.JustifyContentMode.EVENLY.name().toLowerCase());
        Assert.assertEquals("space-evenly",
                hLayout.getCssValue("justify-content"));
    }

    @Test
    public void layoutWithAlignment() {
        WebElement vlayout = layout.findElement(By.id("layout-with-alignment"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("center", vlayout.getCssValue("align-items"));

        RadioButtonGroupElement rbg = $(RadioButtonGroupElement.class)
                .id("horizontal-layout-alignment-radio-button");
        rbg.selectByText(FlexComponent.Alignment.END.name().toLowerCase());
        waitUntil(driver -> "flex-end"
                .equals(vlayout.getCssValue("align-items")));

        rbg.selectByText(FlexComponent.Alignment.CENTER.name().toLowerCase());
        waitUntil(
                driver -> "center".equals(vlayout.getCssValue("align-items")));

        rbg.selectByText(FlexComponent.Alignment.STRETCH.name().toLowerCase());
        waitUntil(
                driver -> "stretch".equals(vlayout.getCssValue("align-items")));

        rbg.selectByText(FlexComponent.Alignment.START.name().toLowerCase());
        waitUntil(driver -> "flex-start"
                .equals(vlayout.getCssValue("align-items")));

        rbg.selectByText(FlexComponent.Alignment.BASELINE.name().toLowerCase());
        waitUntil(driver -> "baseline"
                .equals(vlayout.getCssValue("align-items")));
    }

    @Test
    public void layoutWithIndividualAlignments() {
        WebElement vlayout = layout
                .findElement(By.id("layout-with-individual-alignments"));
        assertBasicFlexPropertiesAreSet(vlayout);

        Assert.assertEquals("space-between",
                vlayout.getCssValue("justify-content"));

        WebElement child = vlayout.findElement(By.id("start-aligned"));
        Assert.assertEquals("flex-start", child.getCssValue("align-self"));

        child = vlayout.findElement(By.id("center-aligned"));
        Assert.assertEquals("center", child.getCssValue("align-self"));

        child = vlayout.findElement(By.id("end-aligned"));
        Assert.assertEquals("flex-end", child.getCssValue("align-self"));

        child = vlayout.findElement(By.id("stretch-aligned"));
        Assert.assertEquals("stretch", child.getCssValue("align-self"));
    }

    @Test
    public void layoutWithExpandRatios() {
        WebElement vlayout = layout
                .findElement(By.id("layout-with-expand-ratios"));
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
        WebElement hlayout = layout.findElement(By.id("layout-with-center"));
        assertBasicFlexPropertiesAreSet(hlayout);

        Assert.assertEquals("center", hlayout.getCssValue("justify-content"));

        Assert.assertTrue(isElementPresent(By.id("center")));
    }

    @Test
    public void boxSizing() {
        WebElement hlayout = layout
                .findElement(By.id("horizontal-layout-with-box-sizing"));
        Assert.assertEquals("border-box", hlayout.getCssValue("box-sizing"));

        RadioButtonGroupElement rbg = $(RadioButtonGroupElement.class)
                .id("horizontal-layout-with-box-sizing-radio-button");
        rbg.selectByText("Content-box");
        Assert.assertEquals("content-box", hlayout.getCssValue("box-sizing"));
    }

    private void assertBasicFlexPropertiesAreSet(WebElement vlayout) {
        Assert.assertEquals("row", vlayout.getCssValue("flex-direction"));
    }

    private void checkThemeChanges(WebElement layoutToCheck, String themeName,
            boolean shouldPresent) {
        layout.findElement(By.id(String.format("toggle-%s", themeName)))
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

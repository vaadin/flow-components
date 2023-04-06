
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.tests.ComponentDemoTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link CheckboxDemoPage}.
 */
public class CheckboxIT extends ComponentDemoTest {

    @Test
    public void defaultCheckbox() {
        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertTrue("Default checkbox should be present",
                "vaadin-checkbox".equals(checkbox.getTagName()));
    }

    @Test
    public void defaultCheckbox_labelIsDisplayed() {
        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertEquals(
                "Checkbox label should have the text 'Default Checkbox'",
                "Default Checkbox", checkbox.getText());
    }

    @Test
    public void defaultCheckbox_changeLabel_newLabelIsDisplayed() {
        clickButton("change-default-label");

        WebElement checkbox = layout.findElement(By.id("default-checkbox"));
        Assert.assertEquals("Checkbox label should have the text 'New Label'",
                "New Label", checkbox.getText());
    }

    @Test
    public void defaultCheckbox_changeLabel_clickOnLabel_checkboxIsChecked() {
        clickButton("change-default-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("default-checkbox");
        checkbox.$("label").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void defaultCheckbox_changeLabel_clickOnInput_checkboxIsChecked() {
        clickButton("change-default-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("default-checkbox");
        checkbox.$("input").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void disabledCheckbox() {
        WebElement checkbox = layout.findElement(By.id("disabled-checkbox"));
        Assert.assertEquals("true", checkbox.getAttribute("disabled"));
        WebElement message = layout
                .findElement(By.id("disabled-checkbox-message"));
        Assert.assertEquals("", message.getText());

        executeScript("arguments[0].removeAttribute(\"disabled\");"
                + "arguments[0].click();", checkbox);
        message = layout.findElement(By.id("disabled-checkbox-message"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void indeterminateCheckbox() {
        WebElement checkbox = layout
                .findElement(By.id("indeterminate-checkbox"));
        WebElement button = layout.findElement(By.id("reset-indeterminate"));
        Assert.assertEquals("This checkbox should be in indeterminate state",
                "true", checkbox.getAttribute("indeterminate"));

        checkbox.click();
        Assert.assertNotEquals(
                "Checkbox should not be in indeterminate state after clicking it",
                "true", checkbox.getAttribute("indeterminate"));

        clickElementWithJs(button);
        Assert.assertEquals(
                "This checkbox should be in indeterminate state after resetting",
                "true", checkbox.getAttribute("indeterminate"));
    }

    @Test
    public void valueChangeCheckbox() {
        WebElement checkbox = layout
                .findElement(By.id("value-change-checkbox"));
        WebElement message = layout
                .findElement(By.id("value-change-checkbox-message"));
        checkbox.click();
        Assert.assertEquals("Clicking checkbox should update message div",
                "Checkbox value changed from 'false' to 'true'",
                message.getText());
    }

    @Test
    public void accessibleCheckbox() {
        WebElement checkbox = layout.findElement(By.id("accessible-checkbox"));
        Assert.assertEquals(
                "Accessible checkbox should have the aria-label attribute",
                "Click me", checkbox.getAttribute("aria-label"));
    }

    @Test
    public void htmlLabelCheckbox_labelLinkIsDisplayed() {
        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("html-label-checkbox");
        WebElement anchor = checkbox.$("label").first()
                .findElement(By.tagName("a"));

        Assert.assertEquals(
                "Content should contain a link to vaadin.com/privacy-policy",
                "https://vaadin.com/privacy-policy",
                anchor.getAttribute("href"));
    }

    @Test
    public void htmlLabelCheckbox_changeLabel_newLabelLinkIsDisplayed() {
        clickButton("change-html-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("html-label-checkbox");
        WebElement anchor = checkbox.$("label").first()
                .findElement(By.tagName("a"));

        Assert.assertEquals(
                "Content should contain a link to vaadin.com/community-terms",
                "https://vaadin.com/community-terms",
                anchor.getAttribute("href"));
    }

    @Test
    public void htmlLabelCheckbox_changeLabel_clickOnInput_checkboxIsChecked() {
        clickButton("change-html-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("html-label-checkbox");
        checkbox.$("input").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void htmlLabelCheckbox_changeLabel_clickOnLabel_checkboxIsChecked() {
        clickButton("change-html-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("html-label-checkbox");
        checkbox.$("label").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void lazyHtmlLabelCheckbox_setLabel_labelLinkIsDisplayed() {
        clickButton("set-html-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("lazy-html-label-checkbox");
        WebElement anchor = checkbox.$("label").first()
                .findElement(By.tagName("a"));

        Assert.assertEquals(
                "Content should contain a link to vaadin.com/privacy-policy",
                "https://vaadin.com/privacy-policy",
                anchor.getAttribute("href"));
    }

    @Test
    public void lazyHtmlLabelCheckbox_setLabel_clickOnLabel_checkboxIsChecked() {
        clickButton("set-html-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("lazy-html-label-checkbox");
        checkbox.$("label").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Test
    public void lazyHtmlLabelCheckbox_setLabel_clickOnInput_checkboxIsChecked() {
        clickButton("set-html-label");

        CheckboxElement checkbox = $(CheckboxElement.class)
                .id("lazy-html-label-checkbox");
        checkbox.$("input").first().click();

        Assert.assertEquals("Checkbox should be checked", true,
                checkbox.isChecked());
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-checkbox-test-demo");
    }

    private void clickButton(String id) {
        $("button").id(id).click();
    }
}

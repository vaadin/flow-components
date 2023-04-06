
package com.vaadin.flow.component.checkbox.tests;

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.tests.ComponentDemoTest;

public class CheckboxGroupIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/vaadin-checkbox-group-test-demo";
    }

    @Test
    public void valueChange() {
        WebElement valueDiv = layout.findElement(By.id("checkbox-group-value"));
        WebElement group = layout.findElement(
                By.id("checkbox-group-with-value-change-listener"));

        executeScript("arguments[0].value=['2'];", group);

        waitUntil(driver -> "Checkbox group value changed from '[]' to '[bar]'"
                .equals(valueDiv.getText()));

        executeScript("arguments[0].value=['1','2'];", group);

        waitUntil(
                driver -> "Checkbox group value changed from '[bar]' to '[bar, foo]'"
                        .equals(valueDiv.getText()));
    }

    @Test
    public void itemGenerator() {
        WebElement valueDiv = layout
                .findElement(By.id("checkbox-group-gen-value"));
        WebElement group = layout
                .findElement(By.id("checkbox-group-with-item-generator"));

        executeScript("arguments[0].value=['5'];", group);

        waitUntil(driver -> "Checkbox group value changed from '[]' to '[John]'"
                .equals(valueDiv.getText()));
    }

    @Test
    public void disabledGroup() {
        WebElement group = layout.findElement(By.id("checkbox-group-disabled"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));
    }

    @Test
    public void disabledGroupItems() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-group-disabled-items");

        List<TestBenchElement> checkboxes = group.$("vaadin-checkbox").all();

        Assert.assertEquals(Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("disabled"));

        scrollToElement(group);

        executeScript("arguments[0].value=['1'];", group);

        WebElement infoLabel = layout
                .findElement(By.id("checkbox-group-disabled-items-info"));

        Assert.assertEquals("'foo' should be selected", "[foo]",
                infoLabel.getText());

        executeScript("arguments[0].value=['1','2'];", group);

        try {
            waitUntil(
                    driver -> group.findElements(By.tagName("vaadin-checkbox"))
                            .get(1).getAttribute("disabled") != null);
        } catch (WebDriverException wde) {
            Assert.fail("Server should have disabled the checkbox again.");
        }

        Assert.assertEquals("Value 'foo' should have been re-selected", "[foo]",
                infoLabel.getText());

        Assert.assertTrue(
                "Value 'foo' should have been re-selected on the client side",
                Boolean.valueOf(checkboxes.get(0).getAttribute("checked")));
    }

    @Test
    public void readOnlyGroup() {
        WebElement group = layout
                .findElement(By.id("checkbox-group-read-only"));

        List<WebElement> checkboxes = group
                .findElements(By.tagName("vaadin-checkbox"));

        Assert.assertEquals(Boolean.TRUE.toString(),
                checkboxes.get(1).getAttribute("disabled"));
        Assert.assertEquals(Boolean.TRUE.toString(),
                group.getAttribute("disabled"));

        scrollToElement(group);
        getCommandExecutor().executeScript("window.scrollBy(0,50);");

        executeScript("arguments[0].value=['2'];", group);

        WebElement valueInfo = layout.findElement(By.id("selected-value-info"));
        Assert.assertEquals("", valueInfo.getText());

        // make the group not read-only
        WebElement switchReadOnly = findElement(By.id("switch-read-only"));
        new Actions(getDriver()).moveToElement(switchReadOnly).click().build()
                .perform();

        executeScript("arguments[0].value=['2'];", group);
        Assert.assertEquals("[bar]", valueInfo.getText());

        // make it read-only again
        new Actions(getDriver()).moveToElement(switchReadOnly).click().build()
                .perform();

        // click to the first item
        executeScript("arguments[0].value=['1'];", group);

        // Nothing has changed
        Assert.assertEquals("[bar]", valueInfo.getText());
    }

    @Test
    public void assertThemeVariant() {
        verifyThemeVariantsBeingToggled();
    }

    @Test
    public void groupHasLabelAndErrorMessage_setInvalidShowEM_setValueRemoveEM() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("group-with-label-and-error-message");

        Assert.assertEquals("Label Attribute should present with correct text",
                group.getAttribute("label"), "Group label");

        TestBenchElement errorMessage = group.$("div")
                .attributeContains("slot", "error-message").first();

        verifyGroupValid(group, errorMessage);

        layout.findElement(By.id("group-with-label-button")).click();
        verifyGroupInvalid(group, errorMessage);

        Assert.assertEquals(
                "Correct error message should be shown after the button clicks",
                "Field has been set to invalid from server side",
                errorMessage.getText());
    }

    @Test
    public void assertHelperText() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-helper-text");

        TestBenchElement helperText = group.$("div")
                .attributeContains("slot", "helper").first();

        Assert.assertEquals("Helper text", helperText.getText());

        $("button").id("button-clear-helper").click();
        Assert.assertEquals("", helperText.getText());
    }

    @Test
    public void assertHelperComponent() {
        TestBenchElement group = $(TestBenchElement.class)
                .id("checkbox-helper-component");

        TestBenchElement helperComponent = group.$("span")
                .attributeContains("slot", "helper").first();
        Assert.assertEquals("Helper text", helperComponent.getText());

        $("button").id("button-clear-component").click();

        waitUntil(ExpectedConditions
                .invisibilityOfElementLocated(By.id("helper-component")));
    }

    private void verifyGroupInvalid(TestBenchElement group,
            TestBenchElement errorMessage) {
        Assert.assertEquals("Checkbox group is invalid.", true,
                group.getPropertyBoolean("invalid"));
        Assert.assertFalse("Error message should be shown.",
                errorMessage.getText().isEmpty());
    }

    private void verifyGroupValid(TestBenchElement group,
            TestBenchElement errorMessage) {
        Boolean isInvalid = group.getPropertyBoolean("invalid");
        Assert.assertThat("Checkbox group is not invalid.", isInvalid,
                CoreMatchers.anyOf(CoreMatchers.equalTo(isInvalid),
                        CoreMatchers.equalTo(false)));
        Assert.assertTrue("Error message should be empty.",
                errorMessage.getText().isEmpty());
    }
}

/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Integration tests for the ButtonView.
 */
@TestPath("vaadin-button")
public class ButtonIT extends AbstractComponentIT {

    private TestBenchTestCase layout;

    @Before
    public void init() {
        open();
        layout = this;
        waitForElementPresent(By.tagName("vaadin-button"));
    }

    @Test
    public void clickOnDefaultButton_textIsDisplayed() {
        WebElement button = layout.findElement(By.id("default-button"));

        scrollIntoViewAndClick(button);
        waitUntilMessageIsChangedForClickedButton("Vaadin button");
    }

    @Test
    public void clickOnIconButtons_textIsDisplayed() {
        WebElement leftButton = layout.findElement(By.id("left-icon-button"));
        WebElement icon = leftButton.findElement(By.tagName("vaadin-icon"));
        Assert.assertEquals("vaadin:arrow-left", icon.getAttribute("icon"));

        // the icon is before the text
        Assert.assertTrue(getCenterX(leftButton) > getCenterX(icon));

        WebElement rightButton = layout.findElement(By.id("right-icon-button"));
        icon = rightButton.findElement(By.tagName("vaadin-icon"));
        Assert.assertEquals("vaadin:arrow-right", icon.getAttribute("icon"));

        // the icon is after the text
        Assert.assertTrue(getCenterX(rightButton) < getCenterX(icon));

        WebElement thumbButton = layout.findElement(By.id("thumb-icon-button"));
        icon = thumbButton.findElement(By.tagName("vaadin-icon"));
        Assert.assertEquals("vaadin:thumbs-up", icon.getAttribute("icon"));

        scrollIntoViewAndClick(leftButton);
        waitUntilMessageIsChangedForClickedButton("Left");

        scrollIntoViewAndClick(rightButton);
        waitUntilMessageIsChangedForClickedButton("Right");

        scrollIntoViewAndClick(thumbButton);
        waitUntilMessageIsChangedForClickedButton("thumbs up");
    }

    @Test
    public void clickOnImageButton_textIsDisplayed() {
        WebElement button = layout.findElement(By.id("image-button"));

        WebElement img = button.findElement(By.tagName("img"));
        img.getAttribute("href");

        Assert.assertEquals(getRootURL() + "/img/vaadin-logo.svg",
                img.getAttribute("src"));
        Assert.assertEquals("Vaadin logo", img.getAttribute("alt"));

        scrollIntoViewAndClick(button);
        waitUntilMessageIsChangedForClickedButton("with image");
    }

    @Test
    public void clickOnAccessibleButton_textIsDisplayed() {
        WebElement button = layout.findElement(By.id("accessible-button"));
        Assert.assertEquals("Click me", button.getAttribute("aria-label"));

        scrollIntoViewAndClick(button);
        waitUntilMessageIsChangedForClickedButton("Accessible");
    }

    @Test
    public void clickOnTabIndexButtons_textIsDisplayed() {
        WebElement button1 = layout.findElement(By.id("button-tabindex-1"));
        Assert.assertEquals("1", button1.getAttribute("tabindex"));
        WebElement button2 = layout.findElement(By.id("button-tabindex-2"));
        Assert.assertEquals("2", button2.getAttribute("tabindex"));
        WebElement button3 = layout.findElement(By.id("button-tabindex-3"));
        Assert.assertEquals("3", button3.getAttribute("tabindex"));

        scrollIntoViewAndClick(button3);
        waitUntilMessageIsChangedForClickedButton("3");

        scrollIntoViewAndClick(button2);
        waitUntilMessageIsChangedForClickedButton("2");

        scrollIntoViewAndClick(button1);
        waitUntilMessageIsChangedForClickedButton("1");
    }

    @Test
    public void clickOnDisabledButton_nothingIsDisplayed() {
        WebElement button = layout.findElement(By.id("disabled-button"));
        Assert.assertTrue("The button should contain the 'disabled' attribute",
                button.getAttribute("disabled").equals("")
                        || button.getAttribute("disabled").equals("true"));

        // valo theme adds the pointer-events: none CSS property, which makes
        // the button unclickable by selenium.
        Assert.assertEquals("none", button.getCssValue("pointer-events"));

        WebElement message = layout.findElement(By.id("buttonMessage"));
        Assert.assertEquals("", message.getText());

        // Remove disabled Attribute and click again from client side, click
        // message should not been shown in the dom
        executeScript("arguments[0].removeAttribute(\"disabled\");"
                + "arguments[0].click();", button);
        message = layout.findElement(By.id("buttonMessage"));
        Assert.assertEquals("", message.getText());
    }

    @Test
    public void clickDisableOnClickButton_newClickNotRegistered() {
        WebElement button = layout
                .findElement(By.id("disable-on-click-button"));
        Assert.assertTrue(
                "The button should not contain the 'disabled' attribute",
                button.getAttribute("disabled") == null);

        scrollToElement(button);
        executeScript(
                "arguments[0].click();arguments[0].click();arguments[0].click();arguments[0].click();",
                button);

        // Check that button is disabled
        String disabled = button.getAttribute("disabled");
        Assert.assertTrue(
                "The button should contain the 'disabled' attribute after click",
                disabled != null);

        String singleClick = "Button Disabled on click was clicked and enabled state was changed to false receiving 1 clicks";
        Assert.assertEquals(
                "Too many click events received to disabled button.",
                singleClick,
                layout.findElement(By.id("disabled-message")).getText());

        // Remove disabled Attribute and click again from client side, click
        // message should not been shown in the dom
        executeScript(
                "arguments[0].removeAttribute(\"disabled\");"
                        + "arguments[0].click();",
                layout.findElement(By.id("disable-on-click-button")));

        Assert.assertEquals("\"Hacking\" button disabled state went through.",
                singleClick,
                layout.findElement(By.id("disabled-message")).getText());

        // test that enabling after disable on click works more than once ...
        for (int i = 0; i < 3; i++) {
            // Enable button from server side.
            layout.findElement(By.id("enable-button")).click();

            button = layout.findElement(By.id("disable-on-click-button"));

            // Check that button is not disabled anymore
            Assert.assertTrue(
                    "The button should not contain the 'disabled' attribute after server side clearing (iteration: "
                            + i + ")",
                    button.getAttribute("disabled") == null);

            Assert.assertEquals(
                    "Button re-enabled message should be the latest message (iteration: "
                            + i + ")",
                    "Re-enabled button from server.",
                    layout.findElement(By.id("disabled-message")).getText());

            button.click();

            // Assert that button gets disabled again on click
            disabled = button.getAttribute("disabled");
            Assert.assertTrue(
                    "The button should contain the 'disabled' attribute after click (iteration: "
                            + i + ")",
                    disabled != null);

            Assert.assertEquals(
                    "Button should have gotten 1 click and become disabled.",
                    singleClick,
                    layout.findElement(By.id("disabled-message")).getText());
        }
    }

    @Test // https://github.com/vaadin/vaadin-button-flow/issues/115
    public void disableButtonOnClick_canBeEnabled() {
        getCommandExecutor().disableWaitForVaadin();
        ButtonElement button = $(ButtonElement.class)
                .id("temporarily-disabled-button");

        for (int i = 0; i < 3; i++) {
            button.click();

            Assert.assertFalse("button should be disabled", button.isEnabled());
            waitUntil(ExpectedConditions.elementToBeClickable(
                    $(ButtonElement.class).id("temporarily-disabled-button")),
                    2000);

            Assert.assertTrue("button should be enabled again",
                    button.isEnabled());
        }

        getCommandExecutor().enableWaitForVaadin();
    }

    @Test
    public void removeDisabled_buttonWorksNormally() {
        WebElement button = layout
                .findElement(By.id("disable-on-click-button"));
        Assert.assertTrue(
                "The button should not contain the 'disabled' attribute",
                button.getAttribute("disabled") == null);

        scrollIntoViewAndClick(button);

        // Check that button is disabled
        String disabled = button.getAttribute("disabled");
        Assert.assertTrue(
                "The button should contain the 'disabled' attribute after click",
                disabled != null);

        layout.findElement(By.id("enable-button")).click();
        layout.findElement(By.id("toggle-button")).click();

        button = layout.findElement(By.id("disable-on-click-button"));

        button.click();
        button.click();

        Assert.assertNull(
                "The button should not be disabled after " + "click anymore.",
                button.getAttribute("disabled"));

        String singleClick = "Button Disabled on click was clicked and enabled state was changed to true receiving 2 clicks";
        Assert.assertEquals("Button didn't get expected amount of clicks.",
                singleClick,
                layout.findElement(By.id("disabled-message")).getText());

    }

    @Test
    public void buttonShortcuts_shortcutsWork() {
        WebElement button = findElement(By.id("shortcuts-enter-button"));

        scrollToElement(button);

        // invoke shortcut
        WebElement body = getDriver().findElement(By.xpath("//body"));
        body.sendKeys(Keys.ENTER);

        waitUntilMessageIsChangedForClickedButton("Has global Enter-shortcut");

        WebElement firstNameField = layout
                .findElement(By.id("shortcuts-firstname"));
        WebElement lastNamefield = layout
                .findElement(By.id("shortcuts" + "-lastname"));

        firstNameField.sendKeys("text 1");
        lastNamefield.sendKeys("text 2");

        Assert.assertEquals("text 1", firstNameField.getAttribute("value"));
        Assert.assertEquals("text 2", lastNamefield.getAttribute("value"));

        lastNamefield.sendKeys(Keys.ALT, "l");

        Assert.assertEquals("", firstNameField.getAttribute("value"));
        Assert.assertEquals("", lastNamefield.getAttribute("value"));
    }

    private void waitUntilMessageIsChangedForClickedButton(
            String messageString) {
        final String expected = "Button " + messageString + " was clicked.";
        WebElement message = layout.findElement(By.id("buttonMessage"));

        WebDriverWait wait = new WebDriverWait(getDriver(),
                Duration.ofSeconds(5));
        wait.until(driver -> {
            String msg = message.getText();
            wait.withMessage("Expected '" + expected + "' but found '"
                    + (msg != null ? msg : "") + "'!");
            return expected.equals(msg);
        });
    }

    @Test
    public void assertVariants() {
        WebElement button = findElement(By.id("button-theme-variants"));
        Assert.assertEquals("small primary", button.getAttribute("theme"));

        findElement(By.id("remove-theme-variant-button")).click();
        Assert.assertEquals("primary", button.getAttribute("theme"));
    }

    private int getCenterX(WebElement element) {
        return element.getLocation().getX() + element.getSize().getWidth() / 2;
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-button");
    }
}

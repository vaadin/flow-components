/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog-view")
public class DialogIT extends AbstractComponentIT {

    @Test
    public void openAndCloseConfirmationDialog_buttonsRenderedWithClickListeners() {
        open();

        WebElement message = findElement(By.id("confirmation-dialog-message"));

        findElement(By.id("confirmation-dialog-button")).click();
        getDialog().findElements(By.tagName("vaadin-button")).get(0).click();
        verifyDialogClosed();
        Assert.assertEquals("Confirmed!", message.getText());

        findElement(By.id("confirmation-dialog-button")).click();
        getDialog().findElements(By.tagName("vaadin-button")).get(1).click();
        verifyDialogClosed();
        Assert.assertEquals("Cancelled...", message.getText());
    }

    @Test
    public void validateClosingFromServerSide() {
        open();

        findElement(By.id("server-side-close-dialog-button")).click();
        verifyDialogOpened();

        executeScript("document.body.click()");
        verifyDialogOpened();

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).perform();
        verifyDialogClosed();

        Assert.assertEquals("Closed from server-side",
                findElement(By.id("server-side-close-dialog-message"))
                        .getText());
    }

    @Test
    public void focusElementOnOpen() {
        open();

        findElement(By.id("focus-dialog-button")).click();

        WebElement element = getDialog().findElement(By.tagName("input"));

        Assert.assertEquals(element, driver.switchTo().activeElement());
    }

    @Test
    public void styleDialogContent() {
        open();

        scrollIntoViewAndClick(
                findElement(By.id("styled-content-dialog-button")));

        WebElement element = getDialog().findElement(By.className("my-style"));

        Assert.assertEquals("rgba(255, 0, 0, 1)", element.getCssValue("color"));
    }

    private DialogElement getDialog() {
        return $(DialogElement.class).withAttribute("opened").first();
    }

    private void verifyDialogClosed() {
        waitForElementNotPresent(By.cssSelector("vaadin-dialog[opened]"));
    }

    private void verifyDialogOpened() {
        waitForElementPresent(By.cssSelector("vaadin-dialog[opened]"));
    }

}

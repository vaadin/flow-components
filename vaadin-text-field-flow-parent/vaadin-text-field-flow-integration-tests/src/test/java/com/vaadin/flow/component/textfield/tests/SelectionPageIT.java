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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for selection API.
 */
@TestPath("vaadin-text-field/selection-test")
public class SelectionPageIT extends AbstractComponentIT {

    private String expectedText = "12345";

    @Before
    public void init() {
        open();
    }

    private String getExpectedText() {
        return expectedText;
    }

    @Test
    public void assertSelectAll() {
        doAssertSelectAll();
    }

    @Test
    public void assertGetSelectionRange() {
        doAssertGetSelectionRange();
    }

    @Test
    public void assertSelectAllTextArea() {
        findElement(By.id("textArea")).click();
        doAssertSelectAll();
    }

    @Test
    public void assertGetSelectionRangeTextArea() {
        findElement(By.id("textArea")).click();
        doAssertGetSelectionRange();
    }

    @Test
    public void assertSelectAllPasswordField() {
        findElement(By.id("passwordField")).click();
        doAssertSelectAll();
    }

    @Test
    public void assertGetSelectionRangePasswordField() {
        findElement(By.id("passwordField")).click();
        doAssertGetSelectionRange();
    }

    @Test
    @Ignore("Selecting all text and setting selection range works, but can't verify programmatically due to browser quirks.")
    public void assertSelectAllIntegerField() {
        findElement(By.id("integerField")).click();
        doAssertSelectAll();
    }

    @Test
    @Ignore("Does not currently work with Chrome.")
    public void assertGetSelectionRangeIntegerField() {
        findElement(By.id("integerField")).click();
        doAssertGetSelectionRange();
    }

    @Test
    public void assertSelectAllBigDecimalField() {
        findElement(By.id("bigDecimalField")).click();
        doAssertSelectAll();
    }

    @Test
    public void assertGetSelectionRangeBigDecimalField() {
        findElement(By.id("bigDecimalField")).click();
        doAssertGetSelectionRange();
    }

    @Test
    @Ignore("Selecting all text and setting selection range works, but can't verify programmatically due to browser quirks.")
    public void assertSelectAllNumberField() {
        findElement(By.id("numberField")).click();
        expectedText = "12345.0";
        doAssertSelectAll();
    }

    @Test
    @Ignore("Does not currently work with Chrome.")
    public void assertGetSelectionRangeNumberField() {
        findElement(By.id("numberField")).click();
        expectedText = "12345.0";
        doAssertGetSelectionRange();
    }

    @Test
    @Ignore("Selecting all text and setting selection range works, but can't verify programmatically due to browser quirks.")
    public void assertSelectAllEmailField() {
        findElement(By.id("emailField")).click();
        expectedText = "test@test.com";
        doAssertSelectAll();
    }

    @Test
    @Ignore("Doesn't currently work properly in Chrome")
    public void assertGetSelectionRangeEmailField() {
        findElement(By.id("emailField")).click();
        expectedText = "test@test.com";
        doAssertGetSelectionRange();
    }

    private void doAssertSelectAll() {
        findElement(By.id("selectall")).click();
        Assert.assertEquals(0, getSelectionStart());
        Assert.assertEquals(getExpectedText().length(), getSelectionEnd());
    }

    private void doAssertGetSelectionRange() {
        findElement(By.id("selection")).click();

        WebElement display = findElement(By.id("display"));
        final int expectedStart = 1;
        final int expectedEnd = 3;
        String expectedText = getExpectedText().substring(expectedStart,
                expectedEnd);
        Assert.assertEquals(
                expectedStart + "," + expectedEnd + ":" + expectedText,
                display.getText());
    }

    private int getSelectionStart() {
        // FIXME
        Long result = (Long) getCommandExecutor()
                .executeScript("return document.activeElement.selectionStart;");
        return result.intValue();
    }

    private int getSelectionEnd() {
        // FIXME
        Long result = (Long) getCommandExecutor()
                .executeScript("return document.activeElement.selectionEnd;");
        return result.intValue();
    }

}

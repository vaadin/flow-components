/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.textfield.testbench.EmailFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@code HasSelection} mixin on text fields.
 */
@TestPath("vaadin-text-field/selection-test")
public class SelectionPageIT extends AbstractComponentIT {

    private TextFieldElement textField;

    @Before
    public void init() {
        open();
        textField = $(TextFieldElement.class).id("text-field");
    }

    @Test
    public void selectAll_selectsEntireValue() {
        clickButton("focus");
        clickButton("select-all");

        Assert.assertEquals(0, selectionStart(textField));
        Assert.assertEquals("Hello world".length(), selectionEnd(textField));
    }

    @Test
    public void setSelectionRange_appliesGivenRange() {
        clickButton("focus");
        clickButton("set-range");

        Assert.assertEquals(2, selectionStart(textField));
        Assert.assertEquals(7, selectionEnd(textField));
    }

    @Test
    public void setCursorPosition_collapsesAtPosition() {
        clickButton("focus");
        clickButton("set-cursor");

        Assert.assertEquals(4, selectionStart(textField));
        Assert.assertEquals(4, selectionEnd(textField));
    }

    @Test
    public void deselect_collapsesAtSelectionEnd() {
        clickButton("focus");
        clickButton("set-range");
        clickButton("deselect");

        Assert.assertEquals(7, selectionStart(textField));
        Assert.assertEquals(7, selectionEnd(textField));
    }

    @Test
    public void selectionSignal_updatesOnProgrammaticChange() {
        clickButton("focus");
        clickButton("set-range");

        waitUntil(driver -> infoText().equals("2-7:5:llo w"));
    }

    @Test
    public void selectionSignal_updatesOnUserSelection() {
        Actions actions = new Actions(getDriver());
        actions.moveToElement(textField).click().perform();
        executeScript("arguments[0].inputElement.setSelectionRange(0, 5);"
                + " arguments[0].inputElement.dispatchEvent(new Event('select'));",
                textField);

        waitUntil(driver -> infoText().equals("0-5:5:Hello"));
    }

    @Test
    public void textArea_inheritsHasSelection() {
        TextAreaElement area = $(TextAreaElement.class).id("text-area");
        clickButton("area-focus");
        clickButton("area-select-range");

        Assert.assertEquals(0, selectionStart(area));
        Assert.assertEquals(5, selectionEnd(area));
    }

    @Test
    public void emailField_inheritsHasSelection() {
        EmailFieldElement email = $(EmailFieldElement.class).id("email-field");
        clickButton("email-focus");
        clickButton("email-select-all");

        Assert.assertEquals(0, selectionStart(email));
        Assert.assertEquals("user@example.com".length(), selectionEnd(email));
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }

    private String infoText() {
        WebElement info = findElement(By.id("selection-info"));
        return info.getText();
    }

    private int selectionStart(TestBenchElement element) {
        return ((Long) executeScript(
                "return arguments[0].inputElement.selectionStart", element))
                .intValue();
    }

    private int selectionEnd(TestBenchElement element) {
        return ((Long) executeScript(
                "return arguments[0].inputElement.selectionEnd", element))
                .intValue();
    }
}

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

import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement;
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
    public void selectAll_focusesAndSelectsEntireValue() {
        clickButton("select-all");

        Assert.assertEquals(0, selectionStart(textField));
        Assert.assertEquals("Hello world".length(), selectionEnd(textField));
        Assert.assertTrue("selectAll should focus the field by default",
                isFocused(textField));
    }

    @Test
    public void setSelectionRange_focusesAndAppliesGivenRange() {
        clickButton("set-range");

        Assert.assertEquals(2, selectionStart(textField));
        Assert.assertEquals(7, selectionEnd(textField));
        Assert.assertTrue("setSelectionRange should focus the field by default",
                isFocused(textField));
    }

    @Test
    public void setCursorPosition_focusesAndCollapsesAtPosition() {
        clickButton("set-cursor");

        Assert.assertEquals(4, selectionStart(textField));
        Assert.assertEquals(4, selectionEnd(textField));
        Assert.assertTrue(isFocused(textField));
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

    /**
     * Reproduces the scenario PR #3194 worried about with the async
     * {@code getSelectionRange(callback)} API: user selects text, clicks a
     * server-side button, and the handler must see the selection that was
     * active at click time. With {@code selectionSignal().peek()} this is a
     * synchronous read and the transform applies to exactly the selected
     * substring.
     */
    @Test
    public void selectionSignal_serverHandlerSeesSelectionAtClickTime() {
        TextAreaElement area = $(TextAreaElement.class).id("text-area");
        // Select "Lorem" (the first word, indices 0-5)
        executeScript("arguments[0].inputElement.focus();"
                + "arguments[0].inputElement.setSelectionRange(0, 5);"
                + "arguments[0].inputElement.dispatchEvent(new Event('select'));",
                area);
        waitUntil(driver -> areaInfoText().startsWith("0-5:5:"));

        clickButton("area-uppercase");

        waitUntil(driver -> findElement(By.id("transform-info")).getText()
                .equals("#1 0-5:Lorem"));
        Assert.assertEquals("LOREM ipsum dolor sit amet", area.getValue());
        Assert.assertEquals(0, selectionStart(area));
        Assert.assertEquals(5, selectionEnd(area));

        // Move the selection to "ipsum" (indices 6-11) and transform again.
        // The second click must read the *new* selection, not the previous
        // one — proving the signal stays current across user actions.
        executeScript("arguments[0].inputElement.setSelectionRange(6, 11);"
                + "arguments[0].inputElement.dispatchEvent(new Event('select'));",
                area);
        waitUntil(driver -> areaInfoText().startsWith("6-11:5:"));

        clickButton("area-uppercase");

        waitUntil(driver -> findElement(By.id("transform-info")).getText()
                .equals("#2 6-11:ipsum"));
        Assert.assertEquals("LOREM IPSUM dolor sit amet", area.getValue());
    }

    @Test
    public void passwordField_implementsHasSelection() {
        PasswordFieldElement password = $(PasswordFieldElement.class)
                .id("password-field");
        clickButton("password-focus");
        clickButton("password-select-all");

        Assert.assertEquals(0, selectionStart(password));
        Assert.assertEquals("secret123".length(), selectionEnd(password));
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }

    private String infoText() {
        WebElement info = findElement(By.id("selection-info"));
        return info.getText();
    }

    private String areaInfoText() {
        WebElement info = findElement(By.id("area-selection-info"));
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

    private boolean isFocused(TestBenchElement element) {
        return Boolean.TRUE.equals(executeScript(
                "return document.activeElement === arguments[0].inputElement"
                        + " || arguments[0].contains(document.activeElement)",
                element));
    }
}

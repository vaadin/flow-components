/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.textfield;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.By;

/**
 * Integration tests for the {@link TextArea}.
 */
public class TextAreaIT extends ComponentDemoTest {

    @Override
    protected String getTestPath() {
        return "/text-area";
    }

    @Test
    public void valueChangeListenerReportsCorrectValues() {
        WebElement textFieldValueDiv = layout
                .findElement(By.id("text-area-value"));
        WebElement textArea = layout
                .findElement(By.id("text-area-with-value-change-listener"));

        textArea.sendKeys("a");
        waitUntilTextsEqual("Text area value changed from '' to 'a'",
                textFieldValueDiv.getText());

        textArea.sendKeys(Keys.BACK_SPACE);
        waitUntilTextsEqual("Text area value changed from 'a' to ''",
                textFieldValueDiv.getText());
    }

    @Test
    public void maxHeight() {
        WebElement textArea = layout
                .findElement(By.id("text-area-with-max-height"));

        IntStream.range(0, 20).forEach(i -> textArea.sendKeys("foobarbaz"));

        Assert.assertEquals(125, textArea.getSize().getHeight());
    }

    @Test
    public void minHeight() throws InterruptedException {
        WebElement textArea = layout
                .findElement(By.id("text-area-with-min-height"));

        IntStream.range(0, 20).forEach(i -> textArea.sendKeys("foobarbaz"));

        Assert.assertNotEquals(125, textArea.getSize().getHeight());

        IntStream.range(0, 20 * "foobarbaz".length())
                .forEach(i -> textArea.sendKeys(Keys.BACK_SPACE));

        Assert.assertEquals(125, textArea.getSize().getHeight());
    }

    @Test
    public void textAreaHasPlaceholder() {
        WebElement textField = layout
                .findElement(By.id("text-area-with-value-change-listener"));
        Assert.assertEquals(textField.getAttribute("placeholder"),
                "placeholder text");
    }

    private void waitUntilTextsEqual(String expected, String actual) {
        waitUntil(driver -> expected.equals(actual));
    }
}

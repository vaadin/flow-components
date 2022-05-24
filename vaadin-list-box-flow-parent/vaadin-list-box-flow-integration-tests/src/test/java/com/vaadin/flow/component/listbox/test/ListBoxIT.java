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
package com.vaadin.flow.component.listbox.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.listbox.testbench.ListBoxElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;

import static org.hamcrest.CoreMatchers.containsString;

/**
 * Integration tests for the {@link ListBoxViewDemoPage}.
 */
@TestPath("vaadin-list-box-demo")
public class ListBoxIT extends AbstractComponentIT {

    private static final String BREAD = "Bread";
    private static final String BUTTER = "Butter";
    private static final String MILK = "Milk";

    private WebElement card;
    private ListBoxElement listBox;
    private List<WebElement> items;

    private WebElement messageLabel;

    @Before
    public void init() {
        open();
    }

    private void init(String cardId) {
        card = findElement(By.id(cardId));
        listBox = $(ListBoxElement.class).context(card).first();
        items = listBox.findElements(By.tagName("vaadin-item"));
    }

    @Test
    public void selection() {
        init("list-box-with-selection");
        messageLabel = findElement(By.tagName("label"));

        final List<String> texts = listBox.getOptions();
        Assert.assertEquals(Arrays.asList(BREAD, BUTTER, MILK), texts);

        listBox.selectByText(texts.get(1));

        assertMessage(null, BUTTER, true);

        card.findElement(By.tagName("button")).click();
        assertMessage(BUTTER, MILK, false);

        listBox.selectByText(texts.get(0));
        assertMessage(MILK, BREAD, true);

        listBox.selectByText(texts.get(2));
        assertMessage(BREAD, MILK, true);

    }

    private void assertMessage(String oldValue, String newValue,
            boolean fromClient) {
        Assert.assertThat(
                "The label should show the old and new values of the ListBox "
                        + "after selection changes",
                messageLabel.getText(), containsString(
                        String.format("from %s to %s", oldValue, newValue)));
        Assert.assertThat(
                "The label should indicate that the event is from "
                        + (fromClient ? "client" : "server"),
                messageLabel.getText(),
                containsString("from client: " + fromClient));
    }

    @Test
    public void componentsBetweenItems() {
        init("list-box-with-components-between");
        List<WebElement> children = listBox.findElements(By.xpath("child::*"));
        Object[] texts = children.stream().map(WebElement::getText).toArray();
        Assert.assertArrayEquals(new Object[] { "Before bread", BREAD, BUTTER,
                "After butter", MILK, "After all the items" }, texts);
    }

    @Test
    public void itemRenderer() {
        init("list-box-with-renderer");
        assertItem(items.get(0), BREAD);
        assertItem(items.get(1), BUTTER);
        assertItem(items.get(2), MILK);
    }

    @Test
    public void disabledListBox() {
        init("disabled-list-box");
        messageLabel = findElement(By.id("message-label"));

        Object[] texts = items.stream().map(WebElement::getText).toArray();
        Assert.assertArrayEquals(new Object[] { BREAD, BUTTER, MILK }, texts);

        executeScript("arguments[0].click(); return true;", items.get(1));
        Assert.assertEquals("Item should not have been selectable", "-",
                messageLabel.getText());

        card.findElement(By.tagName("button")).click();
        assertMessage(null, MILK, false);
    }

    private void assertItem(WebElement item, String itemName) {
        Assert.assertEquals("Items should be enabled in the beginning", null,
                item.getAttribute("disabled"));

        List<WebElement> labels = item.findElements(By.tagName("label"));
        String nameText = labels.get(0).getText();
        String stockText = labels.get(1).getText();

        Assert.assertThat(
                "First label inside the item should contain the name of the item",
                nameText, containsString(itemName));
        Assert.assertThat(
                "Second child inside the item should display the amount of items in stock",
                stockText, containsString("In stock"));

        try {
            int stock = Integer
                    .parseInt(stockText.substring(stockText.length() - 1));
            IntStream.range(0, stock).forEach(
                    $ -> item.findElement(By.tagName("button")).click());
        } catch (NumberFormatException e) {
            Assert.fail("Could not parse integer value from the last symbol "
                    + "of the label text, which should indicate the "
                    + "amount of items in stock.");
        }

        Assert.assertEquals(
                "Item should be disabled after clicking the button enough times",
                "true", item.getAttribute("disabled"));
    }

    @Test
    public void itemItemLabelGenerator() {
        init("list-box-with-item-label-generator");
        assertItemGenerator(items.get(0), BREAD);
        assertItemGenerator(items.get(1), BUTTER);
        assertItemGenerator(items.get(2), MILK);
    }

    private void assertItemGenerator(WebElement item, String itemName) {
        List<WebElement> labels = item.findElements(By.tagName("span"));
        String nameText = labels.get(0).getText();

        Assert.assertThat(
                "First label inside the item should contain the name of the item",
                nameText, containsString(itemName));
    }

}

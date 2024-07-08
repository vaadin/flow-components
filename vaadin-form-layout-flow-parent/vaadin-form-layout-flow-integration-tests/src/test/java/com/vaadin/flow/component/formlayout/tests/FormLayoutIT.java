/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.formlayout.tests;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.formlayout.demo.FormLayoutView;
import com.vaadin.tests.ComponentDemoTest;

/**
 * Integration tests for the {@link FormLayoutView}.
 */
public class FormLayoutIT extends ComponentDemoTest {

    @Before
    public void init() {
        Assert.assertTrue(isElementPresent(By.tagName("vaadin-form-layout")));
    }

    @Test
    /*
     * The test works locally but fails on TC. Disabling it for now.
     *
     * The issue is that the fields do not behave the same (responsive) way when
     * you resize the browser window
     */
    public void custom_responsive_layouting() {
        WebElement firstLayout = layout
                .findElement(By.tagName("vaadin-form-layout"));
        List<WebElement> textFields = firstLayout
                .findElements(By.tagName("vaadin-text-field"));
        Assert.assertEquals(3, textFields.size());

        // 3 columns, all should be horizontally aligned (tolerance of some
        // pixels)
        getDriver().manage().window().setSize(new Dimension(1000, 1000));
        int y2 = textFields.get(2).getLocation().getY();
        int y1 = textFields.get(1).getLocation().getY();
        Assert.assertTrue("All 3 columns should be horizontally aligned y1="
                + y1 + " y2=" + y2, Math.abs(y2 - y1) < 2);

        // window resized, should be in 2 column mode, two below one
        getDriver().manage().window().setSize(new Dimension(620, 620));

        y2 = textFields.get(2).getLocation().getY();
        y1 = textFields.get(1).getLocation().getY();
        Assert.assertTrue(
                "Layout should be in 2 column mode, last field should be below the first two y1="
                        + y1 + " y2=" + y2,
                y2 > y1 + 2);

        // resized to 1 column mode, fields should be arranged below one another
        getDriver().manage().window().setSize(new Dimension(100, 620));
        y1 = textFields.get(1).getLocation().getY();
        int y0 = textFields.get(0).getLocation().getY();
        Assert.assertTrue(
                "Layout should be in 1 column mode, all fields should be below one another y0="
                        + y0 + " y1=" + y1,
                y1 > y0);
    }

    @Test
    public void form_with_binder() {
        // Empty form validation: there is an error
        WebElement info = findElement(By.id("binder-info"));
        WebElement save = findElement(By.id("binder-save"));
        forceClick(save);

        waitUntil(
                driver -> "There are errors: Please specify your email, Please specify your phone, Please add the address"
                        .equals(info.getText()));

        // Fill form: there shouldn't be an error
        setValue("binder-phone", "123-456-789");
        setValue("binder-email", "example@foo.bar");
        setValue("binder-address", "foo bar");
        forceClick(save);

        // waitUntil(driver -> info.getText().startsWith("Saved bean values"));
        Assert.assertTrue(info.getText().contains("phone 123-456-789"));
        Assert.assertTrue(info.getText().contains(", e-mail example@foo.bar"));
        Assert.assertTrue(info.getText().contains(", address foo bar"));

        // Make email address incorrect
        setValue("binder-email", "abc");
        forceClick(save);

        waitUntil(driver -> info.getText().startsWith("There are errors"));
        Assert.assertEquals("There are errors: Incorrect email address",
                info.getText());

        // reset
        forceClick(findElement(By.id("binder-reset")));

        // there's a bug preventing invalid fields from being cleared You need
        // to reset twice. See https://github.com/vaadin/flow-demo/issues/344
        forceClick(findElement(By.id("binder-reset")));

        // Wait for everything to update.
        waitUntil(driver -> info.getText().isEmpty());

        Assert.assertEquals("", getValue("binder-address"));
        Assert.assertEquals("", getValue("binder-phone"));
        Assert.assertEquals("", getValue("binder-email"));
    }

    private void setChecked(String id, boolean checked) {
        WebElement element = findElement(By.id(id));
        executeScript("arguments[0].checked = arguments[1];", element, checked);
    }

    private void setValue(String id, String keys) {
        WebElement element = findElement(By.id(id));
        executeScript("arguments[0].value = arguments[1];", element, keys);
    }

    private String getValue(String id) {
        WebElement element = findElement(By.id(id));
        return String
                .valueOf(executeScript("return arguments[0].value;", element));
    }

    private boolean isChecked(String id) {
        WebElement element = findElement(By.id(id));
        return Boolean.parseBoolean(String.valueOf(
                executeScript("return arguments[0].checked;", element)));
    }

    private void clearInput(String id) {
        WebElement element = findElement(By.id(id));
        executeScript("arguments[0].value = '';", element);
    }

    private void forceClick(WebElement element) {
        executeScript("arguments[0].click();", element);
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-form-layout");
    }
}

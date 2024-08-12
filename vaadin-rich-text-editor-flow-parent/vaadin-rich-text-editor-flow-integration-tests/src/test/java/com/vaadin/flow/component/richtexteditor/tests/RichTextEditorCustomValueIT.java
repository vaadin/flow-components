/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-rich-text-editor/custom-value")
public class RichTextEditorCustomValueIT extends AbstractComponentIT {
    private RichTextEditorElement editor;
    private ButtonElement setAsDeltaValueButton;
    private WebElement customValueInput;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
        setAsDeltaValueButton = $(ButtonElement.class).id("set-as-delta-value");
        customValueInput = $("input").id("custom-value-input");
    }

    @Test
    public void setDeltaWithLeadingTabs_htmlHasLeadingTabs() {
        setCustomDeltaValue("[{\"insert\":\"\\tLeading tab\"}]");
        Assert.assertEquals("<p>\tLeading tab</p>", getEditorHtmlValue());
    }

    @Test
    public void setDeltaWithExtraSpaces_htmlHasExtraSpaces() {
        setCustomDeltaValue("[{\"insert\":\"Extra   spaces\"}]");
        Assert.assertEquals("<p>Extra   spaces</p>", getEditorHtmlValue());
    }

    private String getEditorHtmlValue() {
        return editor.getPropertyString("htmlValue");
    }

    private void setCustomDeltaValue(String value) {
        customValueInput.sendKeys(value);
        customValueInput.sendKeys(Keys.ENTER);
        setAsDeltaValueButton.click();
        waitUntilDeltaValueApplied();
    }

    private void waitUntilDeltaValueApplied() {
        // RTE web component has an internal debouncer that updates the
        // htmlValue only after a 200ms delay after the last change to the
        // editor content.
        waitUntil(driver -> {
            return editor.getCommandExecutor().executeScript(
                    "return arguments[0].__debounceSetValue && !arguments[0].__debounceSetValue.isActive()",
                    editor);
        });
    }
}

/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-rich-text-editor/dialog")
public class RichTextEditorDialogIT extends AbstractComponentIT {
    private RichTextEditorElement editor;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
    }

    @Test
    public void setHtmlValue_correctlyConvertsHtmlToDeltaValue() {
        // Wait until delta value has been updated
        waitUntil(driver -> !editor.getPropertyString("value").isEmpty());

        String expectedHtml = "<ul><li>Item 1</li><li>Item 2</li></ul>";
        String expectedDelta = "[{\"insert\":\"Item 1\"},{\"attributes\":{\"list\":\"bullet\"},\"insert\":\"\\n\"},{\"insert\":\"Item 2\"},{\"attributes\":{\"list\":\"bullet\"},\"insert\":\"\\n\"}]";

        Assert.assertEquals(expectedHtml,
                editor.getEditor().getProperty("innerHTML"));
        Assert.assertEquals(expectedDelta, editor.getProperty("value"));
    }
}

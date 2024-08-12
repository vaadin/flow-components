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

@TestPath("vaadin-rich-text-editor/detach-reattach")
public class RichTextEditorDetachReattachIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void detach_reattach_htmlValueInitialized() {
        assertHtmlValue();

        $("button").id("detach").click();
        $("button").id("attach").click();

        assertHtmlValue();
    }

    private void assertHtmlValue() {
        RichTextEditorElement editor = $(RichTextEditorElement.class).first();

        Assert.assertEquals("<h1>foo</h1>",
                editor.getEditor().getProperty("innerHTML"));
    }
}

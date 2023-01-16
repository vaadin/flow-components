package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-rich-text-editor/detach-reattach")
public class RichTextEditorDetachReattachIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void detach_reattach_htmlValueInitialized() {
        $("button").id("detach").click();
        $("button").id("attach").click();

        RichTextEditorElement editor = $(RichTextEditorElement.class).first();

        Assert.assertEquals("<h1>foo</h1>",
                editor.getEditor().getProperty("innerHTML"));
    }
}

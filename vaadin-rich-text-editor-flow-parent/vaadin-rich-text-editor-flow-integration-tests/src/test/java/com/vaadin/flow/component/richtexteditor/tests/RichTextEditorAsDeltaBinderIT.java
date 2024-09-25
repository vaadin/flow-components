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
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-rich-text-editor/as-delta-binder")
public class RichTextEditorAsDeltaBinderIT extends AbstractComponentIT {
    private RichTextEditorElement editor;
    private TestBenchElement binderError;
    private TestBenchElement beanValue;
    private TestBenchElement writeBean;
    private TestBenchElement readBean;
    private TestBenchElement reset;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
        binderError = $(TestBenchElement.class).id("binder-error");
        beanValue = $(TestBenchElement.class).id("bean-value");
        writeBean = $(TestBenchElement.class).id("write-bean");
        readBean = $(TestBenchElement.class).id("read-bean");
        reset = $(TestBenchElement.class).id("reset");
    }

    @Test
    public void emptyEditor_writeBean_hasError() {
        writeBean.click();

        waitUntil(driver -> !binderError.getText().isEmpty());

        Assert.assertEquals("true", binderError.getText());
        Assert.assertEquals("", beanValue.getText());
    }

    @Test
    public void enterText_writeBean_noError() {
        writeAndBlur("foo");
        writeBean.click();

        waitUntil(driver -> !binderError.getText().isEmpty());

        Assert.assertEquals("false", binderError.getText());
        Assert.assertEquals("[{\"insert\":\"foo\\n\"}]", beanValue.getText());
    }

    @Test
    public void readBean_editorUpdated() {
        readBean.click();

        Assert.assertEquals("<p>foo</p>",
                editor.getEditor().getProperty("innerHTML"));
    }

    @Test
    public void enterText_writeBean_reset_emptyEditor() {
        writeAndBlur("foo");
        writeBean.click();

        waitUntil(driver -> !binderError.getText().isEmpty());

        reset.click();

        Assert.assertEquals("<p><br></p>",
                editor.getEditor().getProperty("innerHTML"));
    }

    private void writeAndBlur(CharSequence... keysToSend) {
        editor.getEditor().sendKeys(keysToSend);
        editor.getEditor().dispatchEvent("focusout");
        getCommandExecutor().waitForVaadin();
    }
}

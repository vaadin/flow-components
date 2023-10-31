package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-rich-text-editor/sanitization")
public class RichTextEditorSanitizationIT extends AbstractComponentIT {
    private RichTextEditorElement editor;
    private TestBenchElement setUnsanitizedValue;
    private TestBenchElement valueOutput;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
        setUnsanitizedValue = $(TestBenchElement.class)
                .id("set-unsanitized-value");
        valueOutput = $(TestBenchElement.class).id("value-output");
    }

    @Test
    public void setUnsanitizedValueOnServer_sanitizedClientValue() {
        setUnsanitizedValue.click();

        String expectedValue = "<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4v5ThPwAG7wKklwQ/bwAAAABJRU5ErkJggg==\"></p>";
        Assert.assertEquals(expectedValue,
                editor.getEditor().getProperty("innerHTML"));
    }

    @Test
    public void setUnsanitizedValueOnClient_sanitizedServerValue() {
        // Img element requires a src in order to not be discarded by Quill.
        // Server-side sanitization requires a base64 encoded URL
        String value = "<img onload=\"console.log('load')\" onerror=\"console.log('error')\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4v5ThPwAG7wKklwQ/bwAAAABJRU5ErkJggg==\">"
                + "<script>console.log('script')</script>";
        editor.getEditor().setProperty("innerHTML", value);

        // Wait for the web component to update the htmlValue property
        waitUntil(driver -> editor.getPropertyString("htmlValue")
                .contains("img"));

        editor.dispatchEvent("change");

        String expectedValue = "<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4v5ThPwAG7wKklwQ/bwAAAABJRU5ErkJggg==\"></p><p><br></p>";
        Assert.assertEquals(expectedValue, valueOutput.getText());
    }
}

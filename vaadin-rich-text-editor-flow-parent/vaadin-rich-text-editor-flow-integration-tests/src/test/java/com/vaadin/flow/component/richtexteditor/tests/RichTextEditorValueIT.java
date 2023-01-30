package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-rich-text-editor/value")
public class RichTextEditorValueIT extends AbstractComponentIT {
    private RichTextEditorElement editor;
    private TestBenchElement valueOutput;
    private TestBenchElement asHtmlValueOutput;
    private TestBenchElement asDeltaValueOutput;
    private TestBenchElement setValue;
    private TestBenchElement setAsHtmlValue;
    private TestBenchElement setAsDeltaValue;
    private TestBenchElement setupEditorWithInitialDeltaValue;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
        valueOutput = $("span").id("value-output");
        asHtmlValueOutput = $("span").id("as-html-value-output");
        asDeltaValueOutput = $("span").id("as-delta-value-output");
        setValue = $("button").id("set-value");
        setAsHtmlValue = $("button").id("set-as-html-value");
        setAsDeltaValue = $("button").id("set-as-delta-value");
        setupEditorWithInitialDeltaValue = $("button")
                .id("setup-editor-with-initial-delta-value");
    }

    @Test
    public void writeText_valuesUpdated() {
        writeAndBlur("Foo");

        ValueChangeEventData valueChangeEvent = new ValueChangeEventData(
                valueOutput);
        ValueChangeEventData asHtmlValueChangeEvent = new ValueChangeEventData(
                asHtmlValueOutput);
        ValueChangeEventData asDeltaValueChangeEvent = new ValueChangeEventData(
                asDeltaValueOutput);

        Assert.assertEquals("<p>Foo</p>", valueChangeEvent.value);
        Assert.assertEquals("<p>Foo</p>", asHtmlValueChangeEvent.value);
        Assert.assertEquals("[{\"insert\":\"Foo\\n\"}]",
                asDeltaValueChangeEvent.value);

        Assert.assertTrue(valueChangeEvent.isFromClient);
        Assert.assertTrue(asHtmlValueChangeEvent.isFromClient);
        Assert.assertTrue(asDeltaValueChangeEvent.isFromClient);
    }

    @Test
    public void setValue_valuesUpdated() {
        setValue.click();

        // All values are only synced after another roundtrip
        waitUntil(driver -> !asDeltaValueOutput.getText().isEmpty());

        ValueChangeEventData valueChangeEvent = new ValueChangeEventData(
                valueOutput);
        ValueChangeEventData asHtmlValueChangeEvent = new ValueChangeEventData(
                asHtmlValueOutput);
        ValueChangeEventData asDeltaValueChangeEvent = new ValueChangeEventData(
                asDeltaValueOutput);

        Assert.assertEquals("<h1>value</h1>", valueChangeEvent.value);
        Assert.assertEquals("<h1>value</h1>", asHtmlValueChangeEvent.value);
        Assert.assertEquals(
                "[{\"insert\":\"value\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]",
                asDeltaValueChangeEvent.value);

        Assert.assertFalse(valueChangeEvent.isFromClient);
        Assert.assertFalse(asHtmlValueChangeEvent.isFromClient);
        Assert.assertFalse(asDeltaValueChangeEvent.isFromClient);
    }

    @Test
    public void setAsHtmlValue_valuesUpdated() {
        setAsHtmlValue.click();

        // All values are only synced after another roundtrip
        waitUntil(driver -> !asDeltaValueOutput.getText().isEmpty());

        ValueChangeEventData valueChangeEvent = new ValueChangeEventData(
                valueOutput);
        ValueChangeEventData asHtmlValueChangeEvent = new ValueChangeEventData(
                asHtmlValueOutput);
        ValueChangeEventData asDeltaValueChangeEvent = new ValueChangeEventData(
                asDeltaValueOutput);

        Assert.assertEquals("<h1>as-html-value</h1>", valueChangeEvent.value);
        Assert.assertEquals("<h1>as-html-value</h1>",
                asHtmlValueChangeEvent.value);
        Assert.assertEquals(
                "[{\"insert\":\"as-html-value\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]",
                asDeltaValueChangeEvent.value);

        Assert.assertFalse(valueChangeEvent.isFromClient);
        Assert.assertFalse(asHtmlValueChangeEvent.isFromClient);
        Assert.assertFalse(asDeltaValueChangeEvent.isFromClient);
    }

    @Test
    public void setAsDeltaValue_valuesUpdated() {
        setAsDeltaValue.click();

        // All values are only synced after another roundtrip
        waitUntil(driver -> !asDeltaValueOutput.getText().isEmpty());

        ValueChangeEventData valueChangeEvent = new ValueChangeEventData(
                valueOutput);
        ValueChangeEventData asHtmlValueChangeEvent = new ValueChangeEventData(
                asHtmlValueOutput);
        ValueChangeEventData asDeltaValueChangeEvent = new ValueChangeEventData(
                asDeltaValueOutput);

        Assert.assertEquals("<h1>as-delta-value</h1>", valueChangeEvent.value);
        Assert.assertEquals("<h1>as-delta-value</h1>",
                asHtmlValueChangeEvent.value);
        Assert.assertEquals(
                "[{\"insert\":\"as-delta-value\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]",
                asDeltaValueChangeEvent.value);

        Assert.assertFalse(valueChangeEvent.isFromClient);
        Assert.assertFalse(asHtmlValueChangeEvent.isFromClient);
        Assert.assertFalse(asDeltaValueChangeEvent.isFromClient);
    }

    @Test
    public void multipleClientUpdates_oldDeltaValueUpdated() {
        writeAndBlur("foo");
        writeAndBlur("bar");

        ValueChangeEventData asDeltaValueChangeEvent = new ValueChangeEventData(
                asDeltaValueOutput);

        Assert.assertEquals("[{\"insert\":\"foobar\\n\"}]",
                asDeltaValueChangeEvent.value);
        Assert.assertEquals("[{\"insert\":\"foo\\n\"}]",
                asDeltaValueChangeEvent.oldValue);
    }

    @Test
    public void multipleServerUpdates_oldDeltaValueUpdated() {
        // First update
        setValue.click();
        // Wait for delta sync
        waitUntil(driver -> !asDeltaValueOutput.getText().isEmpty());
        // Clean output so that we can wait until it is filled again
        asDeltaValueOutput.setProperty("textContent", "");

        // Second update
        setAsDeltaValue.click();
        // Wait for delta sync
        waitUntil(driver -> !asDeltaValueOutput.getText().isEmpty());

        ValueChangeEventData asDeltaValueChangeEvent = new ValueChangeEventData(
                asDeltaValueOutput);

        Assert.assertEquals(
                "[{\"insert\":\"value\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]",
                asDeltaValueChangeEvent.oldValue);
    }

    @Test
    public void setInitialDeltaValue_initialValueChangeEvent() {
        setupEditorWithInitialDeltaValue.click();

        valueOutput = $("span").id("value-output");
        waitUntil(driver -> !valueOutput.getText().isEmpty());

        ValueChangeEventData valueChangeEvent = new ValueChangeEventData(
                valueOutput);

        Assert.assertEquals("<h1>initial-value</h1>", valueChangeEvent.value);
    }

    private void writeAndBlur(CharSequence... keysToSend) {
        editor.getEditor().sendKeys(keysToSend);
        editor.getEditor().dispatchEvent("focusout");
        getCommandExecutor().waitForVaadin();
    }

    private static class ValueChangeEventData {
        private final String value;
        private final String oldValue;
        private final boolean isFromClient;

        public ValueChangeEventData(TestBenchElement outputElement) {
            String[] parts = outputElement.getText().split("\\|");
            this.value = parts[0];
            this.oldValue = parts[1];
            this.isFromClient = Boolean.parseBoolean(parts[2]);
        }
    }
}

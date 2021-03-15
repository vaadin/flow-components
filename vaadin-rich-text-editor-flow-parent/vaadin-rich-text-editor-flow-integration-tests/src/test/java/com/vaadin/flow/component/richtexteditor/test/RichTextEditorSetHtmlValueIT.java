package com.vaadin.flow.component.richtexteditor.test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath(value = "vaadin-rich-text-editor/set-html-value")
public class RichTextEditorSetHtmlValueIT extends AbstractComponentIT {

    private TestBenchElement rteValue;
    private TestBenchElement rteHtmlValue;

    @Before
    public void init() {
        open();
        rteValue = $(TestBenchElement.class).id("rteValue");
        rteHtmlValue = $(TestBenchElement.class).id("rteHtmlValue");
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad() {
        final String expectedValue = "[{\"insert\":\"Test\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]";
        waitForRteValue(expectedValue);
        Assert.assertEquals("<h1>Test</h1>", rteHtmlValue.getText());
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick() {
        $(TestBenchElement.class).id("setValueButton").click();
        final String expectedValue = "[{\"insert\":\"Test 1\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]";
        waitForRteValue(expectedValue);
        Assert.assertEquals("<h1>Test 1</h1>", rteHtmlValue.getText());
    }

    private void waitForRteValue(String value) {
        waitUntil(driver -> value.equals(rteValue.getText()));
    }
}

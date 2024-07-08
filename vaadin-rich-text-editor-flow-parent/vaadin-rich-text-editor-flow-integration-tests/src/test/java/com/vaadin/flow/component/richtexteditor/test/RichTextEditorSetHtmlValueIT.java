/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.test;

import com.vaadin.flow.data.value.ValueChangeMode;
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
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad_default_mode() {
        doSetHtmlValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick_default_mode() {
        doSetHtmlValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad_eager() {
        setRteValue(ValueChangeMode.EAGER);
        doSetHtmlValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick_eager() {
        setRteValue(ValueChangeMode.EAGER);
        doSetHtmlValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad_lazy() {
        setRteValue(ValueChangeMode.LAZY);
        doSetHtmlValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick_lazy() {
        setRteValue(ValueChangeMode.LAZY);
        doSetHtmlValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad_timeout() {
        setRteValue(ValueChangeMode.TIMEOUT);
        doSetHtmlValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick_timeout() {
        setRteValue(ValueChangeMode.TIMEOUT);
        doSetHtmlValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad_blur() {
        setRteValue(ValueChangeMode.ON_BLUR);
        doSetHtmlValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick_blur() {
        setRteValue(ValueChangeMode.ON_BLUR);
        doSetHtmlValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueBeforeLoad_change() {
        setRteValue(ValueChangeMode.ON_CHANGE);
        doSetHtmlValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setHtmlValueOnServerSideUpdatesValueOnClick_change() {
        setRteValue(ValueChangeMode.ON_CHANGE);
        doSetHtmlValueOnServerSideUpdatesValueOnClick();
    }

    private void doSetHtmlValueOnServerSideUpdatesValueBeforeLoad() {
        final String expectedValue = "[{\"insert\":\"Test\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]";
        waitForRteValue(expectedValue);
        Assert.assertEquals("<h1>Test</h1>", rteHtmlValue.getText());
    }

    private void doSetHtmlValueOnServerSideUpdatesValueOnClick() {
        $(TestBenchElement.class).id("setValueButton").click();
        final String expectedValue = "[{\"insert\":\"Test 1\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]";
        waitForRteValue(expectedValue);
        Assert.assertEquals("<h1>Test 1</h1>", rteHtmlValue.getText());
    }

    private void setRteValue(ValueChangeMode valueChangeMode) {
        final String id = String.format("setChangeMode_%s",
                valueChangeMode.toString());
        $(TestBenchElement.class).id(id).click();
        Assert.assertEquals(valueChangeMode.toString(),
                $(TestBenchElement.class).id("rteValueChangeMode").getText());
    }

    private void waitForRteValue(String value) {
        waitUntil(driver -> value.equals(rteValue.getText()));
    }
}

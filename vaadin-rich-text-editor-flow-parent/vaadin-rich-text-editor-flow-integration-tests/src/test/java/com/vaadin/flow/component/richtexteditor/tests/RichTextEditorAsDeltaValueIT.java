package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath(value = "vaadin-rich-text-editor/set-delta-value")
public class RichTextEditorAsDeltaValueIT extends AbstractComponentIT {

    private TestBenchElement rteValue;
    private TestBenchElement rteDeltaValue;

    @Before
    public void init() {
        open();
        rteValue = $(TestBenchElement.class).id("rteValue");
        rteDeltaValue = $(TestBenchElement.class).id("rteDeltaValue");
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueBeforeLoad_default_mode() {
        doSetDeltaValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueOnClick_default_mode() {
        doSetDeltaValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueBeforeLoad_eager() {
        setRteValue(ValueChangeMode.EAGER);
        doSetDeltaValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueOnClick_eager() {
        setRteValue(ValueChangeMode.EAGER);
        doSetDeltaValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueBeforeLoad_lazy() {
        setRteValue(ValueChangeMode.LAZY);
        doSetDeltaValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueOnClick_lazy() {
        setRteValue(ValueChangeMode.LAZY);
        doSetDeltaValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueBeforeLoad_timeout() {
        setRteValue(ValueChangeMode.TIMEOUT);
        doSetDeltaValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueOnClick_timeout() {
        setRteValue(ValueChangeMode.TIMEOUT);
        doSetDeltaValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueBeforeLoad_blur() {
        setRteValue(ValueChangeMode.ON_BLUR);
        doSetDeltaValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueOnClick_blur() {
        setRteValue(ValueChangeMode.ON_BLUR);
        doSetDeltaValueOnServerSideUpdatesValueOnClick();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueBeforeLoad_change() {
        setRteValue(ValueChangeMode.ON_CHANGE);
        doSetDeltaValueOnServerSideUpdatesValueBeforeLoad();
    }

    @Test
    public void setDeltaValueOnServerSideUpdatesValueOnClick_change() {
        setRteValue(ValueChangeMode.ON_CHANGE);
        doSetDeltaValueOnServerSideUpdatesValueOnClick();
    }

    private void doSetDeltaValueOnServerSideUpdatesValueBeforeLoad() {
        waitForRteValue("<h1>Test</h1>");
        final String expectedDeltaValue = "[{\"insert\":\"Test\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]";
        Assert.assertEquals(expectedDeltaValue, rteDeltaValue.getText());
    }

    private void doSetDeltaValueOnServerSideUpdatesValueOnClick() {
        $(TestBenchElement.class).id("setValueButton").click();
        waitForRteValue("<h1>Test 1</h1>");
        final String expectedDeltaValue = "[{\"insert\":\"Test 1\"},{\"attributes\":{\"header\":1},\"insert\":\"\\n\"}]";
        Assert.assertEquals(expectedDeltaValue, rteDeltaValue.getText());
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

package com.vaadin.flow.component.richtexteditor;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;

/**
 * Tests for the {@link RichTextEditor}.
 */
public class RichTextEditorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        RichTextEditor rte = new RichTextEditor();
        assertEquals("Value should be an empty string", "",
                rte.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        rte.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals(rte.getEmptyValue(),
                rte.getElement().getProperty("value"));
    }
}

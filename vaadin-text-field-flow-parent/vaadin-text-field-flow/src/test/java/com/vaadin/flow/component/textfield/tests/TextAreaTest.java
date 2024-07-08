/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.textfield.TextArea;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link TextArea}.
 */
public class TextAreaTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        TextArea textArea = new TextArea();
        assertEquals("Value should be an empty string", "",
                textArea.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        textArea.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        TextArea textArea = new TextArea();
        assertEquals(textArea.getEmptyValue(),
                textArea.getElement().getProperty("value"));
    }

    @Test
    public void clearButtonVisiblePropertyValue() {
        TextArea textArea = new TextArea();

        assertClearButtonPropertyValueEquals(textArea, true);
        assertClearButtonPropertyValueEquals(textArea, false);
    }

    public void assertClearButtonPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setClearButtonVisible(value);
        assertEquals(value, textArea.isClearButtonVisible());
        assertEquals(textArea.isClearButtonVisible(),
                textArea.getElement().getProperty("clearButtonVisible", value));
    }

    @Test
    public void autoselectPropertyValue() {
        TextArea textArea = new TextArea();

        assertAutoselectPropertyValueEquals(textArea, true);
        assertAutoselectPropertyValueEquals(textArea, false);
    }

    @Test
    public void patternPropertyValue() {
        String testPattern = "TEST";
        TextArea textArea = new TextArea();

        textArea.setPattern(testPattern);
        assertEquals(testPattern, textArea.getPattern());
        assertEquals(testPattern,
                textArea.getElement().getProperty("pattern", ""));
    }

    public void assertAutoselectPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setAutoselect(value);
        assertEquals(value, textArea.isAutoselect());
        assertEquals(textArea.isAutoselect(),
                textArea.getElement().getProperty("autoselect", value));
    }
}

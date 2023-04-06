
package com.vaadin.flow.component.textfield.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.textfield.TextArea;

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
    public void elementHasValue_wrapIntoTextField_propertyIsNotSetToInitialValue() {
        ComponentFromTest
                .elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
                        "foo", TextArea.class);
    }

    public void assertAutoselectPropertyValueEquals(TextArea textArea,
            Boolean value) {
        textArea.setAutoselect(value);
        assertEquals(value, textArea.isAutoselect());
        assertEquals(textArea.isAutoselect(),
                textArea.getElement().getProperty("autoselect", value));
    }
}

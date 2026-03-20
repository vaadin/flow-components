/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ArrayNode;

/**
 * Tests for the {@link RichTextEditor}.
 */
class RichTextEditorTest {
    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @Test
    void setValueNull() {
        RichTextEditor rte = new RichTextEditor();
        assertEquals("", rte.getValue(), "Value should be an empty string");

        NullPointerException ex = Assertions.assertThrows(
                NullPointerException.class, () -> rte.setValue(null));
        Assertions.assertEquals("Null value is not supported", ex.getMessage());
    }

    @Test
    void initialValuePropertyValue() {
        RichTextEditor rte = new RichTextEditor();
        Assertions.assertEquals(rte.getEmptyValue(),
                rte.getElement().getProperty("htmlValue"));
    }

    @Test
    void initialAsHtmlValue() {
        RichTextEditor rte = new RichTextEditor();
        Assertions.assertEquals(rte.asHtml().getEmptyValue(),
                rte.asHtml().getValue());
    }

    @Test
    void initialAsDeltaValue() {
        RichTextEditor rte = new RichTextEditor();
        Assertions.assertEquals(rte.asDelta().getEmptyValue(),
                rte.asDelta().getValue());
        Assertions.assertEquals(rte.asDelta().getEmptyValue(),
                rte.getElement().getProperty("value"));
    }

    @Test
    void setValueStartingWithJsonArray_throws() {
        RichTextEditor rte = new RichTextEditor();

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> rte.setValue("[{\"insert\":\"Foo\"}]"));
        Assertions.assertTrue(ex.getMessage()
                .contains("The value starts with either '[' or '{'"));
    }

    @Test
    void setValueStartingWithJsonObject_throws() {
        RichTextEditor rte = new RichTextEditor();

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> rte.setValue("{\"insert\":\"Foo\"}"));
        Assertions.assertTrue(ex.getMessage()
                .contains("The value starts with either '[' or '{'"));
    }

    // asHtml

    @Test
    void asHtml_setValue_getValue() {
        HasValue<ValueChangeEvent<String>, String> rteAsHtml = new RichTextEditor()
                .asHtml();
        String htmlValue = "<strong>Foo</strong>";
        rteAsHtml.setValue(htmlValue);
        Assertions.assertEquals(htmlValue, rteAsHtml.getValue(),
                "Should get the same value as it was set");
    }

    @Test
    void asHtml_setReadOnly_rteIsReadonly() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> rteAsHtml = rte.asHtml();
        rteAsHtml.setReadOnly(true);
        Assertions.assertTrue(rte.isReadOnly(),
                "Should be possible to set readonly on asHtml");
    }

    @Test
    void asHtml_setRequiredIndicatorVisible_rteRequiredIndicatorVisible() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> rteAsHtml = rte.asHtml();
        rteAsHtml.setRequiredIndicatorVisible(true);
        Assertions.assertTrue(rte.isRequiredIndicatorVisible(),
                "Should be possible to set required indicator to be visible on asHtml");
    }

    @Test
    void asHtml_setValueStartingWithJson_noException() {
        RichTextEditor rte = new RichTextEditor();

        String value = "[{\"insert\":\"Foo\"}]";
        rte.asHtml().setValue(value);
        Assertions.assertEquals(value, rte.getValue());

        value = "{\"insert\":\"Foo\"}";
        rte.asHtml().setValue(value);
        Assertions.assertEquals(value, rte.getValue());
    }

    // asDelta

    @Test
    void asDelta_setValue_getValue() {
        String deltaValue = "[{\"insert\":\"Foo\"}]";
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();
        asDelta.setValue(deltaValue);

        Assertions.assertEquals(deltaValue,
                rte.getElement().getProperty("value"),
                "Should set value property");
        Assertions.assertEquals(deltaValue, asDelta.getValue(),
                "Should get the same value as it was set");
    }

    @Test
    void asDelta_setNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new RichTextEditor().asDelta().setValue(null));
    }

    @Test
    void asDelta_setReadOnly_rteIsReadonly() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();
        asDelta.setReadOnly(true);
        Assertions.assertTrue(rte.isReadOnly(),
                "Should be possible to set readonly on asDelta");
    }

    @Test
    void asDelta_setRequiredIndicatorVisible_rteRequiredIndicatorVisible() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();
        asDelta.setRequiredIndicatorVisible(true);
        Assertions.assertTrue(rte.isRequiredIndicatorVisible(),
                "Should be possible to set required indicator to be visible on asDelta");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void asDelta_addChangeListener() {
        String deltaValue = "[{\"insert\":\"Foo\"}]";
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();

        HasValue.ValueChangeListener valueChangeListenerMock = Mockito
                .mock(HasValue.ValueChangeListener.class);
        asDelta.addValueChangeListener(valueChangeListenerMock);

        rte.asDelta().setValue(deltaValue);
        Mockito.verify(valueChangeListenerMock, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void asDelta_noChangeEventForSameValue() {
        String deltaValue = "[{\"insert\":\"Foo\"}]";
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();

        HasValue.ValueChangeListener valueChangeListenerMock = Mockito
                .mock(HasValue.ValueChangeListener.class);
        asDelta.addValueChangeListener(valueChangeListenerMock);

        // Set a value
        asDelta.setValue(deltaValue);

        // Set same value again
        asDelta.setValue(deltaValue);

        // Change listener should only have been called once
        Mockito.verify(valueChangeListenerMock, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @Test
    void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-rich-text-editor");

        element.setProperty("value", "foo");

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(ui.getService().getInstantiator())
                .thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(RichTextEditor.class))
                .thenAnswer(invocation -> new RichTextEditor());

        RichTextEditor field = Component.from(element, RichTextEditor.class);
        Assertions.assertEquals("foo",
                field.getElement().getPropertyRaw("value"));
    }

    @Test
    void setColorOptions_propertyIsUpdated() {
        RichTextEditor rte = new RichTextEditor();
        rte.setColorOptions(
                List.of("#000000", "#0066cc", "#008a00", "#e60000"));
        ArrayNode jsonArray = (ArrayNode) rte.getElement()
                .getPropertyRaw("colorOptions");
        Assertions.assertEquals(4, jsonArray.size());
        Assertions.assertEquals("#000000", jsonArray.get(0).asString());
        Assertions.assertEquals("#0066cc", jsonArray.get(1).asString());
        Assertions.assertEquals("#008a00", jsonArray.get(2).asString());
        Assertions.assertEquals("#e60000", jsonArray.get(3).asString());
    }

    @Test
    void setColorOptions_getColorOptions() {
        RichTextEditor rte = new RichTextEditor();
        rte.setColorOptions(
                List.of("#000000", "#0066cc", "#008a00", "#e60000"));
        List<String> options = rte.getColorOptions();
        Assertions.assertEquals(4, options.size());
        Assertions.assertEquals("#000000", options.get(0));
        Assertions.assertEquals("#0066cc", options.get(1));
        Assertions.assertEquals("#008a00", options.get(2));
        Assertions.assertEquals("#e60000", options.get(3));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(RichTextEditor.class));
    }
}

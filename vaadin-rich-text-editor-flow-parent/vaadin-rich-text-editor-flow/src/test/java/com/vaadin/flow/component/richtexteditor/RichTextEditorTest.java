package com.vaadin.flow.component.richtexteditor;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

/**
 * Tests for the {@link RichTextEditor}.
 */
public class RichTextEditorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        RichTextEditor rte = new RichTextEditor();
        assertEquals("Value should be an empty string", "", rte.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        rte.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals(rte.getEmptyValue(),
                rte.getElement().getProperty("htmlValue"));
    }

    @Test
    public void initialAsHtmlValue() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals(rte.asHtml().getEmptyValue(),
                rte.asHtml().getValue());
    }

    @Test
    public void initialAsDeltaValue() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals(rte.asDelta().getEmptyValue(),
                rte.asDelta().getValue());
        Assert.assertEquals(rte.asDelta().getEmptyValue(),
                rte.getElement().getProperty("value"));
    }

    @Test
    public void setValueStartingWithJsonArray_throws() {
        RichTextEditor rte = new RichTextEditor();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The value starts with either '[' or '{'");

        rte.setValue("[{\"insert\":\"Foo\"}]");
    }

    @Test
    public void setValueStartingWithJsonObject_throws() {
        RichTextEditor rte = new RichTextEditor();

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The value starts with either '[' or '{'");

        rte.setValue("{\"insert\":\"Foo\"}");
    }

    // asHtml

    @Test
    public void asHtml_setValue_getValue() {
        HasValue<ValueChangeEvent<String>, String> rteAsHtml = new RichTextEditor()
                .asHtml();
        String htmlValue = "<strong>Foo</strong>";
        rteAsHtml.setValue(htmlValue);
        Assert.assertEquals("Should get the same value as it was set",
                htmlValue, rteAsHtml.getValue());
    }

    @Test
    public void asHtml_setReadOnly_rteIsReadonly() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> rteAsHtml = rte.asHtml();
        rteAsHtml.setReadOnly(true);
        Assert.assertTrue("Should be possible to set readonly on asHtml",
                rte.isReadOnly());
    }

    @Test
    public void asHtml_setRequiredIndicatorVisible_rteRequiredIndicatorVisible() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> rteAsHtml = rte.asHtml();
        rteAsHtml.setRequiredIndicatorVisible(true);
        Assert.assertTrue(
                "Should be possible to set required indicator to be visible on asHtml",
                rte.isRequiredIndicatorVisible());
    }

    @Test
    public void asHtml_setValueStartingWithJson_noException() {
        RichTextEditor rte = new RichTextEditor();

        String value = "[{\"insert\":\"Foo\"}]";
        rte.asHtml().setValue(value);
        Assert.assertEquals(value, rte.getValue());

        value = "{\"insert\":\"Foo\"}";
        rte.asHtml().setValue(value);
        Assert.assertEquals(value, rte.getValue());
    }

    // asDelta

    @Test
    public void asDelta_setValue_getValue() {
        String deltaValue = "[{\"insert\":\"Foo\"}]";
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();
        asDelta.setValue(deltaValue);

        Assert.assertEquals("Should set value property", deltaValue,
                rte.getElement().getProperty("value"));
        Assert.assertEquals("Should get the same value as it was set",
                deltaValue, asDelta.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void asDelta_setNull_throws() {
        new RichTextEditor().asDelta().setValue(null);
    }

    @Test
    public void asDelta_setReadOnly_rteIsReadonly() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();
        asDelta.setReadOnly(true);
        Assert.assertTrue("Should be possible to set readonly on asDelta",
                rte.isReadOnly());
    }

    @Test
    public void asDelta_setRequiredIndicatorVisible_rteRequiredIndicatorVisible() {
        RichTextEditor rte = new RichTextEditor();
        HasValue<ValueChangeEvent<String>, String> asDelta = rte.asDelta();
        asDelta.setRequiredIndicatorVisible(true);
        Assert.assertTrue(
                "Should be possible to set required indicator to be visible on asDelta",
                rte.isRequiredIndicatorVisible());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void asDelta_addChangeListener() {
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
    public void asDelta_noChangeEventForSameValue() {
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
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-rich-text-editor");

        element.setProperty("value", "foo");
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(RichTextEditor.class))
                .thenAnswer(invocation -> new RichTextEditor());

        RichTextEditor field = Component.from(element, RichTextEditor.class);
        Assert.assertEquals("foo", field.getElement().getPropertyRaw("value"));
    }
}

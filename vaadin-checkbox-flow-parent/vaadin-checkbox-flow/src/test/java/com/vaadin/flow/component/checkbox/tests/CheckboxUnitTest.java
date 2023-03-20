
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class CheckboxUnitTest {

    @Test
    public void initialValue() {
        Checkbox checkbox = new Checkbox();
        Assert.assertFalse(checkbox.getValue());

        checkbox = new Checkbox(true);
        Assert.assertTrue(checkbox.getValue());

        checkbox = new Checkbox(false);
        Assert.assertFalse(checkbox.getValue());
    }

    @Test
    public void testIndeterminate() {
        Checkbox checkbox = new Checkbox();
        Assert.assertFalse(checkbox.isIndeterminate());

        checkbox = new Checkbox(true);
        Assert.assertFalse(checkbox.isIndeterminate());

        checkbox.setIndeterminate(true);
        Assert.assertTrue(checkbox.getValue());
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.setValue(true);
        Assert.assertTrue(checkbox.getValue());
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.setValue(false);
        Assert.assertFalse(checkbox.getValue());
        Assert.assertTrue(checkbox.isIndeterminate());

        checkbox.setIndeterminate(false);
        Assert.assertFalse(checkbox.getValue());
        Assert.assertFalse(checkbox.isIndeterminate());
    }

    @Test
    public void labelAndInitialValueCtor() {
        Checkbox checkbox = new Checkbox("foo", true);
        Assert.assertTrue(checkbox.getValue());
        Assert.assertEquals("foo", checkbox.getLabel());

        checkbox = new Checkbox("foo", false);
        Assert.assertFalse(checkbox.getValue());
        Assert.assertEquals("foo", checkbox.getLabel());
    }

    @Test
    public void setEnable() {
        Checkbox checkbox = new Checkbox("foo", true);
        checkbox.setEnabled(true);
        Assert.assertTrue(checkbox.isEnabled());
        checkbox.setEnabled(false);
        Assert.assertFalse(checkbox.isEnabled());
    }

    @Test
    public void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue() {
        Element element = new Element("vaadin-checkbox");
        element.setProperty("checked", true);
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(Checkbox.class))
                .thenAnswer(invocation -> new Checkbox());
        Checkbox field = Component.from(element, Checkbox.class);
        Assert.assertEquals(Boolean.TRUE,
                field.getElement().getPropertyRaw("checked"));
    }
}

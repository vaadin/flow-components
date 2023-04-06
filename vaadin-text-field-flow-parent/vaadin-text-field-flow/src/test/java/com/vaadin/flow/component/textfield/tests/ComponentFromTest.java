
package com.vaadin.flow.component.textfield.tests;

import java.io.Serializable;
import java.util.Optional;

import org.junit.Assert;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.AnnotationReader;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

class ComponentFromTest {

    static <T extends Serializable> void elementHasValue_wrapIntoField_propertyIsNotSetToInitialValue(
            T value, Class<? extends Component> componentClass) {
        Optional<Tag> tag = AnnotationReader.getAnnotationFor(componentClass,
                Tag.class);
        Element element = new Element(tag.get().value());

        element.getStateProvider().setProperty(element.getNode(), "value",
                value, true);

        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        VaadinService service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);

        Instantiator instantiator = Mockito.mock(Instantiator.class);

        Mockito.when(service.getInstantiator()).thenReturn(instantiator);

        Mockito.when(instantiator.createComponent(componentClass)).thenAnswer(
                invocation -> ReflectTools.createInstance(componentClass));

        Component field = Component.from(element, componentClass);
        Assert.assertEquals(value, field.getElement().getPropertyRaw("value"));
    }
}

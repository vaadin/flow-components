/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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

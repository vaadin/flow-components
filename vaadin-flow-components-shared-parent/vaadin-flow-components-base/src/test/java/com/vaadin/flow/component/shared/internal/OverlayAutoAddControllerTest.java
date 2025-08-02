/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.shared.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementUtil;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.internal.BeforeLeaveHandler;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.Json;
import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class OverlayAutoAddControllerTest {
    private UI ui;

    @Before
    public void setUp() {
        ui = Mockito.spy(new TestUI());
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui.getInternals().setSession(session);
    }

    @Test
    public void open_withoutUI_throws() {
        UI.setCurrent(null);

        TestComponent component = new TestComponent();

        Assert.assertThrows(IllegalStateException.class,
                () -> component.setOpened(true));
    }

    @Test
    public void open_withoutParent_autoAdded() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        Assert.assertEquals(ui.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void open_withParent_notAutoAdded() {
        ParentComponent parent = new ParentComponent();
        TestComponent component = new TestComponent();
        parent.add(component);

        component.setOpened(true);
        fakeClientResponse();

        Assert.assertEquals(parent.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void open_addParentBeforeClientResponse_notAutoAdded() {
        ParentComponent parent = new ParentComponent();
        TestComponent component = new TestComponent();

        component.setOpened(true);
        parent.add(component);
        fakeClientResponse();

        Assert.assertEquals(parent.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void open_closeBeforeClientResponse_notAutoAdded() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        component.setOpened(false);
        fakeClientResponse();

        Assert.assertNull(component.getElement().getParent());
    }

    @Test
    public void open_beforeLeaveEventFiresBeforeClientResponse_autoAdded() {
        TestComponent component = new TestComponent();
        component.setOpened(true);

        BeforeLeaveEvent beforeLeaveEvent = Mockito
                .mock(BeforeLeaveEvent.class);
        ui.getInternals().getListeners(BeforeLeaveHandler.class)
                .forEach(handler -> handler.beforeLeave(beforeLeaveEvent));
        fakeClientResponse();

        Assert.assertEquals(ui.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void setSkipOnNavigation_open_beforeLeaveEventFiresBeforeClientResponse_notAutoAdded() {
        TestComponent component = new TestComponent();
        component.controller.setSkipOnNavigation(true);
        component.setOpened(true);

        BeforeLeaveEvent beforeLeaveEvent = Mockito
                .mock(BeforeLeaveEvent.class);
        ui.getInternals().getListeners(BeforeLeaveHandler.class)
                .forEach(handler -> handler.beforeLeave(beforeLeaveEvent));
        fakeClientResponse();

        Assert.assertNull(component.getElement().getParent());
    }

    @Test
    public void autoAdded_closeWithoutEvent_notAutoRemoved() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        // Just setting the property should not yet remove the component,
        // instead it should wait for the closed event
        component.setOpened(false);

        Assert.assertEquals(ui.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void autoAdded_closeWithEventOnly_notAutoRemoved() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        // Just receiving the closed event should not remove the component,
        // as the component also needs to be closed on the server side
        fireClosedEvent(component);

        Assert.assertEquals(ui.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void autoAdded_close_autoRemoved() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        // Setting the property and receiving a closed event should remove the
        // component
        component.setOpened(false);
        fireClosedEvent(component);

        Assert.assertNull(component.getElement().getParent());
    }

    @Test
    public void autoAdded_inert_close_autoRemoved() {
        TestComponent component = new TestComponent(() -> false);

        component.setOpened(true);
        fakeClientResponse();

        // Mark the component as inert
        ElementUtil.setInert(component.getElement(), true);
        fakeClientResponse();
        Assert.assertTrue(component.getElement().getNode().isInert());

        // Inert components should still receive the closed event
        component.setOpened(false);
        fireClosedEvent(component);
        Assert.assertNull(component.getElement().getParent());
    }

    @Test
    public void notAutoAdded_close_notAutoRemoved() {
        ParentComponent parent = new ParentComponent();
        TestComponent component = new TestComponent();
        parent.add(component);

        component.setOpened(true);
        fakeClientResponse();

        component.setOpened(false);
        fireClosedEvent(component);

        Assert.assertEquals(parent.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void autoAdded_close_reopenBeforeClientResponse_autoRemovedAndAutoAdded() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        component.setOpened(false);
        fireClosedEvent(component);

        Assert.assertNull(component.getElement().getParent());

        component.setOpened(true);
        fakeClientResponse();

        Assert.assertEquals(ui.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void open_withoutModalSupplier_notModal() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        Mockito.verify(ui, Mockito.times(1)).setChildComponentModal(component,
                false);
    }

    @Test
    public void open_withModalSupplierReturningTrue_isModal() {
        TestComponent component = new TestComponent(() -> true);

        component.setOpened(true);
        fakeClientResponse();

        Mockito.verify(ui, Mockito.times(1)).setChildComponentModal(component,
                true);
    }

    @Test
    public void open_withModalSupplierReturningFalse_notModal() {
        TestComponent component = new TestComponent(() -> false);

        component.setOpened(true);
        fakeClientResponse();

        Mockito.verify(ui, Mockito.times(1)).setChildComponentModal(component,
                false);
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private void fireClosedEvent(Component component) {
        Element element = component.getElement();
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(
                new DomEvent(element, "closed", Json.createObject()));
    }

    @Tag("test")
    private static class TestComponent extends Component {
        private final OverlayAutoAddController<TestComponent> controller;

        public TestComponent() {
            controller = new OverlayAutoAddController<>(this);
        }

        public TestComponent(SerializableSupplier<Boolean> isModalSupplier) {
            controller = new OverlayAutoAddController<>(this, isModalSupplier);
        }

        public void setOpened(boolean opened) {
            getElement().setProperty("opened", opened);
        }
    }

    @Tag("parent")
    private static class ParentComponent extends Component
            implements HasComponents {
    }

    private static class TestUI extends UI {
        @Override
        public boolean equals(Object obj) {
            // Needed for check in UI.setChildComponentModal to pass
            return true;
        }
    }
}

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

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.internal.BeforeLeaveHandler;
import com.vaadin.flow.server.VaadinSession;

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
    public void close_withoutParent_autoRemoved() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        component.setOpened(false);

        Assert.assertNull(component.getElement().getParent());
    }

    @Test
    public void close_withParent_notAutoRemoved() {
        ParentComponent parent = new ParentComponent();
        TestComponent component = new TestComponent();
        parent.add(component);

        component.setOpened(true);
        fakeClientResponse();

        component.setOpened(false);

        Assert.assertEquals(parent.getElement(),
                component.getElement().getParent());
    }

    @Test
    public void close_reopenBeforeClientResponse_autoRemovedAndAutoAdded() {
        TestComponent component = new TestComponent();

        component.setOpened(true);
        fakeClientResponse();

        component.setOpened(false);
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

/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.shared;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.internal.DisableOnClickController;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class DisableOnClickControllerTest {

    private static final String INIT_DISABLE_ON_CLICK_JS = "window.Vaadin.Flow.disableOnClick.initDisableOnClick(this);";

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @Test
    public void disableOnClickFalseByDefault() {
        Assert.assertFalse(component.isDisableOnClick());
    }

    @Test
    public void setDisableOnClick_disableOnClickUpdated() {
        component.setDisableOnClick(true);
        Assert.assertTrue(component.isDisableOnClick());
        component.setDisableOnClick(false);
        Assert.assertFalse(component.isDisableOnClick());
    }

    @Test
    public void disableOnClickNotSetUp_click_componentIsStillEnabled() {
        var componentIsEnabled = new AtomicBoolean(true);
        component.addClickListener(
                event -> componentIsEnabled.set(event.getSource().isEnabled()));
        component.click();
        Assert.assertTrue(componentIsEnabled.get());
    }

    @Test
    public void setDisableOnClick_click_componentIsDisabled() {
        var componentIsEnabled = new AtomicBoolean(true);
        component.addClickListener(
                event -> componentIsEnabled.set(event.getSource().isEnabled()));
        component.setDisableOnClick(true);
        component.click();
        Assert.assertFalse(componentIsEnabled.get());
    }

    @Test
    public void setDisableOnClick_clickRevertsDisabled_componentIsEnabled() {
        component.addClickListener(event -> event.getSource().setEnabled(true));
        component.setDisableOnClick(true);
        component.click();
        Assert.assertTrue(component.isEnabled());
    }

    @Test
    public void itemNotDisableOnClick_attach_initNotCalled() {
        var spiedElement = getSpiedElement();

        var ui = initUi();
        ui.add(component);
        fakeClientCommunication(ui);
        Assert.assertTrue(component.isAttached());

        Mockito.verify(spiedElement, Mockito.never())
                .executeJs(INIT_DISABLE_ON_CLICK_JS);
    }

    @Test
    public void itemDisableOnClick_notAttached_initNotCalled() {
        var spiedElement = getSpiedElement();

        component.setDisableOnClick(true);

        var ui = initUi();
        fakeClientCommunication(ui);

        Mockito.verify(spiedElement, Mockito.never())
                .executeJs(INIT_DISABLE_ON_CLICK_JS);
    }

    @Test
    public void itemAlreadyAttached_setDisableOnClickMultipleTimesInRoundTrip_initCalledOnce() {
        var spiedElement = getSpiedElement();

        var ui = initUi();
        ui.add(component);
        fakeClientCommunication(ui);
        Assert.assertTrue(component.isAttached());

        component.setDisableOnClick(true);
        component.setDisableOnClick(false);
        component.setDisableOnClick(true);
        fakeClientCommunication(ui);

        Mockito.verify(spiedElement, Mockito.times(1))
                .executeJs(INIT_DISABLE_ON_CLICK_JS);
    }

    private UI initUi() {
        var ui = new UI();
        UI.setCurrent(ui);
        var session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
        var service = Mockito.mock(VaadinService.class);
        Mockito.when(session.getService()).thenReturn(service);
        return ui;
    }

    private void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private Element getSpiedElement() {
        var spiedElement = Mockito.spy(component.getElement());
        try {
            var elementField = Component.class.getDeclaredField("element");
            elementField.setAccessible(true);
            elementField.set(component, spiedElement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return spiedElement;
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasEnabled, ClickNotifier<TestComponent> {
        private final DisableOnClickController<TestComponent> disableOnClickController = new DisableOnClickController<>(
                this);

        public void setDisableOnClick(boolean disableOnClick) {
            disableOnClickController.setDisableOnClick(disableOnClick);
        }

        public boolean isDisableOnClick() {
            return disableOnClickController.isDisableOnClick();
        }

        public void click() {
            if (isEnabled()) {
                fireEvent(new ClickEvent<>(this, false, 0, 0, 0, 0, 0, 0, false,
                        false, false, false));
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            HasEnabled.super.setEnabled(enabled);
            disableOnClickController.onSetEnabled(enabled);
        }
    }
}

/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.shared.internal.DisableOnClickController;

class DisableOnClickControllerTest {

    private TestComponent component;

    @BeforeEach
    void setup() {
        component = new TestComponent();
    }

    @Test
    void disableOnClickFalseByDefault() {
        Assertions.assertFalse(component.isDisableOnClick());
    }

    @Test
    void setDisableOnClick_disableOnClickUpdated() {
        component.setDisableOnClick(true);
        Assertions.assertTrue(component.isDisableOnClick());
        component.setDisableOnClick(false);
        Assertions.assertFalse(component.isDisableOnClick());
    }

    @Test
    void setDisableOnClick_updatesAttribute() {
        component.setDisableOnClick(true);
        Assertions.assertTrue(
                component.getElement().hasAttribute("disableonclick"));

        component.setDisableOnClick(false);
        Assertions.assertFalse(
                component.getElement().hasAttribute("disableonclick"));
    }

    @Test
    void disableOnClickNotSetUp_click_componentIsStillEnabled() {
        var componentIsEnabled = new AtomicBoolean(true);
        component.addClickListener(
                event -> componentIsEnabled.set(event.getSource().isEnabled()));
        component.click();
        Assertions.assertTrue(componentIsEnabled.get());
    }

    @Test
    void setDisableOnClick_click_componentIsDisabled() {
        var componentIsEnabled = new AtomicBoolean(true);
        component.addClickListener(
                event -> componentIsEnabled.set(event.getSource().isEnabled()));
        component.setDisableOnClick(true);
        component.click();
        Assertions.assertFalse(componentIsEnabled.get());
    }

    @Test
    void setDisableOnClick_clickRevertsDisabled_componentIsEnabled() {
        component.addClickListener(event -> event.getSource().setEnabled(true));
        component.setDisableOnClick(true);
        component.click();
        Assertions.assertTrue(component.isEnabled());
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

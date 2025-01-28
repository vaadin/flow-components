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
package com.vaadin.flow.component.shared;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.shared.internal.DisableOnClickController;

public class DisableOnClickControllerTest {

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
    public void setDisableOnClick_updatesAttribute() {
        component.setDisableOnClick(true);
        Assert.assertTrue(
                component.getElement().hasAttribute("disableOnClick"));

        component.setDisableOnClick(false);
        Assert.assertFalse(
                component.getElement().hasAttribute("disableOnClick"));
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

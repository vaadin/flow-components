/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
                component.getElement().hasAttribute("disableonclick"));

        component.setDisableOnClick(false);
        Assert.assertFalse(
                component.getElement().hasAttribute("disableonclick"));
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

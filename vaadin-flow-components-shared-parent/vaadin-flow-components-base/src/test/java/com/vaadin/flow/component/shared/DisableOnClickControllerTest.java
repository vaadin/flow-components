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

import java.util.List;
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
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.shared.internal.DisableOnClickController;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class DisableOnClickControllerTest {

    private final UI ui = new UI();
    private TestComponent component;

    @Before
    public void setup() {
        var mockSession = Mockito.mock(VaadinSession.class);
        var mockService = Mockito.mock(VaadinService.class);
        var mockContext = Mockito.mock(VaadinContext.class);
        var mockConfiguration = Mockito.mock(DeploymentConfiguration.class);

        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockSession.getConfiguration())
                .thenReturn(mockConfiguration);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);

        ui.getInternals().setSession(mockSession);

        component = new TestComponent();
        ui.add(component);
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

    @Test
    public void disableOnClickNotSetUp_setEnabled_clientSideDisabledPropertyNotUpdated() {
        component.setEnabled(false);
        component.setEnabled(true);
        Assert.assertEquals(List.of(), dumpClientSideDisabledValues());
    }

    @Test
    public void setDisableOnClick_setEnabled_clientSideDisabledPropertyUpdated() {
        component.setDisableOnClick(true);
        component.setEnabled(false);
        Assert.assertEquals(List.of(true), dumpClientSideDisabledValues());

        component.setEnabled(true);
        Assert.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    public void disableOnClickTurnedOffAfterClick_setEnabled_clientSideDisabledPropertyUpdated() {
        component.setDisableOnClick(true);
        component.click();
        Assert.assertEquals(List.of(true), dumpClientSideDisabledValues());

        component.setDisableOnClick(false);
        component.setEnabled(true);
        Assert.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    private List<Object> dumpClientSideDisabledValues() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("this.disabled = $0"))
                .map(invocation -> invocation.getParameters().get(0)).toList();
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

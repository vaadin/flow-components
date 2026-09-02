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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.shared.internal.DisableOnClickController;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.MockUIExtension;

class DisableOnClickControllerTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private TestParent parent;
    private TestComponent component;

    @BeforeEach
    void setup() {
        parent = new TestParent();
        component = new TestComponent();
        parent.add(component);
        ui.add(parent);
        ui.fakeClientCommunication();
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

    @Test
    void setEnabled_clientSideDisabledPropertyUpdated() {
        component.setEnabled(false);
        Assertions.assertEquals(List.of(true), dumpClientSideDisabledValues());

        component.setEnabled(true);
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void setEnabled_noChange_clientSideDisabledPropertyUpdated() {
        component.setEnabled(true);
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void disabledAndEnabledInSameRoundTrip_clientSideEnabledOnce() {
        component.setEnabled(false);
        component.setEnabled(true);
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void click_clickListenerEnablesComponent_clientSideEnabled() {
        component.addClickListener(event -> event.getSource().setEnabled(true));
        component.setDisableOnClick(true);
        component.click();
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void parentDisabled_setEnabled_clientSideStaysDisabled() {
        parent.setEnabled(false);
        ui.fakeClientCommunication();

        component.setEnabled(true);
        Assertions.assertEquals(List.of(true), dumpClientSideDisabledValues());
    }

    @Test
    void parentDisabled_setEnabledAndParentEnabledInSameRoundTrip_clientSideEnabled() {
        parent.setEnabled(false);
        ui.fakeClientCommunication();

        component.setEnabled(true);
        parent.setEnabled(true);
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void parentDisabledInSameRoundTrip_setEnabled_clientSideDisabled() {
        component.setEnabled(true);
        parent.setEnabled(false);
        Assertions.assertEquals(List.of(true), dumpClientSideDisabledValues());
    }

    @Test
    void detached_setEnabled_clientSideUpdatedAfterAttach() {
        parent.remove(component);
        ui.fakeClientCommunication();

        component.setEnabled(false);
        Assertions.assertEquals(List.of(), dumpClientSideDisabledValues());

        parent.add(component);
        Assertions.assertEquals(List.of(true), dumpClientSideDisabledValues());
    }

    @Test
    void detachedBeforeResponse_attachedToAnotherUi_setEnabled_clientSideDisabledPropertyUpdated() {
        component.setEnabled(false);
        component.getElement().removeFromTree();
        ui.fakeClientCommunication();

        ui.replaceUI();
        ui.add(component);
        ui.fakeClientCommunication();

        component.setEnabled(true);
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void disableOnClickModeUntilEnabledByDefault() {
        Assertions.assertEquals(DisableOnClickMode.UNTIL_ENABLED,
                component.getDisableOnClickMode());
    }

    @Test
    void setDisableOnClickMode_modeUpdated_disableOnClickEnabled() {
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);
        Assertions.assertEquals(DisableOnClickMode.UNTIL_RESPONSE,
                component.getDisableOnClickMode());
        Assertions.assertTrue(component.isDisableOnClick());
        Assertions.assertTrue(
                component.getElement().hasAttribute("disableonclick"));
    }

    @Test
    void setDisableOnClickMode_disableOnClickToggled_modeKept() {
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);
        component.setDisableOnClick(false);
        Assertions.assertFalse(component.isDisableOnClick());
        Assertions.assertEquals(DisableOnClickMode.UNTIL_RESPONSE,
                component.getDisableOnClickMode());

        component.setDisableOnClick(true);
        Assertions.assertTrue(component.isDisableOnClick());
        Assertions.assertEquals(DisableOnClickMode.UNTIL_RESPONSE,
                component.getDisableOnClickMode());
    }

    @Test
    void setDisableOnClickMode_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> component.setDisableOnClick(null));
    }

    @Test
    void untilEnabled_click_componentStaysDisabledAfterRoundTrip() {
        component.setDisableOnClick(DisableOnClickMode.UNTIL_ENABLED);
        component.click();
        ui.fakeClientCommunication();
        Assertions.assertFalse(component.isEnabled());
    }

    @Test
    void untilResponse_click_componentIsDisabledDuringClick_enabledAfterRoundTrip() {
        var componentIsEnabled = new AtomicBoolean(true);
        component.addClickListener(
                event -> componentIsEnabled.set(event.getSource().isEnabled()));
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        component.click();
        Assertions.assertFalse(componentIsEnabled.get());
        Assertions.assertFalse(component.isEnabled());

        ui.fakeClientCommunication();
        Assertions.assertTrue(component.isEnabled());
    }

    @Test
    void untilResponse_click_clientSideDisabledPropertyIsReset() {
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        component.click();
        Assertions.assertEquals(List.of(false), dumpClientSideDisabledValues());
    }

    @Test
    void untilResponse_clientClicksMultipleTimesInSameRoundTrip_onlyFirstClickHandled() {
        var clickCount = new AtomicInteger();
        component.addClickListener(event -> clickCount.incrementAndGet());
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        fakeClientClick();
        fakeClientClick();
        fakeClientClick();
        Assertions.assertEquals(1, clickCount.get());
        Assertions.assertFalse(component.isEnabled());

        ui.fakeClientCommunication();
        Assertions.assertTrue(component.isEnabled());

        fakeClientClick();
        Assertions.assertEquals(2, clickCount.get());
    }

    @Test
    void untilResponse_listenerDisablesComponent_componentStaysDisabledAfterRoundTrip() {
        component
                .addClickListener(event -> event.getSource().setEnabled(false));
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        component.click();
        ui.fakeClientCommunication();
        Assertions.assertFalse(component.isEnabled());
    }

    @Test
    void untilResponse_listenerEnablesComponent_componentIsEnabledAfterRoundTrip() {
        component.addClickListener(event -> event.getSource().setEnabled(true));
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        component.click();
        Assertions.assertTrue(component.isEnabled());
        ui.fakeClientCommunication();
        Assertions.assertTrue(component.isEnabled());
    }

    @Test
    void untilResponse_listenerDisablesParent_clientSideStaysDisabled() {
        component.addClickListener(event -> parent.setEnabled(false));
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        component.click();
        Assertions.assertEquals(List.of(true), dumpClientSideDisabledValues());
        Assertions.assertFalse(component.isEnabled());
        Assertions.assertTrue(component.getElement().getNode().isEnabledSelf());
    }

    @Test
    void untilResponse_disableOnClickTurnedOff_click_componentStaysEnabled() {
        var componentIsEnabled = new AtomicBoolean(false);
        component.addClickListener(
                event -> componentIsEnabled.set(event.getSource().isEnabled()));
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);
        component.setDisableOnClick(false);

        component.click();
        Assertions.assertTrue(componentIsEnabled.get());
        ui.fakeClientCommunication();
        Assertions.assertTrue(component.isEnabled());
    }

    @Test
    void untilResponse_detachedComponentClicked_enabledAfterAttachAndRoundTrip() {
        parent.remove(component);
        ui.fakeClientCommunication();
        component.setDisableOnClick(DisableOnClickMode.UNTIL_RESPONSE);

        component.click();
        Assertions.assertFalse(component.isEnabled());
        ui.fakeClientCommunication();
        Assertions.assertFalse(component.isEnabled());

        parent.add(component);
        ui.fakeClientCommunication();
        Assertions.assertTrue(component.isEnabled());
    }

    private void fakeClientClick() {
        Element element = component.getElement();
        DomEvent event = new DomEvent(element, "click",
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }

    private List<Object> dumpClientSideDisabledValues() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression()
                        .contains("this.disabled = $0"))
                .map(invocation -> invocation.getParameters().get(0)).toList();
    }

    @Tag("test-parent")
    private static class TestParent extends Component
            implements HasComponents, HasEnabled {
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

        public void setDisableOnClick(DisableOnClickMode mode) {
            disableOnClickController.setDisableOnClick(mode);
        }

        public DisableOnClickMode getDisableOnClickMode() {
            return disableOnClickController.getDisableOnClickMode();
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
            disableOnClickController.onSetEnabled();
        }
    }
}

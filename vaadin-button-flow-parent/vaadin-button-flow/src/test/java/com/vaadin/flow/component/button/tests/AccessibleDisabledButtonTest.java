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
package com.vaadin.flow.component.button.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.ElementListenerMap;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

public class AccessibleDisabledButtonTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            FeatureFlags.ACCESSIBLE_DISABLED_BUTTONS);

    private Button button = Mockito.spy(Button.class);

    @SuppressWarnings("rawtypes")
    private ComponentEventListener mockFocusListener = Mockito
            .mock(ComponentEventListener.class);

    @SuppressWarnings("rawtypes")
    private ComponentEventListener mockBlurListener = Mockito
            .mock(ComponentEventListener.class);

    @Before
    public void setUp() {
        button.setEnabled(false);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsDisabled_focusListenerDisabled() {
        featureFlagRule.disableFeature();

        button.addFocusListener(mockFocusListener);

        fakeClientDomEvent(button, "focus");

        Mockito.verify(mockFocusListener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsEnabled_focusListenerEnabled() {
        button.addFocusListener(mockFocusListener);

        fakeClientDomEvent(button, "focus");

        Mockito.verify(mockFocusListener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsDisabled_blurListenerDisabled() {
        featureFlagRule.disableFeature();

        button.addBlurListener(mockBlurListener);

        fakeClientDomEvent(button, "blur");

        Mockito.verify(mockBlurListener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsEnabled_blurListenerEnabled() {
        button.addBlurListener(mockBlurListener);

        fakeClientDomEvent(button, "blur");

        Mockito.verify(mockBlurListener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test
    public void accessibleButtonsDisabled_focusShortcutDisabled() {
        featureFlagRule.disableFeature();

        button.addFocusShortcut(Key.KEY_A);
        ui.add(button);
        ui.fakeClientCommunication();

        var keydownEvent = new KeyDownEvent(button, "A"); // actual key of the
                                                          // event doesn't
                                                          // matter with this
                                                          // test setup, as the
                                                          // filtering happens
                                                          // on the client side
        ComponentUtil.fireEvent(ui.getUI(), keydownEvent);

        Mockito.verify(button, Mockito.never()).focus();
    }

    @Test
    public void accessibleButtonsEnabled_focusShortcutEnabled() {
        button.addFocusShortcut(Key.KEY_A);
        ui.add(button);
        ui.fakeClientCommunication();

        var keydownEvent = new KeyDownEvent(button, "A"); // actual key of the
                                                          // event doesn't
                                                          // matter with this
                                                          // test setup, as the
                                                          // filtering happens
                                                          // on the client side
        ComponentUtil.fireEvent(ui.getUI(), keydownEvent);

        Mockito.verify(button, Mockito.times(1)).focus();
    }

    private void fakeClientDomEvent(Component component, String eventName) {
        Element element = component.getElement();
        DomEvent event = new DomEvent(element, eventName,
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }
}

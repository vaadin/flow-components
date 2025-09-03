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
package com.vaadin.flow.component.button.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
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
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class AccessibleDisabledButtonTest {

    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic = Mockito
            .mockStatic(FeatureFlags.class);

    private FeatureFlags mockFeatureFlags = Mockito.mock(FeatureFlags.class);

    private Button button = Mockito.spy(Button.class);

    @SuppressWarnings("rawtypes")
    private ComponentEventListener mockFocusListener = Mockito
            .mock(ComponentEventListener.class);

    @SuppressWarnings("rawtypes")
    private ComponentEventListener mockBlurListener = Mockito
            .mock(ComponentEventListener.class);

    private UI ui = new UI();

    @Before
    public void setUp() {
        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        VaadinService mockService = Mockito.mock(VaadinService.class);
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);

        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(mockContext))
                .thenReturn(mockFeatureFlags);

        ui.getInternals().setSession(mockSession);
        UI.setCurrent(ui);

        button.setEnabled(false);
    }

    @After
    public void tearDown() {
        mockFeatureFlagsStatic.close();
        UI.setCurrent(null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsDisabled_focusListenerDisabled() {
        button.addFocusListener(mockFocusListener);

        fakeClientDomEvent(button, "focus");

        Mockito.verify(mockFocusListener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsEnabled_focusListenerEnabled() {
        Mockito.when(mockFeatureFlags
                .isEnabled(FeatureFlags.ACCESSIBLE_DISABLED_BUTTONS))
                .thenReturn(true);

        button.addFocusListener(mockFocusListener);

        fakeClientDomEvent(button, "focus");

        Mockito.verify(mockFocusListener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsDisabled_blurListenerDisabled() {
        button.addBlurListener(mockBlurListener);

        fakeClientDomEvent(button, "blur");

        Mockito.verify(mockBlurListener, Mockito.never())
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void accessibleButtonsEnabled_blurListenerEnabled() {
        Mockito.when(mockFeatureFlags
                .isEnabled(FeatureFlags.ACCESSIBLE_DISABLED_BUTTONS))
                .thenReturn(true);

        button.addBlurListener(mockBlurListener);

        fakeClientDomEvent(button, "blur");

        Mockito.verify(mockBlurListener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test
    public void accessibleButtonsDisabled_focusShortcutDisabled() {
        button.addFocusShortcut(Key.KEY_A);
        ui.add(button);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        var keydownEvent = new KeyDownEvent(button, "A"); // actual key of the
                                                          // event doesn't
                                                          // matter with this
                                                          // test setup, as the
                                                          // filtering happens
                                                          // on the client side
        ComponentUtil.fireEvent(ui, keydownEvent);

        Mockito.verify(button, Mockito.never()).focus();
    }

    @Test
    public void accessibleButtonsEnabled_focusShortcutEnabled() {
        Mockito.when(mockFeatureFlags
                .isEnabled(FeatureFlags.ACCESSIBLE_DISABLED_BUTTONS))
                .thenReturn(true);

        button.addFocusShortcut(Key.KEY_A);
        ui.add(button);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        var keydownEvent = new KeyDownEvent(button, "A"); // actual key of the
                                                          // event doesn't
                                                          // matter with this
                                                          // test setup, as the
                                                          // filtering happens
                                                          // on the client side
        ComponentUtil.fireEvent(ui, keydownEvent);

        Mockito.verify(button, Mockito.times(1)).focus();
    }

    private void fakeClientDomEvent(Component component, String eventName) {
        Element element = component.getElement();
        DomEvent event = new DomEvent(element, eventName,
                JacksonUtils.createObjectNode());
        element.getNode().getFeature(ElementListenerMap.class).fireEvent(event);
    }
}

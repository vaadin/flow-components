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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.*;
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class ButtonSignalTest extends AbstractSignalsTest {

    private Button button;
    private Icon icon;
    private final ComponentEventListener<ClickEvent<Button>> listener = Mockito
            .spy(new ComponentEventListener<>() {
                @Override
                public void onComponentEvent(ClickEvent<Button> event) {
                }
            });
    private ValueSignal<String> textSignal;
    private Signal<String> computedSignal;

    @BeforeEach
    void setup() {
        textSignal = new ValueSignal<>("foo");
        computedSignal = Signal.computed(() -> textSignal.get() + " bar");
    }

    @AfterEach
    void tearDown() {
        if (button != null) {
            if (button.isAttached()) {
                button.removeFromParent();
            }
        }
        Mockito.reset((Object) listener);
    }

    @Test
    void textSignalCtor() {
        button = new Button(textSignal);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        Assertions.assertNull(button.getIcon());
    }

    @Test
    void textSignalAndIconCtor() {
        Icon icon = new Icon();
        button = new Button(textSignal, icon);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        Assertions.assertEquals(icon, button.getIcon());
    }

    @Test
    void textSignalAndEventCtor() {
        button = new Button(textSignal, listener);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        button.click();
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test
    void textSignalAndIconAndEventCtor() {
        Icon icon = new Icon();
        button = new Button(textSignal, icon, listener);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        Assertions.assertEquals(icon, button.getIcon());
        button.click();
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test
    void textNodeAsIcon_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Button(textSignal, new Text("bar")));
    }

    @Test
    void textSignalAndSetText_error() {
        button = new Button(textSignal, new Icon());
        UI.getCurrent().add(button);

        Assertions.assertThrows(BindingActiveException.class,
                () -> button.setText("bar"));
    }

    @Test
    void setIcon_textSignalChange_slotRemoved() {
        icon = new Icon();
        button = new Button(textSignal, icon);
        UI.getCurrent().add(button);

        textSignal.set("");

        Assertions.assertFalse(icon.getElement().hasAttribute("slot"));
    }

    @Test
    void textSignalChange_slotAttributeIsPreserved() {
        button = new Button(textSignal);
        UI.getCurrent().add(button);
        Icon icon = new Icon(VaadinIcon.BULLSEYE);
        icon.getElement().setAttribute("slot", "prefix");
        button.setIcon(icon);

        textSignal.set("bar");
        Assertions.assertEquals("prefix",
                icon.getElement().getAttribute("slot"));
    }

    @Test
    void textSignal_notAttached() {
        button = new Button(textSignal);
        assertTextSignalBindingInactive();
    }

    @Test
    void textSignal_detachedAndAttached() {
        button = new Button(textSignal);
        UI.getCurrent().add(button);
        button.removeFromParent();
        assertTextSignalBindingInactive();

        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
    }

    @Test
    void textComputedSignalCtor() {
        button = new Button(computedSignal);
        UI.getCurrent().add(button);
        Assertions.assertEquals("foo bar", button.getText());
        textSignal.set("bar");
        Assertions.assertEquals("bar bar", button.getText());
    }

    @Test
    void textComputedSignalCtor_bindText() {
        button = new Button(computedSignal);
        UI.getCurrent().add(button);

        Assertions.assertThrows(BindingActiveException.class,
                () -> button.bindText(textSignal));
    }

    @Test
    void textSignalConstructors_usesBindText() {
        var mockButton = Mockito.mock(Button.class);
        class SpyButton extends Button {
            public SpyButton(Signal<String> textSignal) {
                super(textSignal);
            }

            public SpyButton(Signal<String> textSignal, Icon icon) {
                super(textSignal, icon);
            }

            public SpyButton(Signal<String> textSignal, Icon icon,
                    ComponentEventListener<ClickEvent<Button>> listener) {
                super(textSignal, icon, listener);
            }

            @Override
            public SignalBinding<String> bindText(Signal<String> value) {
                mockButton.bindText(value);
                return super.bindText(value);
            }
        }
        ;

        button = new SpyButton(textSignal);
        Mockito.verify(mockButton, Mockito.times(1)).bindText(textSignal);
        Mockito.clearInvocations(mockButton);

        button = new SpyButton(textSignal, new Icon());
        Mockito.verify(mockButton, Mockito.times(1)).bindText(textSignal);
        Mockito.clearInvocations(mockButton);
    }

    @Test
    void bindEnabled_disableOnClickActive_throws() {
        button = new Button("foo");
        button.setDisableOnClick(true);

        Assertions.assertThrows(IllegalStateException.class,
                () -> button.bindEnabled(new ValueSignal<>(true)));
    }

    @Test
    void setDisableOnClick_enabledBindingActive_throws() {
        button = new Button("foo");
        UI.getCurrent().add(button);
        button.bindEnabled(new ValueSignal<>(true));

        Assertions.assertThrows(IllegalStateException.class,
                () -> button.setDisableOnClick(true));
    }

    @Test
    void setDisableOnClickFalse_enabledBindingActive_doesNotThrow() {
        button = new Button("foo");
        UI.getCurrent().add(button);
        button.bindEnabled(new ValueSignal<>(true));

        button.setDisableOnClick(false);
    }

    @Test
    void bindEnabled_disableOnClickNotActive_doesNotThrow() {
        button = new Button("foo");
        UI.getCurrent().add(button);

        button.bindEnabled(new ValueSignal<>(true));
    }

    private void assertTextSignalBindingActive() {
        textSignal.set("foo");
        Assertions.assertEquals("foo", button.getText());
        textSignal.set("bar");
        Assertions.assertEquals("bar", button.getText());
    }

    private void assertTextSignalBindingInactive() {
        var currentText = button.getText();
        textSignal.set(currentText + " with change");
        Assertions.assertEquals(currentText, button.getText());
    }
}

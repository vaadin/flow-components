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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.*;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ButtonSignalTest extends AbstractSignalsUnitTest {

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

    @Before
    public void setup() {
        textSignal = new ValueSignal<>("foo");
        computedSignal = Signal.computed(() -> textSignal.value() + " bar");
    }

    @After
    public void tearDown() {
        if (button != null) {
            if (button.isAttached()) {
                button.removeFromParent();
            }
        }
        Mockito.reset((Object) listener);
    }

    @Test
    public void textSignalCtor() {
        button = new Button(textSignal);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        Assert.assertNull(button.getIcon());
    }

    @Test
    public void textSignalAndIconCtor() {
        Icon icon = new Icon();
        button = new Button(textSignal, icon);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        Assert.assertEquals(icon, button.getIcon());
    }

    @Test
    public void textSignalAndEventCtor() {
        button = new Button(textSignal, listener);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        button.click();
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test
    public void textSignalAndIconAndEventCtor() {
        Icon icon = new Icon();
        button = new Button(textSignal, icon, listener);
        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
        Assert.assertEquals(icon, button.getIcon());
        button.click();
        Mockito.verify(listener, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void textNodeAsIcon_throws() {
        button = new Button(textSignal, new Text("bar"));
    }

    @Test(expected = BindingActiveException.class)
    public void textSignalAndSetText_error() {
        button = new Button(textSignal, new Icon());
        UI.getCurrent().add(button);

        button.setText("bar");
    }

    @Test
    public void textSignal_removeBinding() {
        button = new Button(textSignal, new Icon());
        UI.getCurrent().add(button);

        button.bindText(null);
        assertTextSignalBindingInactive();

        button.setText("bar");
        Assert.assertEquals("bar", button.getText());

        button.setText(null);
        Assert.assertEquals("", button.getText());
    }

    @Test
    public void setIcon_textSignalChange_slotRemoved() {
        icon = new Icon();
        button = new Button(textSignal, icon);

        textSignal.value("");

        Assert.assertFalse(icon.getElement().hasAttribute("slot"));
    }

    @Test
    public void textSignalChange_slotAttributeIsPreserved() {
        button = new Button(textSignal);
        UI.getCurrent().add(button);
        Icon icon = new Icon(VaadinIcon.BULLSEYE);
        icon.getElement().setAttribute("slot", "prefix");
        button.setIcon(icon);

        textSignal.value("bar");
        Assert.assertEquals("prefix", icon.getElement().getAttribute("slot"));
    }

    @Test
    public void textSignal_notAttached() {
        button = new Button(textSignal);
        assertTextSignalBindingInactive();
    }

    @Test
    public void textSignal_detachedAndAttached() {
        button = new Button(textSignal);
        UI.getCurrent().add(button);
        button.removeFromParent();
        assertTextSignalBindingInactive();

        UI.getCurrent().add(button);
        assertTextSignalBindingActive();
    }

    @Test
    public void textComputedSignalCtor() {
        button = new Button(computedSignal);
        UI.getCurrent().add(button);
        Assert.assertEquals("foo bar", button.getText());
        textSignal.value("bar");
        Assert.assertEquals("bar bar", button.getText());
    }

    @Test(expected = BindingActiveException.class)
    public void textComputedSignalCtor_bindText() {
        button = new Button(computedSignal);
        UI.getCurrent().add(button);

        button.bindText(textSignal);
    }

    @Test
    public void textComputedSignalCtor_removeBindingAndBindText() {
        button = new Button(computedSignal);
        UI.getCurrent().add(button);

        button.bindText(null);
        button.bindText(textSignal);
        assertTextSignalBindingActive();

        button.bindText(null);
        assertTextSignalBindingInactive();
    }

    @Test
    public void textSignalConstructors_usesBindText() {
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
            public void bindText(Signal<String> value) {
                mockButton.bindText(value);
                super.bindText(value);
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

    private void assertTextSignalBindingActive() {
        textSignal.value("foo");
        Assert.assertEquals("foo", button.getText());
        textSignal.value("bar");
        Assert.assertEquals("bar", button.getText());
    }

    private void assertTextSignalBindingInactive() {
        var currentText = button.getText();
        textSignal.value(currentText + " with change");
        Assert.assertEquals(currentText, button.getText());
    }

    private void assertIconBeforeText() {
        Assert.assertTrue("Icon should be child of button",
                button.getElement().getChildren()
                        .anyMatch(child -> child.equals(icon.getElement())));
        Assert.assertFalse(button.isIconAfterText());
        Assert.assertEquals("prefix", icon.getElement().getAttribute("slot"));
    }

    private void assertIconAfterText() {
        Assert.assertTrue("Icon should be child of button",
                button.getElement().getChildren()
                        .anyMatch(child -> child.equals(icon.getElement())));
        Assert.assertTrue(button.isIconAfterText());
        Assert.assertEquals("suffix", icon.getElement().getAttribute("slot"));
    }
}

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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasTooltip;

class ButtonTest {

    private Button button;
    private Icon icon;

    private static final String TEST_STRING = "lorem ipsum";

    @Test
    void emptyCtor() {
        button = new Button();
        Assertions.assertEquals("", button.getText());
        Assertions.assertNull(button.getIcon());
    }

    @Test
    void textCtor() {
        button = new Button("foo");
        Assertions.assertEquals("foo", button.getText());
        Assertions.assertNull(button.getIcon());
    }

    @Test
    void iconCtor() {
        Icon icon = new Icon();
        button = new Button(icon);
        Assertions.assertEquals("", button.getText());
        Assertions.assertEquals(icon, button.getIcon());
    }

    @Test
    void emptyButton_hasNoChildren() {
        button = new Button();
        Assertions.assertEquals(0, button.getElement().getChildren().count(),
                "Empty button should have no children");
    }

    @Test
    void emptyButtonWithEmptyText_hasNoChildren() {
        button = new Button("");
        Assertions.assertEquals(0, button.getElement().getChildren().count(),
                "Button with empty text should have no children");
    }

    @Test
    void buttonWithText_hasChild() {
        button = new Button("foo");
        Assertions.assertEquals(1, button.getElement().getChildren().count(),
                "Button with text should have one child");
        Assertions.assertTrue(button.getElement().getChildren().findFirst()
                .get().isTextNode(), "Child should be a text node");
    }

    @Test
    void emptyButton_setText_addsChild() {
        button = new Button();
        Assertions.assertEquals(0, button.getElement().getChildren().count(),
                "Empty button should have no children");

        button.setText("foo");
        Assertions.assertEquals(1, button.getElement().getChildren().count(),
                "Button with text should have one child");
        Assertions.assertTrue(button.getElement().getChildren().findFirst()
                .get().isTextNode(), "Child should be a text node");
    }

    @Test
    void buttonWithText_clearText_removesChild() {
        button = new Button("foo");
        Assertions.assertEquals(1, button.getElement().getChildren().count(),
                "Button with text should have one child");

        button.setText(null);
        Assertions.assertEquals(0, button.getElement().getChildren().count(),
                "Button with null text should have no children");

        button.setText("bar");
        Assertions.assertEquals(1, button.getElement().getChildren().count(),
                "Button with text should have one child");

        button.setText("");
        Assertions.assertEquals(0, button.getElement().getChildren().count(),
                "Button with empty text should have no children");
    }

    @Test
    void textAndIconCtor() {
        Icon icon = new Icon();
        button = new Button("foo", icon);
        Assertions.assertEquals("foo", button.getText());
        Assertions.assertEquals(icon, button.getIcon());
    }

    @Test
    void textIconAndEventCtor() {
        Icon icon = new Icon();
        button = new Button("foo", icon, event -> {
        });
        Assertions.assertEquals("foo", button.getText());
        Assertions.assertEquals(icon, button.getIcon());
    }

    @Test
    void setIcon() {
        button = new Button("foo", new Icon());

        icon = new Icon();
        button.setIcon(icon);
        assertIconBeforeText();

        button.setIcon(null);
        Assertions.assertNull(button.getIcon());
        Assertions.assertFalse(
                button.getChildren().anyMatch(child -> child.equals(icon)));
    }

    @Test
    void textNodeAsIcon_throws() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Button("foo", new Text("bar")));
    }

    @Test
    void setText() {
        button = new Button("foo", new Icon());
        button.setText(null);
        Assertions.assertEquals("", button.getText());

        button.setText("bar");
        Assertions.assertEquals("bar", button.getText());

        button.setText("");
        Assertions.assertEquals("", button.getText());
    }

    @Test
    void setText_setIcon_changeOrder() {
        icon = new Icon();
        button = new Button();

        button.setText(TEST_STRING);
        button.setIcon(icon);

        assertIconBeforeText();
        button.setIconAfterText(true);
        assertIconAfterText();
    }

    @Test
    void changeOrder_setIcon_setText_changeOrder() {
        icon = new Icon();
        button = new Button();

        button.setIconAfterText(true);

        button.setIcon(icon);
        button.setText(TEST_STRING);

        assertIconAfterText();
        button.setIconAfterText(false);
        assertIconBeforeText();
    }

    @Test
    void setIconWithoutText_noSlot() {
        icon = new Icon();
        button = new Button();

        button.setIcon(icon);
        Assertions.assertFalse(icon.getElement().hasAttribute("slot"));

        // Changing icon position should have no effect
        button.setIconAfterText(true);
        Assertions.assertFalse(icon.getElement().hasAttribute("slot"));

        button.setIconAfterText(false);
        Assertions.assertFalse(icon.getElement().hasAttribute("slot"));
    }

    @Test
    void setIcon_setText_slotUpdated() {
        icon = new Icon();
        button = new Button();

        button.setIcon(icon);
        button.setText(TEST_STRING);

        Assertions.assertEquals("prefix",
                icon.getElement().getAttribute("slot"));
    }

    @Test
    void setIcon_setAndRemoveText_slotRemoved() {
        icon = new Icon();
        button = new Button();

        button.setIcon(icon);
        button.setText(TEST_STRING);
        button.setText(null);

        Assertions.assertFalse(icon.getElement().hasAttribute("slot"));
    }

    @Test
    void setEnabled() {
        button = new Button();
        button.setEnabled(true);
        Assertions.assertTrue(button.isEnabled());

        button.setEnabled(false);
        Assertions.assertFalse(button.isEnabled());
    }

    @Test
    void setText_slotAttributeIsPreserved() {
        button = new Button();
        button.setText("foo");
        Icon icon = new Icon(VaadinIcon.BULLSEYE);
        icon.getElement().setAttribute("slot", "prefix");
        button.setIcon(icon);

        button.setText("bar");
        Assertions.assertEquals("prefix",
                icon.getElement().getAttribute("slot"));
    }

    @Test
    void testFireClick() {
        button = new Button();
        AtomicBoolean clicked = new AtomicBoolean(false);
        button.addClickListener(e -> {
            clicked.set(true);
        });

        Assertions.assertFalse(clicked.get());
        button.click();
        Assertions.assertTrue(clicked.get());
    }

    @Test
    void testFireClickDisabled() {
        button = new Button();
        button.setEnabled(false);
        AtomicBoolean clicked = new AtomicBoolean(false);
        button.addClickListener(e -> {
            clicked.set(true);
        });

        Assertions.assertFalse(clicked.get());
        button.click();
        Assertions.assertFalse(clicked.get());
    }

    @Test
    void disableOnClick_click_componentIsDisabled() {
        AtomicBoolean buttonIsEnabled = new AtomicBoolean(true);

        button = new Button("foo",
                event -> buttonIsEnabled.set(event.getSource().isEnabled()));
        button.setDisableOnClick(true);
        button.click();

        Assertions.assertFalse(buttonIsEnabled.get(),
                "Button should have been disabled when event has been fired");
    }

    @Test
    void disableOnClick_clickRevertsDisabled_componentIsEnabled() {
        button = new Button("foo", event -> event.getSource().setEnabled(true));
        button.setDisableOnClick(true);
        button.click();
        Assertions.assertTrue(button.isEnabled(), "Button should be enabled");
    }

    @Test
    void implementsHasTooltip() {
        button = new Button();
        Assertions.assertTrue(button instanceof HasTooltip);
    }

    @Test
    void implementHasAriaLabel() {
        button = new Button();
        Assertions.assertTrue(button instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        button = new Button();
        button.setAriaLabel("Aria label");

        Assertions.assertTrue(button.getAriaLabel().isPresent());
        Assertions.assertEquals("Aria label", button.getAriaLabel().get());
    }

    private void assertIconBeforeText() {
        Assertions.assertTrue(
                button.getElement().getChildren()
                        .anyMatch(child -> child.equals(icon.getElement())),
                "Icon should be child of button");
        Assertions.assertFalse(button.isIconAfterText());
        Assertions.assertEquals("prefix",
                icon.getElement().getAttribute("slot"));
    }

    private void assertIconAfterText() {
        Assertions.assertTrue(
                button.getElement().getChildren()
                        .anyMatch(child -> child.equals(icon.getElement())),
                "Icon should be child of button");
        Assertions.assertTrue(button.isIconAfterText());
        Assertions.assertEquals("suffix",
                icon.getElement().getAttribute("slot"));
    }
}

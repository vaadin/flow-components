/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateNode;

public class ButtonTest {

    private Button button;
    private Icon icon;

    private static final String TEST_STRING = "lorem ipsum";

    private static final String THEME_ATTRIBUTE = "theme";
    private static final String THEME_ATTRIBUTE_ICON = "icon";
    private static final String THEME_ATTRIBUTE_PRIMARY = "primary";

    @Test
    public void emptyCtor() {
        button = new Button();
        Assert.assertEquals("", button.getText());
        Assert.assertNull(button.getIcon());
    }

    @Test
    public void textCtor() {
        button = new Button("foo");
        Assert.assertEquals("foo", button.getText());
        Assert.assertNull(button.getIcon());
    }

    @Test
    public void iconCtor() {
        Icon icon = new Icon();
        button = new Button(icon);
        Assert.assertEquals("", button.getText());
        Assert.assertEquals(icon, button.getIcon());
    }

    @Test
    public void textAndIconCtor() {
        Icon icon = new Icon();
        button = new Button("foo", icon);
        Assert.assertEquals("foo", button.getText());
        Assert.assertEquals(icon, button.getIcon());
    }

    @Test
    public void textIconAndEventCtor() {
        Icon icon = new Icon();
        button = new Button("foo", icon, event -> {
        });
        Assert.assertEquals("foo", button.getText());
        Assert.assertEquals(icon, button.getIcon());
    }

    @Test
    public void setIcon() {
        button = new Button("foo", new Icon());

        icon = new Icon();
        button.setIcon(icon);
        assertIconBeforeText();

        button.setIcon(null);
        Assert.assertNull(button.getIcon());
        Assert.assertFalse(
                button.getChildren().anyMatch(child -> child.equals(icon)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void textNodeAsIcon_throws() {
        button = new Button("foo", new Text("bar"));
    }

    @Test
    public void setText() {
        button = new Button("foo", new Icon());
        button.setText(null);
        Assert.assertEquals("", button.getText());

        button.setText("bar");
        Assert.assertEquals("bar", button.getText());

        button.setText("");
        Assert.assertEquals("", button.getText());
    }

    @Test
    public void setText_setIcon_changeOrder() {
        icon = new Icon();
        button = new Button();

        button.setText(TEST_STRING);
        button.setIcon(icon);

        assertIconBeforeText();
        button.setIconAfterText(true);
        assertIconAfterText();
    }

    @Test
    public void changeOrder_setIcon_setText_changeOrder() {
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
    public void updatingThemeAttribute() {
        button = new Button();
        assertButtonHasNoThemeAttribute();

        button.setIcon(new Icon());
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_ICON);

        button.setText("foo");
        assertButtonHasNoThemeAttribute();

        button.setIcon(null);
        assertButtonHasNoThemeAttribute();

        button = new Button("foo", new Icon());
        assertButtonHasNoThemeAttribute();

        button.setText("");
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_ICON);

        button = new Button("foo", new Icon());
        button.setText(null);
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_ICON);

        // should ignore tooltips when determining theme
        button = new Button();
        button.setTooltipText("foo");
        button.setIcon(new Icon());
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_ICON);

        button = new Button(new Icon());
        button.setTooltipText("foo");
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_ICON);

        // don't override explicitly set theme-attribute
        button = new Button();
        button.getElement().setAttribute(THEME_ATTRIBUTE,
                THEME_ATTRIBUTE_PRIMARY);
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_PRIMARY);
        button.setIcon(new Icon());
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_PRIMARY);
        button.setText("foo");
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_PRIMARY);
        button.setIcon(null);
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_PRIMARY);
        button.setText(null);
        assertButtonHasThemeAttribute(THEME_ATTRIBUTE_PRIMARY);
    }

    @Test
    public void setEnabled() {
        button = new Button();
        button.setEnabled(true);
        Assert.assertTrue(button.isEnabled());

        button.setEnabled(false);
        Assert.assertFalse(button.isEnabled());
    }

    @Test
    public void setText_slotAttributeIsPreserved() {
        button = new Button();
        button.setText("foo");
        Icon icon = new Icon(VaadinIcon.BULLSEYE);
        icon.getElement().setAttribute("slot", "prefix");
        button.setIcon(icon);

        button.setText("bar");
        Assert.assertEquals("prefix", icon.getElement().getAttribute("slot"));
    }

    @Test
    public void testFireClick() {
        button = new Button();
        AtomicBoolean clicked = new AtomicBoolean(false);
        button.addClickListener(e -> {
            clicked.set(true);
        });

        Assert.assertFalse(clicked.get());
        button.click();
        Assert.assertTrue(clicked.get());
    }

    @Test
    public void testFireClickDisabled() {
        button = new Button();
        button.setEnabled(false);
        AtomicBoolean clicked = new AtomicBoolean(false);
        button.addClickListener(e -> {
            clicked.set(true);
        });

        Assert.assertFalse(clicked.get());
        button.click();
        Assert.assertFalse(clicked.get());
    }

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        button = new Button();
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Set<String> themeNames = button.getThemeNames();
        Assert.assertTrue(
                themeNames.contains(ButtonVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        button = new Button();
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        button.removeThemeVariants(ButtonVariant.LUMO_SMALL);

        Set<String> themeNames = button.getThemeNames();
        Assert.assertFalse(
                themeNames.contains(ButtonVariant.LUMO_SMALL.getVariantName()));
    }

    @Test
    public void addThemeVariant_setIcon_themeAttributeContainsThemeVariantAndIcon() {
        button = new Button();
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        button.setIcon(new Icon(VaadinIcon.ARROW_RIGHT));

        Set<String> themeNames = button.getThemeNames();
        Assert.assertTrue(themeNames.contains("icon"));
        Assert.assertTrue(themeNames
                .contains(ButtonVariant.LUMO_SUCCESS.getVariantName()));
    }

    @Test
    public void setIcon_addThemeVariant_themeAttributeContiansThemeVariantAndIcon() {
        button = new Button();
        button.setIcon(new Icon(VaadinIcon.ARROW_RIGHT));
        button.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Set<String> themeNames = button.getThemeNames();
        Assert.assertTrue(themeNames.contains("icon"));
        Assert.assertTrue(themeNames
                .contains(ButtonVariant.LUMO_SUCCESS.getVariantName()));
    }

    @Test
    public void changeIcon_iconThemeIsPreserved() {
        button = new Button();
        button.setIcon(new Icon(VaadinIcon.ARROW_RIGHT));

        Assert.assertEquals("icon", button.getThemeName());

        button.setIcon(new Icon(VaadinIcon.ALARM));

        Assert.assertEquals("icon", button.getThemeName());
    }

    @Test
    public void disableOnClick_click_componentIsDisabled() {
        AtomicBoolean buttonIsEnabled = new AtomicBoolean(true);

        button = new Button("foo",
                event -> buttonIsEnabled.set(event.getSource().isEnabled()));
        button.setDisableOnClick(true);
        button.click();

        Assert.assertFalse(
                "Button should have been disabled when event has been fired",
                buttonIsEnabled.get());
    }

    @Test
    public void disableOnClick_clickRevertsDisabled_componentIsEnabled() {
        button = new Button("foo", event -> event.getSource().setEnabled(true));
        button.setDisableOnClick(true);
        button.click();
        Assert.assertTrue("Button should be enabled", button.isEnabled());
    }

    @Test
    public void implementsHasTooltip() {
        button = new Button();
        Assert.assertTrue(button instanceof HasTooltip);
    }

    @Test
    public void implementHasAriaLabel() {
        button = new Button();
        Assert.assertTrue(button instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        button = new Button();
        button.setAriaLabel("Aria label");

        Assert.assertTrue(button.getAriaLabel().isPresent());
        Assert.assertEquals("Aria label", button.getAriaLabel().get());
    }

    @Test
    public void initDisableOnClick_onlyCalledOnceForSeverRoundtrip() {
        final Element element = Mockito.mock(Element.class);
        StateNode node = new StateNode();
        button = Mockito.spy(Button.class);

        Mockito.when(button.getElement()).thenReturn(element);

        Mockito.when(element.executeJs(Mockito.anyString()))
                .thenReturn(Mockito.mock(PendingJavaScriptInvocation.class));
        Mockito.when(element.getComponent()).thenReturn(Optional.of(button));
        Mockito.when(element.getParent()).thenReturn(null);
        Mockito.when(element.getNode()).thenReturn(node);

        button.setDisableOnClick(true);
        button.setDisableOnClick(false);
        button.setDisableOnClick(true);

        Mockito.verify(element, Mockito.times(1))
                .executeJs("window.Vaadin.Flow.button.initDisableOnClick($0)");
    }

    private void assertButtonHasThemeAttribute(String theme) {
        Assert.assertTrue("Expected " + theme + " to be in the theme attribute",
                button.getThemeNames().contains(theme));
    }

    private void assertButtonHasNoThemeAttribute() {
        Assert.assertNull(button.getElement().getAttribute("theme"));
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

    private Element getButtonChild(int index) {
        return button.getElement().getChild(index);
    }

}

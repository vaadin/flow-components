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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ButtonVariantTest {

    private Button button;

    private static final String THEME_ATTRIBUTE = "theme";
    private static final String THEME_ATTRIBUTE_ICON = "icon";
    private static final String THEME_ATTRIBUTE_PRIMARY = "primary";

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

    private void assertButtonHasThemeAttribute(String theme) {
        Assert.assertTrue("Expected " + theme + " to be in the theme attribute",
                button.getThemeNames().contains(theme));
    }

    private void assertButtonHasNoThemeAttribute() {
        Assert.assertNull(button.getElement().getAttribute("theme"));
    }

}

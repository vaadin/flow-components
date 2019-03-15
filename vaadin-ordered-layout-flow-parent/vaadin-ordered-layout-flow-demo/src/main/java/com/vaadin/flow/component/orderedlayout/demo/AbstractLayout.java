/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout.demo;

import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.impl.ThemeListImpl;

/**
 * Abstract layout class containing common code for horizontal and vertical
 * demos.
 */
public abstract class AbstractLayout extends DemoView {

    protected Div createComponent(int index, String color) {
        Div component = new Div();
        component.setText("Component " + index);
        component.getStyle().set("backgroundColor", color).set("color", "white")
                .set("padding", "5px 10px");
        return component;
    }

    protected Component createSpacingButton(FlexComponent<?> layout, String id,
            FlexComponent.JustifyContentMode spacing) {
        NativeButton button = new NativeButton(spacing.name());
        button.setId(id);
        button.addClickListener(event -> layout.setJustifyContentMode(spacing));
        return button;
    }

    public static NativeButton createToggleThemeButton(ThemableLayout layout,
            String themeName) {
        return createToggleThemeButton(layout, themeName,
                toggle -> layout.getThemeList().set(themeName, toggle));
    }

    public static NativeButton createToggleThemeButton(ThemableLayout layout,
            String themeName, Consumer<Boolean> toggleAction) {
        NativeButton toggleButton = new NativeButton(
                String.format("Toggle %s", themeName),
                event -> toggleAction.accept(hasNoAttributeValue(
                        layout.getElement(), ThemeListImpl.THEME_ATTRIBUTE_NAME,
                        themeName)));
        toggleButton.setId(String.format("toggle-%s", themeName));
        return toggleButton;
    }

    protected static boolean hasNoAttributeValue(Element element,
            String attribute, String attributeValue) {
        return Optional.ofNullable(element.getAttribute(attribute))
                .map(value -> !value.contains(attributeValue)).orElse(true);
    }

    protected Component createAlignmentButton(HorizontalLayout layout,
            String id, FlexComponent.Alignment alignment) {
        NativeButton button = new NativeButton(alignment.name());
        button.setId(id);
        button.addClickListener(event -> layout
                .setDefaultVerticalComponentAlignment(alignment));
        return button;
    }

    protected Component createAlignmentButton(VerticalLayout layout, String id,
            FlexComponent.Alignment alignment) {
        NativeButton button = new NativeButton(alignment.name());
        button.setId(id);
        button.addClickListener(event -> layout
                .setDefaultHorizontalComponentAlignment(alignment));
        return button;
    }

    protected Component createBoxSizingButtons(ThemableLayout layout,
            String idPrefix) {
        NativeButton contentBox = new NativeButton("Use content-box");
        contentBox.setId(idPrefix + "-content-box");
        contentBox.addClickListener(
                event -> layout.setBoxSizing(BoxSizing.CONTENT_BOX));

        NativeButton borderBox = new NativeButton("Use border-box");
        borderBox.setId(idPrefix + "-border-box");
        borderBox.addClickListener(
                event -> layout.setBoxSizing(BoxSizing.BORDER_BOX));

        return new Div(contentBox, borderBox);
    }

    protected Div createLoremIpsum() {
        Div component = new Div();
        component.setText("Lorem ipsum dolor sit amet, consectetur " +
                "adipiscing elit, sed do eiusmod tempor incididunt " +
                "ut labore et dolore magna aliqua. Ut enim ad minim " +
                "veniam, quis nostrud exercitation ullamco laboris " +
                "nisi ut aliquip ex ea commodo consequat. Duis aute " +
                "irure dolor in reprehenderit in voluptate velit " +
                "esse cillum dolore eu fugiat nulla pariatur.");
        component.getStyle().set("border", "1px solid #CCCCCC");

        return component;
    }
}

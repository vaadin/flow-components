/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.TextRenderer;

import java.util.function.Consumer;

/**
 * Abstract layout class containing common code for horizontal and vertical
 * demos.
 */
public abstract class AbstractLayout extends Div {

    protected Div createComponent(int index, String color) {
        Div component = new Div();
        component.setText("Component " + index);
        component.getStyle().set("backgroundColor", color).set("color", "white")
                .set("padding", "5px 10px");
        return component;
    }

    public static Checkbox createToggleThemeCheckbox(ThemableLayout layout,
            String themeName) {
        return createToggleThemeCheckbox(themeName,
                toggle -> layout.getThemeList().set(themeName, toggle), false);
    }

    public static Checkbox createToggleThemeCheckbox(String themeName,
            Consumer<Boolean> toggleAction, boolean defaultValue) {
        Checkbox toggleButton = new Checkbox(themeName);
        toggleButton.setValue(defaultValue);
        toggleButton.addValueChangeListener(
                event -> toggleAction.accept(event.getValue()));
        toggleButton.setId(String.format("toggle-%s", themeName));
        return toggleButton;
    }

    protected Component createBoxSizingButtons(ThemableLayout layout,
            String idPrefix) {
        RadioButtonGroup<String> boxSizing = new RadioButtonGroup<>();
        boxSizing.setItems("Content-box", "Border-box");

        boxSizing.addValueChangeListener(event -> {
            if ("Border-box".equals(event.getValue()))
                layout.setBoxSizing(BoxSizing.BORDER_BOX);

            else
                layout.setBoxSizing(BoxSizing.CONTENT_BOX);
        });
        boxSizing.setValue("Border-box");
        boxSizing.setId(idPrefix + "-radio-button");

        return boxSizing;
    }

    public <T extends Enum> RadioButtonGroup<T> createRadioButtonGroup(
            T[] values, Consumer<T> consumer, T defaultValue) {
        RadioButtonGroup<T> rbg = new RadioButtonGroup<>();
        rbg.setItems(values);
        rbg.setValue(defaultValue);
        rbg.setRenderer(new TextRenderer<>(
                enumValue -> enumValue.name().toLowerCase()));

        rbg.addValueChangeListener(e -> {
            consumer.accept(e.getValue());
        });
        return rbg;
    }

    protected Div createLoremIpsum() {
        Div component = new Div();
        component.setText("Lorem ipsum dolor sit amet, consectetur "
                + "adipiscing elit, sed do eiusmod tempor incididunt "
                + "ut labore et dolore magna aliqua. Ut enim ad minim "
                + "veniam, quis nostrud exercitation ullamco laboris "
                + "nisi ut aliquip ex ea commodo consequat. Duis aute "
                + "irure dolor in reprehenderit in voluptate velit "
                + "esse cillum dolore eu fugiat nulla pariatur.");
        component.getStyle().set("border", "1px solid #CCCCCC");

        return component;
    }
}

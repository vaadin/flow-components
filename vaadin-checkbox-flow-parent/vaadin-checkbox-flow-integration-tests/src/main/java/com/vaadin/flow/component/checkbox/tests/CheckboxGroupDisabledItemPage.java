package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * View for {@link CheckboxGroup} integration tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-group-disabled-item")
public class CheckboxGroupDisabledItemPage extends VerticalLayout {

    public CheckboxGroupDisabledItemPage() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar", "baz");
        group.select("bar");
        group.setItemEnabledProvider(item -> !"bar".equals(item));
        group.setId("checkbox-group-disabled-item");

        NativeButton toggleBarButton = new NativeButton("Toggle \"bar\"",
                event -> {
                    boolean isBarSelected = group.isSelected("bar");
                    if (isBarSelected) {
                        group.deselect("bar");
                    } else {
                        group.select("bar");
                    }
                });
        toggleBarButton.setId("toggle-bar-button");

        NativeButton toggleEnabledButton = new NativeButton("Toggle enabled",
                event -> group.setEnabled(!group.isEnabled()));
        toggleEnabledButton.setId("toggle-enabled-button");

        add(group, new Div(toggleBarButton, toggleEnabledButton));
    }
}

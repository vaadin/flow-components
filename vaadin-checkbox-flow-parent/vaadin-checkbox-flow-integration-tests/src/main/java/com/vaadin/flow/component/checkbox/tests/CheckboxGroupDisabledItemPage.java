package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
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
        add(group);
    }
}

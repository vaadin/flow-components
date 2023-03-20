
package com.vaadin.flow.component.checkbox.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.dataview.CheckboxGroupListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/refresh-on-value-change")
public class RefreshOnValueChangePage extends Div {

    public RefreshOnValueChangePage() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setLabel("Label");

        List<String> items = new LinkedList<>(Arrays.asList("foo", "bar"));
        CheckboxGroupListDataView<String> dataView = group.setItems(items);

        group.addValueChangeListener(e -> dataView.refreshAll());

        add(group);
    }
}

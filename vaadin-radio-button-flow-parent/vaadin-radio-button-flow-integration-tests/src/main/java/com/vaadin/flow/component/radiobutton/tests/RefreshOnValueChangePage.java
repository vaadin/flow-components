
package com.vaadin.flow.component.radiobutton.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.dataview.RadioButtonGroupListDataView;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/refresh-on-value-change")
public class RefreshOnValueChangePage extends Div {

    public RefreshOnValueChangePage() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setLabel("Label");

        List<String> items = new LinkedList<>(Arrays.asList("foo", "bar"));
        RadioButtonGroupListDataView<String> dataView = group.setItems(items);

        group.addValueChangeListener(e -> dataView.refreshAll());

        add(group);
    }
}

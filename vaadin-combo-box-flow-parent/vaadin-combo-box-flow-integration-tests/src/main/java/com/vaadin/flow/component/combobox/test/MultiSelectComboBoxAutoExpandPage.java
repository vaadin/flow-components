package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox.AutoExpandMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/auto-expand")
public class MultiSelectComboBoxAutoExpandPage extends Div {
    public MultiSelectComboBoxAutoExpandPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        NativeButton expandHorizontal = new NativeButton("Expand horizontal",
                e -> {
                    comboBox.setAutoExpand(AutoExpandMode.HORIZONTAL);
                });
        expandHorizontal.setId("expand-horizontal");

        NativeButton expandVertical = new NativeButton("Expand vertical", e -> {
            comboBox.setAutoExpand(AutoExpandMode.VERTICAL);
        });
        expandVertical.setId("expand-vertical");

        NativeButton expandBoth = new NativeButton("Expand both", e -> {
            comboBox.setAutoExpand(AutoExpandMode.BOTH);
        });
        expandBoth.setId("expand-both");

        NativeButton expandNone = new NativeButton("Expand none", e -> {
            comboBox.setAutoExpand(AutoExpandMode.NONE);
        });
        expandNone.setId("expand-none");

        add(comboBox);
        add(new Div(expandHorizontal, expandVertical, expandBoth, expandNone));
    }
}

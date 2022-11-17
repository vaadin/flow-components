package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/detach-reattach")
public class MultiSelectComboBoxDetachReattachPage extends Div {
    public MultiSelectComboBoxDetachReattachPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        NativeButton detach = new NativeButton("detach", e -> remove(comboBox));
        detach.setId("detach");

        NativeButton attach = new NativeButton("attach", e -> add(comboBox));
        attach.setId("attach");

        add(comboBox, detach, attach);
    }
}

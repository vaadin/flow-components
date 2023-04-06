
package com.vaadin.flow.component.radiobutton.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/refresh-items")
public class RefreshItemsPage extends Div {

    public RefreshItemsPage() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setLabel("Label");
        group.setId("group");

        List<String> items = new LinkedList<>(Arrays.asList("foo", "bar"));
        group.setItems(items);

        NativeButton button = new NativeButton("Update items", e -> {
            items.add("baz");
            items.remove(0);
            group.setItems(items);
        });

        button.setId("reset");

        add(group, button);
    }
}

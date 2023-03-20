
package com.vaadin.flow.component.checkbox.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/refresh-data-provider")
public class RefreshDataProviderPage extends Div {

    public RefreshDataProviderPage() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setLabel("Label");
        group.setId("group");

        List<String> items = new LinkedList<>(Arrays.asList("foo", "bar"));
        group.setItems(new ListDataProvider<>(items));

        NativeButton button = new NativeButton("Update items", e -> {
            items.add("baz");
            items.remove(0);
            group.getDataProvider().refreshAll();
        });

        button.setId("reset");

        add(group, button);
    }
}


package com.vaadin.flow.component.checkbox.tests;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link Checkbox}.
 */
@Route("vaadin-checkbox/detach-reattach")
public class DetachReattachPage extends Div {

    public DetachReattachPage() {
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setId("group");

        List<String> items = new LinkedList<>(
                Arrays.asList("foo", "bar", "baz"));
        group.setItems(new ListDataProvider<>(items));

        NativeButton detach = new NativeButton("detach", e -> remove(group));
        detach.setId("detach");

        NativeButton attach = new NativeButton("attach", e -> add(group));
        attach.setId("attach");

        NativeButton setValue = new NativeButton("set value", e -> group
                .setValue(new HashSet<>(Arrays.asList("foo", "baz"))));
        setValue.setId("setValue");

        NativeButton deselectAll = new NativeButton("deselect all",
                e -> group.deselectAll());
        deselectAll.setId("deselectAll");

        add(group, detach, attach, setValue, deselectAll);
    }
}

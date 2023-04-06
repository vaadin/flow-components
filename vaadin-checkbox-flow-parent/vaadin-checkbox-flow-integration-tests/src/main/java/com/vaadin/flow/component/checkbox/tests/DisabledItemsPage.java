
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/disabled-items")
public class DisabledItemsPage extends Div {

    public DisabledItemsPage() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setId("checkbox-group");
        checkboxGroup.setEnabled(false);

        NativeButton nativeButton = new NativeButton("add",
                event -> checkboxGroup.setItems("one", "two"));
        nativeButton.setId("add-button");

        add(checkboxGroup, nativeButton);
    }

}

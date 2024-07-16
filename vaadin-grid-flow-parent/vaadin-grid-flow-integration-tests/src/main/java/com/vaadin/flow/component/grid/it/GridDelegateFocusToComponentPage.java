/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/delegate-focus-inside-component-renderer")
public class GridDelegateFocusToComponentPage extends Div {

    public GridDelegateFocusToComponentPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");

        Div div = new Div();
        div.setId("info");

        grid.addColumn(item -> item);
        grid.addComponentColumn(this::buildComplexComponent)
                .setHeader("Components");
        grid.addComponentColumn(
                item -> new Button(item, e -> div.setText(item)))
                .setHeader("Components");

        grid.setItems("foo", "bar");
        add(grid, div);
    }

    private Div buildComplexComponent(String item) {
        TextField hiddenTextField = new TextField();
        hiddenTextField.setValue("hidden");
        hiddenTextField.getElement().setAttribute("hidden", true);
        TextField disabledTextField = new TextField();
        disabledTextField.setValue("disabled");
        disabledTextField.setEnabled(false);
        TextField focusableTextField = new TextField();
        focusableTextField.setValue(item);
        focusableTextField.setId(item);
        Button nextFocusableComponent = new Button("OK");
        VerticalLayout layout = new VerticalLayout(hiddenTextField,
                disabledTextField, focusableTextField, nextFocusableComponent);

        Button anotherFocusableComponent = new Button("NO");
        return new Div(layout, anotherFocusableComponent);
    }

}


package com.vaadin.flow.component.combobox.test;

import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/pre-selected")
public class PreSelectedValuePage extends Div {

    private static final String PRE_SELECTED_VALUE = "Item 1";

    public PreSelectedValuePage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(IntStream.range(0, 20).mapToObj(i -> "Item " + i));
        comboBox.setValue(PRE_SELECTED_VALUE);
        comboBox.setId("combo");

        Div div = new Div();
        div.setId("info");

        NativeButton button = new NativeButton("Print combo-box value",
                event -> div.setText(comboBox.getValue()));
        button.setId("get-value");

        add(button, div, comboBox);

    }
}


package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clear")
public class ComboBoxClearPage extends VerticalLayout {

    public ComboBoxClearPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten");
        comboBox.setValue("Eight");
        add(comboBox);
    }
}

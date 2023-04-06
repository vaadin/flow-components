
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/change-value-and-visibility")
public class ChangeValueAndVisibilityPage extends VerticalLayout {

    public static final String INITIAL_VALUE = "Item 1";
    public static final String NEW_VALUE = "Item 2";
    public static final String ALTERNATIVE_VALUE = "Item 3";

    public static final String VALUE_CHANGES_ID = "value-changes";
    public static final String VISIBILITY_VALUE_ID = "visibility-value-button";
    public static final String VALUE_ID = "value-button";
    public static final String VISIBILITY_ID = "visibility-button";

    public ChangeValueAndVisibilityPage() {
        ComboBox<String> comboBox = new ComboBox<>("Items", INITIAL_VALUE,
                NEW_VALUE, ALTERNATIVE_VALUE);
        comboBox.setVisible(false);
        comboBox.setValue(INITIAL_VALUE);

        Div valueChanges = new Div();
        valueChanges.setId(VALUE_CHANGES_ID);
        comboBox.addValueChangeListener(
                e -> valueChanges.add(new Paragraph(e.getValue())));

        NativeButton visibilityValueButton = new NativeButton(
                "Make visible and set value", e -> {
                    comboBox.setVisible(true);
                    comboBox.setValue(NEW_VALUE);
                });
        visibilityValueButton.setId(VISIBILITY_VALUE_ID);

        NativeButton visibilityButton = new NativeButton("Make visible ",
                e -> comboBox.setVisible(true));
        visibilityButton.setId(VISIBILITY_ID);

        NativeButton valueButton = new NativeButton("Set value ",
                e -> comboBox.setValue(ALTERNATIVE_VALUE));
        valueButton.setId(VALUE_ID);

        add(visibilityValueButton, visibilityButton, valueButton, comboBox,
                valueChanges);
    }

}

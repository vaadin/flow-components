
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clear-value")
public class ClearValuePage extends Div {
    static final String INITIAL_VALUE = "two";

    static final String COMBO_BOX_ID = "comboBox";
    static final String BUTTON_SET_NULL_ID = "buttonSetNull";
    static final String BUTTON_CLEAR_ID = "buttonClear";

    static final String COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_ID = "comboBoxWithAllowCustomValue";
    static final String BUTTON_CUSTOM_VALUE_SET_NULL_ID = "buttonSetNullCustom";
    static final String BUTTON_CUSTOM_VALUE_CLEAR_ID = "buttonClearCustom";

    static final String COMBO_BOX_WITH_CLEAR_BUTTON_ID = "comboBoxWithClearButton";

    public ClearValuePage() {
        ComboBox<String> comboBox = new ComboBox<>("Ordinary combo box", "one",
                INITIAL_VALUE, "three");
        comboBox.setValue(INITIAL_VALUE);
        comboBox.setId(COMBO_BOX_ID);

        NativeButton setNull = new NativeButton(
                "Set null to ordinary combo box",
                event -> comboBox.setValue(null));
        setNull.setId(BUTTON_SET_NULL_ID);

        NativeButton clear = new NativeButton("Clear ordinary combo box",
                event -> comboBox.clear());
        clear.setId(BUTTON_CLEAR_ID);

        ComboBox<String> comboBoxWithAllowCustomValue = new ComboBox<>(
                "Combo box with custom value", "one", INITIAL_VALUE, "three");
        comboBoxWithAllowCustomValue.setAllowCustomValue(true);
        comboBoxWithAllowCustomValue.setValue(INITIAL_VALUE);
        comboBoxWithAllowCustomValue
                .setId(COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_ID);

        NativeButton setNullCustom = new NativeButton(
                "Set null to combo box with custom value",
                event -> comboBoxWithAllowCustomValue.setValue(null));
        setNullCustom.setId(BUTTON_CUSTOM_VALUE_SET_NULL_ID);

        NativeButton clearCustom = new NativeButton(
                "Clear combo box with custom value",
                event -> comboBoxWithAllowCustomValue.clear());
        clearCustom.setId(BUTTON_CUSTOM_VALUE_CLEAR_ID);

        Div valueMessages = new Div();
        valueMessages.setId("value-messages");

        ComboBox<String> comboBoxWithClearButton = new ComboBox<>(
                "Combo box with clear button", "one", INITIAL_VALUE, "three");
        comboBoxWithClearButton.setValue(INITIAL_VALUE);
        comboBoxWithClearButton.setClearButtonVisible(true);
        comboBoxWithClearButton.setId(COMBO_BOX_WITH_CLEAR_BUTTON_ID);

        comboBoxWithClearButton.addValueChangeListener(e -> valueMessages.add(
                new Paragraph(e.getValue() == null ? "null" : e.getValue())));

        add(new Div(comboBox, setNull, clear),
                new Div(comboBoxWithAllowCustomValue, setNullCustom,
                        clearCustom),
                new Div(comboBoxWithClearButton, valueMessages));
    }
}


package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.bean.TestItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/required-combobox")
public class RequiredComboboxPage extends Div {

    public RequiredComboboxPage() {
        Div message = new Div();
        message.setId("message");

        Binder<TestItem> binder = new Binder<>();

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("foo", "bar");
        comboBox.addValueChangeListener(event -> message
                .setText(String.format("Value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));

        binder.forField(comboBox).asRequired()
                .withValidator(value -> !"foo".equals(value),
                        "'foo' is invalid value")
                .bind(TestItem::getName, TestItem::setName);
        TestItem item = new TestItem(0);
        binder.setBean(item);

        add(comboBox, message);

        requiredComboBoxSetItemsAfter();
    }

    private void requiredComboBoxSetItemsAfter() {

        Binder<TestItem> binder = new Binder<>();

        ComboBox<String> comboBox = new ComboBox<>();

        binder.forField(comboBox).asRequired().bind(TestItem::getName,
                TestItem::setName);
        binder.setBean(new TestItem(0));

        // Set items last:
        comboBox.setItems("foo", "bar");

        add(comboBox);
    }
}

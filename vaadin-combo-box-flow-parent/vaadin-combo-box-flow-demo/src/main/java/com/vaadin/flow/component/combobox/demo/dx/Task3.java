package com.vaadin.flow.component.combobox.demo.dx;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("dx-test-task3")
public class Task3 extends DemoView {

    @Override
    protected void initView() {
        ComboBox<String> comboBox = new ComboBox<>();

        ComboBoxListDataView<String> dataView = comboBox.setItems("Item1",
                "Item2", "Item3");

        // TODO: create a notification of Combo box's item count change.
        //  Then change the items count and check if notification appears.
        //  Consider the filtering as the primary reason of item count
        //  change, but you can also add/remove the items.
        //  Use 'showNotification' method.

        addCard("Task3", comboBox);
    }

    private void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }
}

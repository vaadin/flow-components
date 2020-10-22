package com.vaadin.flow.component.combobox.demo.dx;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("dx-test-task3")
public class Task3 extends DemoView {

    @Override
    protected void initView() {
        ComboBox<String> comboBox = new ComboBox<>();

        ComboBoxListDataView<String> dataView = comboBox.setItems("Item1",
                "Item2", "Item3");

        // TODO: create a notification when the combo box's item count is
        //  changed

        // TODO: use the javadoc of 'ComboBoxListDataView' to figure out what
        //  method to use and when the notification can be triggered

        // TODO: Use Notification component for pop ups

        addCard("Task3", comboBox);
    }
}

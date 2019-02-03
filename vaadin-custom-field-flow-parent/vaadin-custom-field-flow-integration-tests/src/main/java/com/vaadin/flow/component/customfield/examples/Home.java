package com.vaadin.flow.component.customfield.examples;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        final Div result = new Div();
        result.setId("result");
        MyField customField = new MyField();
        NativeButton button = new NativeButton("Update");
        button.setId("button1");
        button.addClickListener(e -> customField.updateValue());
        customField
            .addValueChangeListener(e -> result.setText("" + e.getValue()));
        add(customField, result,button);
    }

    private class MyField extends CustomField<Integer> {
        final TextField field1 = new TextField();
        final TextField field2 = new TextField();

        MyField() {
            field1.setId("field1");
            field2.setId("field2");
            add(field1, field2);

        }


        @Override
        protected Integer generateModelValue() {
            try {
                int i1 = Integer.valueOf(field1.getValue());
                int i2 = Integer.valueOf(field2.getValue());
                return i1 + i2;
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        protected void updateValue() {
            super.updateValue();
        }

        @Override
        protected void setPresentationValue(Integer integer) {

        }
    }
}

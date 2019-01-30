package com.vaadin.flow.component.customfield.examples;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        final TextField field1 = new TextField();
        field1.setId("field1");
        final TextField field2 = new TextField();
        field2.setId("field2");
        final Div result = new Div();
        CustomField<Integer> customField = new CustomField<Integer>() {
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
            protected void setPresentationValue(Integer integer) {
                result.setText("" + integer);
            }
        };
        customField.add(field1, field2, result);

        add(customField);
    }
}

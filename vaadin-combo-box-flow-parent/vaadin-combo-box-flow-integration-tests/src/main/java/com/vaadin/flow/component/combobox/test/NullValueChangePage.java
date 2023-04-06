
package com.vaadin.flow.component.combobox.test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/null-value-change")
public class NullValueChangePage extends Div {

    public static class Person implements Serializable {
        private String name;

        public Person(String firstName) {
            this.name = firstName;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public NullValueChangePage() {
        Person jorma = new Person("Jorma");
        Person kalle = new Person("Kalle");
        List<Person> list = Arrays.asList(jorma, kalle);

        ComboBox<Person> cb = new ComboBox<>();
        cb.setItems(list);
        cb.addValueChangeListener(e -> {
            e.getSource().setValue(null);
        });

        cb.setAllowCustomValue(true);
        add(cb); // MainView extends VerticalLayout

    }
}

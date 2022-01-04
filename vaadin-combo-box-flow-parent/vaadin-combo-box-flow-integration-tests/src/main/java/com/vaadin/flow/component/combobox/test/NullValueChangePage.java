/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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

/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Address;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/beangridpage")
public class BeanGridPage extends Div {

    public BeanGridPage() {
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItems(new Person("Jorma", 2018), new Person("Jarvi", 33));
        grid.setItemDetailsRenderer(
                LitRenderer.<Person> of("<div>${item.name} ${item.age}</div>")
                        .withProperty("name", Person::getFirstName)
                        .withProperty("age", Person::getAge));
        add(grid);

        createSetColumnsGrid();
    }

    private void createSetColumnsGrid() {
        Address address = new Address();
        address.setStreet("Main Street");
        address.setNumber(5);
        address.setPostalCode(12345);
        Person person = new Person("Anna", 25);
        person.setAddress(address);

        Grid<Person> grid = new Grid<>(Person.class);
        grid.setId("set-columns-grid");
        grid.setItems(person);

        NativeButton showAddressColumns = new NativeButton(
                "Show address columns",
                event -> grid.setColumns("address.street", "address.number",
                        "address.postalCode"));
        showAddressColumns.setId("show-address-columns");

        NativeButton showBasicColumns = new NativeButton("Show basic columns",
                event -> grid.setColumns("firstName", "age"));
        showBasicColumns.setId("show-basic-columns");

        add(grid, showAddressColumns, showBasicColumns);
    }

}

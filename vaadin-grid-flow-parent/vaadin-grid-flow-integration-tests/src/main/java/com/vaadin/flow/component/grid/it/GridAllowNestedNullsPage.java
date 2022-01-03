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
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("allow-nested-nulls")
public class GridAllowNestedNullsPage extends Div {

    public GridAllowNestedNullsPage() {
        addButtons();
    }

    public void addEmployeeGrid(NestedNullBehavior behavior) {
        removeAll();
        addButtons();
        List<Employee> employeeList = mockEmployees();
        Grid<Employee> grid = new Grid<>(Employee.class, false);
        grid.setNestedNullBehavior(behavior);
        grid.setColumns("name", "company.companyname");
        grid.setDataProvider(new ListDataProvider<>(employeeList));
        add(grid);
    }

    private void addButtons() {
        Button nullAllowedGridBtn = new Button("Nulls allowed");
        nullAllowedGridBtn.setId("null-allowed");
        nullAllowedGridBtn.addClickListener(event -> {
            addEmployeeGrid(NestedNullBehavior.ALLOW_NULLS);
        });
        Button nullThrownGridBtn = new Button("Nulls thrown");
        nullThrownGridBtn.setId("null-thrown");
        nullThrownGridBtn.addClickListener(event -> {
            addEmployeeGrid(NestedNullBehavior.THROW);
        });
        add(nullAllowedGridBtn, nullThrownGridBtn);
    }

    private List<Employee> mockEmployees() {
        List<Employee> list = new ArrayList<>();
        list.add(new Employee("Laurel"));
        list.add(new Employee("Hardy"));
        return list;
    }

    public class Employee {
        private String name;
        private Company company;

        public Employee(String name) {
            this.name = name;
        }

        public Employee(String name, Company company) {
            this.name = name;
            this.company = company;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }
    }

    public class Company {
        public String companyname;

        public Company(String companyname) {
            this.companyname = companyname;
        }

        public String getCompanyname() {
            return companyname;
        }

        public void setCompanyname(String companyname) {
            this.companyname = companyname;
        }
    }
}

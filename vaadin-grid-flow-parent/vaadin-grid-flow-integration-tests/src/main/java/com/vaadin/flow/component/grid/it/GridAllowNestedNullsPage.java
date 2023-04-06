/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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

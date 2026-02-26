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
package com.vaadin.flow.demo.views;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Crud component.
 */
@Route(value = "crud", layout = MainLayout.class)
@PageTitle("CRUD | Vaadin Kitchen Sink")
public class CrudDemoView extends VerticalLayout {

    private List<Employee> employees = new ArrayList<>();

    public CrudDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("CRUD Component"));
        add(new Paragraph("CRUD provides a complete Create, Read, Update, Delete interface."));

        // Initialize sample data
        initializeData();

        // Basic CRUD
        Crud<Employee> crud = new Crud<>(Employee.class, createEditor());

        // Configure grid columns
        crud.getGrid().removeAllColumns();
        crud.getGrid().addColumn(Employee::getFirstName).setHeader("First Name");
        crud.getGrid().addColumn(Employee::getLastName).setHeader("Last Name");
        crud.getGrid().addColumn(Employee::getEmail).setHeader("Email");
        crud.getGrid().addColumn(Employee::getDepartment).setHeader("Department");

        // Set up data provider
        ListDataProvider<Employee> dataProvider = new ListDataProvider<>(employees);
        crud.setDataProvider(dataProvider);

        // Add CRUD listeners
        crud.addSaveListener(event -> {
            Employee employee = event.getItem();
            if (!employees.contains(employee)) {
                employees.add(employee);
            }
            dataProvider.refreshAll();
            Notification.show("Saved: " + employee.getFirstName() + " " + employee.getLastName());
        });

        crud.addDeleteListener(event -> {
            employees.remove(event.getItem());
            dataProvider.refreshAll();
            Notification.show("Deleted: " + event.getItem().getFirstName());
        });

        crud.setHeight("500px");
        crud.setWidthFull();
        addSection("Employee Management", crud);
    }

    private void initializeData() {
        employees.add(new Employee("John", "Doe", "john.doe@company.com", "Engineering"));
        employees.add(new Employee("Jane", "Smith", "jane.smith@company.com", "Marketing"));
        employees.add(new Employee("Bob", "Johnson", "bob.j@company.com", "Sales"));
        employees.add(new Employee("Alice", "Williams", "alice.w@company.com", "HR"));
        employees.add(new Employee("Charlie", "Brown", "charlie.b@company.com", "Engineering"));
    }

    private CrudEditor<Employee> createEditor() {
        TextField firstName = new TextField("First Name");
        TextField lastName = new TextField("Last Name");
        EmailField email = new EmailField("Email");
        TextField department = new TextField("Department");

        FormLayout form = new FormLayout(firstName, lastName, email, department);

        Binder<Employee> binder = new Binder<>(Employee.class);
        binder.forField(firstName).asRequired().bind(Employee::getFirstName, Employee::setFirstName);
        binder.forField(lastName).asRequired().bind(Employee::getLastName, Employee::setLastName);
        binder.forField(email).asRequired().bind(Employee::getEmail, Employee::setEmail);
        binder.forField(department).bind(Employee::getDepartment, Employee::setDepartment);

        return new BinderCrudEditor<>(binder, form);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }

    public static class Employee {
        private String firstName;
        private String lastName;
        private String email;
        private String department;

        public Employee() {}

        public Employee(String firstName, String lastName, String email, String department) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.department = department;
        }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }
}

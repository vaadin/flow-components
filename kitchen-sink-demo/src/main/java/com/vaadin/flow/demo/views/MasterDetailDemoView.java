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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for MasterDetailLayout component.
 */
@Route(value = "master-detail", layout = MainLayout.class)
@PageTitle("Master Detail | Vaadin Kitchen Sink")
public class MasterDetailDemoView extends VerticalLayout {

    public MasterDetailDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Master Detail Layout Component"));
        add(new Paragraph("MasterDetailLayout provides a responsive master-detail pattern for data browsing."));

        // Basic master-detail layout
        MasterDetailLayout layout = new MasterDetailLayout();

        // Master: Grid with sample data
        Grid<Person> grid = new Grid<>();
        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getEmail).setHeader("Email");
        grid.addColumn(Person::getDepartment).setHeader("Department");
        grid.setItems(getSampleData());

        // Detail: Person details view
        VerticalLayout detailView = new VerticalLayout();
        detailView.addClassNames(LumoUtility.Padding.MEDIUM);

        Paragraph selectHint = new Paragraph("Select a person from the list to view details.");
        selectHint.addClassNames(LumoUtility.TextColor.SECONDARY);
        detailView.add(selectHint);

        // Selection listener to show details
        grid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresentOrElse(
                person -> {
                    detailView.removeAll();
                    detailView.add(createDetailContent(person));
                },
                () -> {
                    detailView.removeAll();
                    detailView.add(selectHint);
                }
            );
        });

        layout.setMaster(grid);
        layout.setDetail(detailView);
        layout.setWidthFull();
        layout.setHeight("500px");

        addSection("Interactive Master-Detail", layout);

        // Explanation
        Div explanation = new Div();
        explanation.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM);

        H3 featuresTitle = new H3("Features");
        Paragraph features = new Paragraph(
            "- Responsive: adapts to different screen sizes\n" +
            "- Master panel typically contains a list or grid\n" +
            "- Detail panel shows selected item's details\n" +
            "- Can be configured for different orientations\n" +
            "- Supports custom split positions"
        );
        features.getStyle().set("white-space", "pre-line");

        explanation.add(featuresTitle, features);
        addSection("Key Features", explanation);

        // Use cases
        Div useCases = new Div();
        useCases.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.MEDIUM);

        H3 useCasesTitle = new H3("Common Use Cases");
        Paragraph useCasesList = new Paragraph(
            "1. Email clients - inbox list + email viewer\n" +
            "2. File browsers - file list + file preview\n" +
            "3. CRM systems - customer list + customer details\n" +
            "4. Admin panels - data grid + edit form\n" +
            "5. Documentation - table of contents + content"
        );
        useCasesList.getStyle().set("white-space", "pre-line");

        useCases.add(useCasesTitle, useCasesList);
        addSection("Common Use Cases", useCases);
    }

    private VerticalLayout createDetailContent(Person person) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H3 name = new H3(person.getName());
        name.addClassNames(LumoUtility.Margin.NONE);

        content.add(name);
        content.add(createDetailRow("Email:", person.getEmail()));
        content.add(createDetailRow("Department:", person.getDepartment()));
        content.add(createDetailRow("Role:", person.getRole()));
        content.add(createDetailRow("Phone:", person.getPhone()));
        content.add(createDetailRow("Location:", person.getLocation()));

        Paragraph bio = new Paragraph("Bio: " + person.getBio());
        bio.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.MEDIUM);
        content.add(bio);

        return content;
    }

    private Div createDetailRow(String label, String value) {
        Div row = new Div();

        Paragraph labelP = new Paragraph(label);
        labelP.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.Margin.NONE);

        Paragraph valueP = new Paragraph(value);
        valueP.addClassNames(LumoUtility.Margin.NONE, LumoUtility.Margin.Left.SMALL);

        row.add(labelP, valueP);
        row.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL);
        return row;
    }

    private List<Person> getSampleData() {
        List<Person> people = new ArrayList<>();
        people.add(new Person("John Doe", "john.doe@example.com", "Engineering",
            "Senior Developer", "+1-555-0101", "New York",
            "Experienced full-stack developer with 10+ years in the industry."));
        people.add(new Person("Jane Smith", "jane.smith@example.com", "Marketing",
            "Marketing Manager", "+1-555-0102", "Los Angeles",
            "Creative marketing professional specializing in digital campaigns."));
        people.add(new Person("Bob Johnson", "bob.j@example.com", "Sales",
            "Sales Director", "+1-555-0103", "Chicago",
            "Results-driven sales leader with a track record of exceeding targets."));
        people.add(new Person("Alice Williams", "alice.w@example.com", "HR",
            "HR Specialist", "+1-555-0104", "Houston",
            "Passionate about building great company culture and employee experience."));
        people.add(new Person("Charlie Brown", "charlie.b@example.com", "Engineering",
            "DevOps Engineer", "+1-555-0105", "Seattle",
            "Infrastructure expert focused on automation and reliability."));
        people.add(new Person("Diana Miller", "diana.m@example.com", "Finance",
            "Financial Analyst", "+1-555-0106", "Boston",
            "Detail-oriented analyst with expertise in financial modeling."));
        return people;
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

    public static class Person {
        private String name;
        private String email;
        private String department;
        private String role;
        private String phone;
        private String location;
        private String bio;

        public Person(String name, String email, String department, String role,
                String phone, String location, String bio) {
            this.name = name;
            this.email = email;
            this.department = department;
            this.role = role;
            this.phone = phone;
            this.location = location;
            this.bio = bio;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getDepartment() { return department; }
        public String getRole() { return role; }
        public String getPhone() { return phone; }
        public String getLocation() { return location; }
        public String getBio() { return bio; }
    }
}

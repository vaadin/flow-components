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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Demo view for VirtualList component.
 */
@Route(value = "virtual-list", layout = MainLayout.class)
@PageTitle("Virtual List | Vaadin Kitchen Sink")
public class VirtualListDemoView extends VerticalLayout {

    public VirtualListDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Virtual List Component"));
        add(new Paragraph("VirtualList efficiently renders large lists using virtualization."));

        // Basic virtual list
        VirtualList<String> basic = new VirtualList<>();
        basic.setItems(generateItems(100));
        basic.setRenderer(new ComponentRenderer<>(item -> {
            Div div = new Div();
            div.setText(item);
            div.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM);
            return div;
        }));
        basic.setHeight("300px");
        basic.setWidthFull();
        addSection("Basic Virtual List (100 items)", basic);

        // Large list
        VirtualList<String> large = new VirtualList<>();
        large.setItems(generateItems(10000));
        large.setRenderer(new ComponentRenderer<>(item -> {
            Div div = new Div();
            div.setText(item);
            div.addClassNames(LumoUtility.Padding.SMALL);
            return div;
        }));
        large.setHeight("300px");
        large.setWidthFull();
        addSection("Large Virtual List (10,000 items)", large);

        // With custom renderer
        VirtualList<Person> customRenderer = new VirtualList<>();
        customRenderer.setItems(generatePeople(50));
        customRenderer.setRenderer(new ComponentRenderer<>(person -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);
            row.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Background.CONTRAST_5,
                    LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Vertical.XSMALL);
            row.setWidthFull();

            Div avatar = new Div();
            avatar.setText(person.name.substring(0, 1));
            avatar.addClassNames(LumoUtility.Background.PRIMARY, LumoUtility.TextColor.PRIMARY_CONTRAST,
                    LumoUtility.BorderRadius.LARGE, LumoUtility.Display.FLEX,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER);
            avatar.setWidth("40px");
            avatar.setHeight("40px");

            VerticalLayout info = new VerticalLayout();
            info.setPadding(false);
            info.setSpacing(false);
            Span name = new Span(person.name);
            name.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
            Span email = new Span(person.email);
            email.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
            info.add(name, email);

            row.add(avatar, info);
            return row;
        }));
        customRenderer.setHeight("350px");
        customRenderer.setWidthFull();
        addSection("With Custom Renderer", customRenderer);
    }

    private List<String> generateItems(int count) {
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            items.add("Item #" + i);
        }
        return items;
    }

    private List<Person> generatePeople(int count) {
        List<Person> people = new ArrayList<>();
        String[] firstNames = {"John", "Jane", "Bob", "Alice", "Charlie", "Diana", "Edward", "Fiona"};
        String[] lastNames = {"Doe", "Smith", "Johnson", "Williams", "Brown", "Miller", "Davis", "Garcia"};
        for (int i = 0; i < count; i++) {
            String first = firstNames[i % firstNames.length];
            String last = lastNames[i % lastNames.length];
            people.add(new Person(first + " " + last + " " + (i + 1),
                first.toLowerCase() + "." + last.toLowerCase() + i + "@example.com"));
        }
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

    private static class Person {
        String name;
        String email;

        Person(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}

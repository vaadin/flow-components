/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.listbox.demo;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.demo.data.DepartmentData;
import com.vaadin.flow.component.listbox.demo.entity.Department;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

/**
 * View for {@link ListBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-list-box")
public class ListBoxView extends DemoView {

    @Override
    public void initView() {
        basicDemo();// Basic usage
        disabledItem();
        multiSelection();
        separatorDemo();// Presentation
        customOptions();
        usingTemplateRenderer();
        styling(); // Styling
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic usage
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option one", "Option two", "Option three");
        listBox.setValue("Option one");

        add(listBox);
        // end-source-example

        addCard("Basic usage", listBox);
    }

    private void disabledItem() {
        // begin-source-example
        // source-example-heading: Disabled item
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option one", "Option two", "Option three");
        listBox.setValue("Option one");
        listBox.setItemEnabledProvider(item -> !"Option three".equals(item));

        add(listBox);
        // end-source-example

        addCard("Disabled item", listBox);
    }

    private void multiSelection() {
        // begin-source-example
        // source-example-heading: Multi select list box
        MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
        listBox.setItems("Option one", "Option two", "Option three",
                "Option four");

        add(listBox);
        // end-source-example

        addCard("Multi select list box", listBox);
    }

    private void separatorDemo() {
        // begin-source-example
        // source-example-heading: Separators
        ListBox<DayOfWeek> listBox = new ListBox<>();
        listBox.setItems(DayOfWeek.values());
        listBox.setValue(DayOfWeek.MONDAY);
        listBox.addComponents(DayOfWeek.FRIDAY, new Hr());

        add(listBox);
        // end-source-example

        addCard("Presentation", "Separators", listBox);
    }

    private void customOptions() {
        // begin-source-example
        // source-example-heading: Customizing the label
        ListBox<Employee> listBox = new ListBox<>();
        List<Employee> list = Arrays.asList(
                new Employee("Gabriella",
                        "https://randomuser.me/api/portraits/women/43.jpg"),
                new Employee("Rudi",
                        "https://randomuser.me/api/portraits/men/77.jpg"),
                new Employee("Hamsa",
                        "https://randomuser.me/api/portraits/men/35.jpg"),
                new Employee("Jacob",
                        "https://randomuser.me/api/portraits/men/76.jpg"));
        listBox.setItems(list);
        listBox.setValue(list.get(0));

        listBox.setRenderer(new ComponentRenderer<>(employee -> {
            Div text = new Div();
            text.setText(employee.getTitle());

            Image image = new Image();
            image.setWidth("21px");
            image.setHeight("21px");
            image.setSrc(employee.getImage());

            FlexLayout wrapper = new FlexLayout();
            text.getStyle().set("margin-left", "0.5em");
            wrapper.add(image, text);
            return wrapper;
        }));

        add(listBox);
        // end-source-example

        addCard("Presentation", "Customizing the label", listBox);
    }

    private List<Department> getDepartments() {

        DepartmentData departmentData = new DepartmentData();
        return departmentData.getDepartments();
    }

    private void usingTemplateRenderer() {
        // begin-source-example
        // source-example-heading: Multi-line label
        ListBox<Department> listBox = new ListBox<>();
        List<Department> listOfDepartments = getDepartments();
        listBox.setItems(listOfDepartments);
        listBox.setValue(listOfDepartments.get(0));

        listBox.setRenderer(new ComponentRenderer<>(department -> {
            Div name = new Div();
            name.getStyle().set("font-weight", "bold");
            name.setText(department.getName());

            Div description = new Div();
            description.setText(department.getDescription());
            Div div = new Div(name, description);
            return div;
        }));

        add(listBox);
        // end-source-example

        addCard("Presentation", "Multi-line label", listBox);
    }

    private void styling() {
        Div firstDiv = new Div();
        firstDiv.setText(
                "To read about styling you can read the related tutorial in");
        Anchor firstAnchor = new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes");

        Div secondDiv = new Div();
        secondDiv.setText("To know about styling in html you can read the ");
        Anchor secondAnchor = new Anchor(
                "https://vaadin.com/components/vaadin-list-box/html-examples/list-box-styling-demos",
                "HTML Styling Demos");

        HorizontalLayout firstHorizontalLayout = new HorizontalLayout(firstDiv,
                firstAnchor);
        HorizontalLayout secondHorizontalLayout = new HorizontalLayout(
                secondDiv, secondAnchor);
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example
        addCard("Styling", "Styling references", firstHorizontalLayout,
                secondHorizontalLayout);
    }

    private static class Employee {
        private String title;
        private String image;

        public Employee() {
        }

        private Employee(String title, String image) {
            this.title = title;
            this.image = image;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}

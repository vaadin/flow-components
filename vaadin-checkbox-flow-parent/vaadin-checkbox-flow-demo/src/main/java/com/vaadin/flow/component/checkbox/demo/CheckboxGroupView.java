/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.checkbox.demo.data.DepartmentData;
import com.vaadin.flow.component.checkbox.demo.entity.Department;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * View for {@link CheckboxGroup} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-group")
public class CheckboxGroupView extends DemoView {

    @Override
    protected void initView() {
        basicDemo(); // Basic Usage
        basicDemoWithCheckboxGroup();
        disabledAndDisabledItem();
        entityList();
        valueChangeEvent();
        indeterminateCheckbox();
        configurationForRequired(); // Validation
        themeVariantsHorizontal();// Theme Variants
        styling(); // Styling
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic usage
        Checkbox checkbox = new Checkbox();
        checkbox.setLabel("Option");
        checkbox.setValue(true);
        // end-source-example

        addCard("Basic usage", checkbox);
    }

    private void basicDemoWithCheckboxGroup() {
        // begin-source-example
        // source-example-heading: Basic usage with checkbox group
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Label");
        checkboxGroup.setItems("Option one", "Option two", "Option three");
        checkboxGroup.setValue(Collections.singleton("Option one"));
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        // end-source-example

        addCard("Basic usage with checkbox group", checkboxGroup);
    }

    private void disabledAndDisabledItem() {
        // begin-source-example
        // source-example-heading: Disabled state
        CheckboxGroup<String> disabledCheckGroup = new CheckboxGroup<>();
        disabledCheckGroup.setLabel("Disabled");
        disabledCheckGroup.setItems("Option one", "Option two", "Option three");
        disabledCheckGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        disabledCheckGroup.setValue(Collections.singleton("Option one"));
        disabledCheckGroup.setEnabled(false);

        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Disabled item");
        checkboxGroup.setItems("Option one", "Option two", "Option three");
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        checkboxGroup
                .setItemEnabledProvider(item -> !"Option three".equals(item));
        // end-source-example

        disabledCheckGroup.getStyle().set("margin-right", "7.5em");
        VerticalLayout verticalLayout = new VerticalLayout(disabledCheckGroup,
                checkboxGroup);
        addCard("Disabled state", verticalLayout);
    }

    private List<Department> getDepartments() {

        DepartmentData departmentData = new DepartmentData();
        return departmentData.getDepartments();
    }

    private void entityList() {
        // begin-source-example
        // source-example-heading: Entity list
        CheckboxGroup<Department> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Department");
        List<Department> departmentList = getDepartments();
        checkboxGroup.setItems(departmentList);
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        // end-source-example

        addCard("Entity list", checkboxGroup);
    }

    private void valueChangeEvent() {
        // begin-source-example
        // source-example-heading: Value change event
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Label");
        checkboxGroup.setItems("Option one", "Option two", "Option three");
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        Div value = new Div();
        value.setText("Select a value");
        checkboxGroup.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                value.setText("No option selected");
            } else {
                value.setText("Selected: " + event.getValue());
            }
        });
        // end-source-example

        addCard("Value change event", checkboxGroup, value);
    }

    private void indeterminateCheckbox() {
        // begin-source-example
        // source-example-heading: Indeterminate checkbox
        Checkbox checkbox = new Checkbox("Select all");
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        Set<String> items = new LinkedHashSet<>(
                Arrays.asList("Option one", "Option two"));
        checkboxGroup.setItems(items);
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        checkboxGroup.addValueChangeListener(event -> {
            if (event.getValue().size() == items.size()) {
                checkbox.setValue(true);
                checkbox.setIndeterminate(false);
            } else if (event.getValue().size() == 0) {
                checkbox.setValue(false);
                checkbox.setIndeterminate(false);
            } else
                checkbox.setIndeterminate(true);

        });
        checkbox.addValueChangeListener(event -> {

            if (checkbox.getValue()) {
                checkboxGroup.setValue(items);
            } else {
                checkboxGroup.deselectAll();
            }
        });
        checkboxGroup.setValue(Collections.singleton("Option one"));
        // end-source-example

        addCard("Indeterminate checkbox", checkbox, checkboxGroup);
    }

    private void configurationForRequired() {
        // begin-source-example
        // source-example-heading: Required
        Employee employee = new Employee();
        Binder<Employee> binder = new Binder<>();

        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setLabel("Employee titles");
        checkboxGroup.setItems("Account Manager", "Designer",
                "Marketing Manager", "Developer");
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        binder.forField(checkboxGroup)
                .asRequired("Please choose employee titles")
                .bind(Employee::getTitles, Employee::setTitles);

        Button button = new Button("Submit", event -> {
            if (binder.writeBeanIfValid(employee)) {
                Notification.show("Submit successful", 2000,
                        Notification.Position.MIDDLE);
            }
        });
        // end-source-example

        checkboxGroup.getStyle().set("margin-right", "5.5em");
        HorizontalLayout layout = new HorizontalLayout(checkboxGroup, button);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        addCard("Validation", "Required", layout);

    }

    private void themeVariantsHorizontal() {
        // begin-source-example
        // source-example-heading: Orientation
        CheckboxGroup<String> horizontal = new CheckboxGroup<>();
        horizontal.setLabel("Horizontal");
        horizontal.setItems("Option one", "Option two", "Option three");
        horizontal.setValue(Collections.singleton("Option one"));

        CheckboxGroup<String> vertical = new CheckboxGroup<>();
        vertical.setLabel("Vertical");
        vertical.setItems("Option one", "Option two", "Option three");
        vertical.setValue(Collections.singleton("Option one"));
        vertical.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        // end-source-example

        addCard("Theme variants", "Orientation", horizontal, vertical);
    }

    private void styling() {
        Paragraph p1 = new Paragraph(
                "To read about styling you can read the related tutorial ");
        p1.add(new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes"));

        Paragraph p2 = new Paragraph(
                "To know about styling in HTML you can read the ");
        p2.add(new Anchor("https://vaadin.com/components/"
                + "vaadin-checkbox/html-examples/checkbox-styling-demos",
                "HTML Styling Demos"));
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example
        addCard("Styling", "Styling references", p1, p2);
    }

    private static class Employee {
        private Set<String> titles;

        public Set<String> getTitles() {
            return titles;
        }

        public void setTitles(Set<String> titles) {
            this.titles = titles;
        }
    }
}

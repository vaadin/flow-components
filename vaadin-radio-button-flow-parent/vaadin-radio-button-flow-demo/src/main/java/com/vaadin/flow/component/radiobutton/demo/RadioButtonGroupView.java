/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.radiobutton.demo.data.DepartmentData;
import com.vaadin.flow.component.radiobutton.demo.entity.Department;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("vaadin-radio-button")
public class RadioButtonGroupView extends DemoView {

    @Override
    protected void initView() {
        basicDemo(); // Basic Usage
        helperDemo();
        entityList();
        disabledAndDisabledItem();
        valueChangeEvent();
        configurationForReqired(); // Validation
        customOptions(); // Presentation
        usingTemplateRenderer();
        themeVariantsHorizontal();// Theme Variants
        themeVariantsHelperText();
        styling(); // Styling
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic usage
        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Label");
        radioGroup.setItems("Option one", "Option two", "Option three");
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setValue("Option one");

        add(radioGroup);
        // end-source-example

        addCard("Basic usage", radioGroup);
    }

    private void helperDemo() {
        // begin-source-example
        // source-example-heading: Helper text
        RadioButtonGroup<String> radioGroupHelperText = new RadioButtonGroup<>();
        radioGroupHelperText.setLabel("Label");
        radioGroupHelperText.setItems("Option one", "Option two",
                "Option three");
        radioGroupHelperText.setHelperText("Helper text");

        RadioButtonGroup<String> radioGroupHelperComponent = new RadioButtonGroup<>();
        radioGroupHelperComponent.setLabel("Label");
        radioGroupHelperComponent.setItems("Option one", "Option two",
                "Option three");
        radioGroupHelperComponent.setHelperComponent(new Span("Helper text"));
        ;

        add(radioGroupHelperText, radioGroupHelperComponent);
        // end-source-example

        addCard("Helper text", radioGroupHelperText, radioGroupHelperComponent);
    }

    private void entityList() {
        // begin-source-example
        // source-example-heading: Entity list
        RadioButtonGroup<Department> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Department");
        List<Department> departmentList = getDepartments();
        radioGroup.setItems(departmentList);
        radioGroup.setRenderer(new TextRenderer<>(Department::getName));
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        add(radioGroup);
        // end-source-example

        addCard("Entity list", radioGroup);
    }

    private void disabledAndDisabledItem() {
        // begin-source-example
        // source-example-heading: Disabled state
        RadioButtonGroup<String> disabledRadioGroup = new RadioButtonGroup<>();
        disabledRadioGroup.setLabel("Disabled");
        disabledRadioGroup.setItems("Option one", "Option two", "Option three");
        disabledRadioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        disabledRadioGroup.setValue("Option one");
        disabledRadioGroup.setEnabled(false);

        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Disabled item");
        radioGroup.setItems("Option one", "Option two", "Option three");
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItemEnabledProvider(item -> !"Option three".equals(item));

        add(disabledRadioGroup, radioGroup);
        // end-source-example

        disabledRadioGroup.getStyle().set("margin-right", "7.5em");
        VerticalLayout verticalLayout = new VerticalLayout(disabledRadioGroup,
                radioGroup);
        addCard("Disabled state", verticalLayout);
    }

    private List<Department> getDepartments() {

        DepartmentData departmentData = new DepartmentData();
        return departmentData.getDepartments();
    }

    private void valueChangeEvent() {
        // begin-source-example
        // source-example-heading: Value change event
        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Label");
        radioGroup.setItems("Option one", "Option two", "Option three");
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        Div value = new Div();
        value.setText("Select a value");
        radioGroup.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                value.setText("No option selected");
            } else {
                value.setText("Selected: " + event.getValue());
            }
        });

        add(radioGroup, value);
        // end-source-example

        addCard("Value change event", radioGroup, value);
    }

    private void configurationForReqired() {
        // begin-source-example
        // source-example-heading: Required
        Employee employee = new Employee();
        Binder<Employee> binder = new Binder<>();

        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("Employee title");
        radioGroup.setItems("Account Manager", "Designer", "Marketing Manager",
                "Developer");
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        binder.forField(radioGroup)
                .asRequired("Please choose an employee title")
                .bind(Employee::getTitle, Employee::setTitle);

        Button button = new Button("Submit", event -> {
            if (binder.writeBeanIfValid(employee)) {
                Notification.show("Submit successful", 2000,
                        Notification.Position.MIDDLE);
            }
        });

        add(radioGroup, button);
        // end-source-example

        radioGroup.getStyle().set("margin-right", "5.5em");
        HorizontalLayout layout = new HorizontalLayout(radioGroup, button);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        addCard("Validation", "Required", layout);

    }

    private void customOptions() {
        // begin-source-example
        // source-example-heading: Customizing radio button label
        RadioButtonGroup<Employee> radioButton = new RadioButtonGroup<>();
        radioButton.setLabel("Employee");
        radioButton.setItems(
                new Employee("Gabriella",
                        "https://randomuser.me/api/portraits/women/43.jpg"),
                new Employee("Rudi",
                        "https://randomuser.me/api/portraits/men/77.jpg"),
                new Employee("Hamsa",
                        "https://randomuser.me/api/portraits/men/35.jpg"),
                new Employee("Jacob",
                        "https://randomuser.me/api/portraits/men/76.jpg"));
        radioButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        radioButton.setRenderer(new ComponentRenderer<>(employee -> {
            Div title = new Div();
            title.setText(employee.getTitle());

            Image image = new Image();
            image.setWidth("21px");
            image.setHeight("21px");
            image.setSrc(employee.getImage());

            FlexLayout wrapper = new FlexLayout();
            title.getStyle().set("margin-left", "0.5em");
            wrapper.add(image, title);
            return wrapper;
        }));

        add(radioButton);
        // end-source-example

        addCard("Presentation", "Customizing radio button label", radioButton);
    }

    private void usingTemplateRenderer() {
        // begin-source-example
        // source-example-heading: Multi-line label
        RadioButtonGroup<Department> radioGroup = new RadioButtonGroup<>();
        List<Department> listOfDepartments = getDepartments();
        radioGroup.setItems(listOfDepartments);
        radioGroup.setLabel("Department");
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        radioGroup.setRenderer(new ComponentRenderer<>(department -> {
            Div name = new Div();
            name.getStyle().set("font-weight", "bold");
            name.setText(department.getName());

            Div description = new Div();
            description.setText(department.getDescription());
            Div div = new Div(name, description);
            return div;
        }));

        add(radioGroup);
        // end-source-example

        addCard("Presentation", "Multi-line label", radioGroup);
    }

    private void themeVariantsHorizontal() {
        // begin-source-example
        // source-example-heading: Direction
        RadioButtonGroup<String> horizontal = new RadioButtonGroup<>();
        horizontal.setLabel("Horizontal");
        horizontal.setItems("Option one", "Option two", "Option three");
        horizontal.setValue("Option one");

        RadioButtonGroup<String> vertical = new RadioButtonGroup<>();
        vertical.setLabel("Vertical");
        vertical.setItems("Option one", "Option two", "Option three");
        vertical.setValue("Option one");
        vertical.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        add(horizontal, vertical);
        // end-source-example

        addCard("Theme variants", "Direction", horizontal, vertical);
    }

    private void themeVariantsHelperText() {
        // begin-source-example
        // source-example-heading: Helper text position
        RadioButtonGroup<String> below = new RadioButtonGroup<>();
        below.setLabel("Label");
        below.setItems("Option one", "Option two", "Option three");
        below.setValue("Option one");
        below.setHelperText("Helper text below");

        RadioButtonGroup<String> above = new RadioButtonGroup<>();
        above.setLabel("Label");
        above.setItems("Option one", "Option two", "Option three");
        above.setValue("Option one");
        above.setHelperText("Helper text above");
        above.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);

        add(below, above);
        // end-source-example

        addCard("Theme variants", "Helper text position", below, above);
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
                + "vaadin-radio-button/html-examples/radio-button-styling-demos",
                "HTML Styling Demos"));
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example
        addCard("Styling", "Styling references", p1, p2);
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

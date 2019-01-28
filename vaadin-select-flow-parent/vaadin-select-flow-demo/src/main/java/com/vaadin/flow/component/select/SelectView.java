package com.vaadin.flow.component.select;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-select")
public class SelectView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
        configurationDemo();
        customizeLabelDemo();
        formFieldDemo();
        customOptionsDemo();
        customIconDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        Select<String> select = new Select<>();
        select.setItems("Jose", "Manolo", "Pedro");

        Span value = new Span("Select a value");
        select.addValueChangeListener(event -> value.setText("Selected: " + event.getValue()));
        // end-source-example

        HorizontalLayout horizontalLayout = new HorizontalLayout(select, value);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        addCard("Basic Usage", horizontalLayout);
    }

    private void configurationDemo() {
        // begin-source-example
        // source-example-heading: Configuring the Select
        Select<String> select = new Select<>("Option one", "Option two");
        select.setPlaceholder("Placeholder");
        select.setLabel("Label");

        Select<String> disabledSelect = new Select<>("Option one", "Option two");
        disabledSelect.setEnabled(false);
        disabledSelect.setLabel("Disabled");

        Select<String> readOnlySelect = new Select<>("Option one", "Option two");
        readOnlySelect.setReadOnly(true);
        readOnlySelect.setValue("Option one");
        readOnlySelect.setLabel("Read-only");

        Select<String> requiredSelect = new Select<>();
        requiredSelect.setRequiredIndicatorVisible(true);
        requiredSelect.setLabel("Required");

        requiredSelect.setItems("Option one", "Option two", "Option three", "Option four");

        // The empty selection item is the first item that maps to an null item.
        // As the item is not selectable, using it also as placeholder
        requiredSelect.setPlaceholder("Select an option");
        requiredSelect.setEmptySelectionCaption("Select an option");
        requiredSelect.setEmptySelectionAllowed(true);
        requiredSelect.setItemEnabledProvider(Objects::nonNull);

        // add a divider after the empty selection item
        requiredSelect.addComponents(null, new Hr());

        FlexLayout layout = new FlexLayout(select, disabledSelect, readOnlySelect,
                requiredSelect);
        layout.getStyle().set("flex-wrap", "wrap");
        // end-source-example

        addCard("Configuring the Select", layout);
    }

    private void customizeLabelDemo() {
        // begin-source-example
        // source-example-heading: Customize the Label of Selected Value
        List<Locale> locales = Arrays.asList(new Locale("Other"),
                new Locale("es"),
                new Locale("fi"),
                new Locale("en"));

        Select<Locale> select = new Select<>();
        select.setPlaceholder("Language");
        select.setTextRenderer(Locale::getDisplayLanguage);
        select.setItemLabelGenerator(locale -> {
            if ("Other".equalsIgnoreCase(locale.getLanguage())) {
                // Empty label string will show the placeholder
                return "";
            } else {
                return locale.getISO3Language();
            }
        });
        select.setItems(locales);
        // end-source-example

        addCard("Customize the Label of Selected Value", select);
    }

    private void formFieldDemo() {
        // begin-source-example
        // source-example-heading: Using as a Form Field
        Employee employee = new Employee();
        Binder<Employee> binder = new Binder<>();

        Select<String> titleSelect = new Select<>();
        titleSelect.setLabel("Title");
        titleSelect.setItems("Account Manager", "Designer", "Marketing Manager", "Developer");

        titleSelect.setEmptySelectionAllowed(true);
        titleSelect.setEmptySelectionCaption("Select you title");
        titleSelect.addComponents(null, new Hr());

        binder.forField(titleSelect)
                .asRequired("Please choose the option closest to your profession")
                .bind(Employee::getTitle, Employee::setTitle);
        // Note that another option of handling the unselected item (null) is:
        // binding.withNullRepresentation("Select your title")
        // That only works when the select item type is String.

        Button button = new Button("Submit", event -> {
            if (binder.writeBeanIfValid(employee)) {
                Notification.show("Submit successful", 2000, Notification.Position.MIDDLE);
            }
        });

        HorizontalLayout layout = new HorizontalLayout(titleSelect, button);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        // end-source-example

        addCard("Form field", "Using as a Form Field", layout);
    }

    private void customOptionsDemo() {
        // begin-source-example
        // source-example-heading: Customizing drop down options
        Select<Emotion> select = new Select<>();
        select.setLabel("How are you feeling today?");

        select.setItems(new Emotion("Good", VaadinIcon.THUMBS_UP),
                new Emotion("Bad", VaadinIcon.THUMBS_DOWN),
                new Emotion("Meh", VaadinIcon.MEH_O),
                new Emotion("This is fine", VaadinIcon.FIRE));

        select.setRenderer(new ComponentRenderer<>(emotion -> {
            Div text = new Div();
            text.getStyle().set("text-align", "right");
            text.setText(emotion.getText());

            FlexLayout wrapper = new FlexLayout();
            wrapper.setFlexGrow(1, text);
            wrapper.add(emotion.getIcon().create(), text);
            return wrapper;
        }));

        // Note that if the setItemLabelGenerator(...) is applied, the label
        // string is shown in the input field instead of the components (icon + text)
        // end-source-example

        addCard("Customizing", "Customizing drop down options", select);
    }

    private void customIconDemo() {
        // begin-source-example
        // source-example-heading: Custom Icon
        Select<String> select = new Select<>();
        select.setItems("Jose", "Manolo", "Pedro");

        Div iconWrapper = new Div();
        iconWrapper.add(VaadinIcon.USER.create());

        select.addToPrefix(iconWrapper);
        // end-source-example

        addCard("Customizing", "Custom Icon", select);
    }

    private static class Employee {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    private static class Emotion {
        private String text;
        private VaadinIcon icon;

        private Emotion(String text, VaadinIcon icon) {
            this.text = text;
            this.icon = icon;
        }

        public String getText() {
            return text;
        }

        public VaadinIcon getIcon() {
            return icon;
        }
    }
}

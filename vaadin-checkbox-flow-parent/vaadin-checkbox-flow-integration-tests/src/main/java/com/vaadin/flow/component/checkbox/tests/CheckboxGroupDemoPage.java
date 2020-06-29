package com.vaadin.flow.component.checkbox.tests;

import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.checkbox.GeneratedVaadinCheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link CheckboxGroup} integration tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-group-test-demo")
public class CheckboxGroupDemoPage extends DemoView {


    public static class Person {

        private String name;
        private int id;

        public Person(String name) {
            this.name = name;
        }

        public Person(int id, String name) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    @Override
    protected void initView() {
        addBasicFeatures();
        addComponentWithLabelAndErrorMessage();
        addItemLabelGenerator();
        addDisabled();
        addDisabledItems();
        addReadOnlyGroup();
        addComponentWithThemeVariant();
    }

    @Override
    public void populateSources() {
        // The body of this method is kept empty because no source population
        // is needed for integration tests. CheckboxGroupDemoPage is only used for testing.
        // Old demos have been moved to integration tests and separated from demos.
    }

    private void addBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic checkbox group
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setDataSource("foo", "bar", "baz");
        group.addValueChangeListener(event -> message.setText(String.format(
                "Checkbox group value changed from '%s' to '%s'",
                toString(event.getOldValue()), toString(event.getValue()))));
        // end-source-example

        group.setId("checkbox-group-with-value-change-listener");
        message.setId("checkbox-group-value");

        addCard("Basic checkbox group", group, message);
    }

    private void addComponentWithLabelAndErrorMessage() {
        // begin-source-example
        // source-example-heading: Group with label and error message
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setDataSource("foo", "bar", "baz");
        group.setLabel("Group label");
        group.setErrorMessage("Field has been set to invalid from server side");
        NativeButton button = new NativeButton("Switch validity state",
                event -> group.setInvalid(!group.isInvalid()));

        // end-source-example
        group.setId("group-with-label-and-error-message");
        button.setId("group-with-label-button");
        addCard("Group with label and error message", group, button);
    }

    private void addItemLabelGenerator() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Checkbox group with label generator
        CheckboxGroup<Person> group = new CheckboxGroup<>();
        group.setDataSource(new Person("Joe"), new Person("John"),
                new Person("Bill"));
        group.setItemLabelGenerator(Person::getName);
        group.addValueChangeListener(event -> message.setText(String.format(
                "Checkbox group value changed from '%s' to '%s'",
                getNames(event.getOldValue()), getNames(event.getValue()))));
        // end-source-example

        group.setId("checkbox-group-with-item-generator");
        message.setId("checkbox-group-gen-value");

        addCard("Checkbox group with label generator", group, message);
    }

    private void addDisabled() {

        // begin-source-example
        // source-example-heading: Disabled checkbox group
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setDataSource("foo", "bar", "baz");
        group.setEnabled(false);
        // end-source-example

        group.setId("checkbox-group-disabled");

        addCard("Disabled checkbox group", group);
    }

    private void addDisabledItems() {

        Div valueInfo = new Div();
        // begin-source-example
        // source-example-heading: Checkbox group with item enabled
        // provider
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setDataSource("foo", "bar", "baz");
        group.setItemEnabledProvider(item -> !"bar".equals(item));
        // end-source-example

        group.addValueChangeListener(
                event -> valueInfo.setText(toString(group.getValue())));

        group.setId("checkbox-group-disabled-items");
        valueInfo.setId("checkbox-group-disabled-items-info");

        addCard("Checkbox group with item enabled provider", group, valueInfo);
    }

    private void addReadOnlyGroup() {
        // begin-source-example
        // source-example-heading: Read-only checkbox group
        Div valueInfo = new Div();

        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setDataSource("foo", "bar", "baz");
        group.setReadOnly(true);

        NativeButton button = new NativeButton("Switch read-only state",
                event -> group.setReadOnly(!group.isReadOnly()));
        group.addValueChangeListener(
                event -> valueInfo.setText(toString(group.getValue())));
        // end-source-example

        group.setId("checkbox-group-read-only");
        valueInfo.setId("selected-value-info");
        button.setId("switch-read-only");

        addCard("Read-only checkbox group", group, button, valueInfo);
    }

    private void addComponentWithThemeVariant() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setDataSource("foo", "bar", "baz");
        group.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        // end-source-example

        addVariantsDemo(() -> group,
                GeneratedVaadinCheckboxGroup::addThemeVariants,
                GeneratedVaadinCheckboxGroup::removeThemeVariants,
                CheckboxGroupVariant::getVariantName,
                CheckboxGroupVariant.LUMO_VERTICAL);
    }

    private String toString(Set<String> value) {
        return value.stream().sorted().collect(Collectors.toList()).toString();
    }

    private String getNames(Set<Person> persons) {
        return persons.stream().map(Person::getName).sorted()
                .collect(Collectors.toList()).toString();
    }

}

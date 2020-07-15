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
package com.vaadin.flow.component.combobox.test;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.test.template.ComboBoxInATemplate;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

@Route("lazy-loading")
public class LazyLoadingPage extends Div {

    private Div message = new Div();

    public LazyLoadingPage() {
        message.setId("message");
        add(message);

        addSeparator();
        createListDataProviderWithStrings();
        addSeparator();
        createComboBoxWithCustomPageSize();
        addSeparator();
        createListDataProviderWithBeans();
        addSeparator();
        createDataProviderWithCustomItemFilter();
        addSeparator();
        createCallbackDataProvider();
        addSeparator();
        createComboBoxInATemplate();
        addSeparator();
        createCallbackDataProviderWhichReturnsZeroItems();
        createComboBoxWithCustomPageSizeAndLazyLoading();
    }

    private void createListDataProviderWithStrings() {
        addTitle("ListDataProvider with strings");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("lazy-strings");

        List<String> items = generateStrings(1000);
        ListDataProvider<String> dp = DataProvider.ofCollection(items);
        comboBox.setDataProvider(dp);

        comboBox.addValueChangeListener(e -> message.setText(e.getValue()));

        NativeButton setButton = new NativeButton("set value", e -> {
            comboBox.setValue(items.get(10));
        });
        setButton.setId("set-value");

        NativeButton disableButton = new NativeButton("set disabled", e -> {
            comboBox.setEnabled(false);
        });
        disableButton.setId("disable");

        NativeButton setCurrentValueButton = new NativeButton(
                "set current value", e -> {
                    comboBox.setValue(comboBox.getValue());
                });
        setCurrentValueButton.setId("set-current-value");

        add(comboBox, setButton, disableButton, setCurrentValueButton);
    }

    private void createComboBoxWithCustomPageSize() {
        addTitle("ListDataProvider with custom page size 180");
        ComboBox<String> comboBox = new ComboBox<>(180);
        comboBox.setId("pagesize");
        comboBox.setDataProvider(
                DataProvider.ofCollection(generateStrings(1000)));

        NativeButton pageSizeButton = new NativeButton("set pagesize 100",
                e -> comboBox.setPageSize(100));
        pageSizeButton.setId("change-pagesize");

        add(comboBox, pageSizeButton);
    }

    private void createListDataProviderWithBeans() {
        addTitle("ListDataProvider with beans");
        ComboBox<Person> comboBox = new ComboBox<>();
        comboBox.setId("lazy-beans");

        List<Person> people = IntStream.range(0, 987)
                .mapToObj(i -> new Person("Person " + i, i))
                .collect(Collectors.toList());
        ListDataProvider<Person> personDataProvider = new ListDataProvider<>(
                people);

        comboBox.setDataProvider(personDataProvider);

        NativeButton setButton = new NativeButton("set value",
                e -> comboBox.setValue(people.get(3)));
        setButton.setId("set-bean-value");

        NativeButton componentRendererButton = new NativeButton("set renderer",
                e -> comboBox.setRenderer(new ComponentRenderer<H4, Person>(
                        person -> new H4(person.getName()))));
        componentRendererButton.setId("component-renderer");

        NativeButton itemLabelGeneratorButton = new NativeButton(
                "change item label generator",
                e -> comboBox.setItemLabelGenerator(
                        person -> "Born " + person.getBorn()));
        itemLabelGeneratorButton.setId("item-label-generator");

        List<Person> altPeople = IntStream.range(0, 220)
                .mapToObj(i -> new Person("Changed " + i, 2000 + i))
                .collect(Collectors.toList());
        ListDataProvider<Person> altPersonDataProvider = new ListDataProvider<>(
                altPeople);
        NativeButton dataProviderButton = new NativeButton(
                "Change data provider",
                e -> comboBox.setDataProvider(altPersonDataProvider));
        dataProviderButton.setId("data-provider");

        NativeButton updateButton = new NativeButton("Update first item", e -> {
            people.get(0).setName("Updated");
            personDataProvider.refreshItem(people.get(0));
        });
        updateButton.setId("update-item");

        NativeButton removeButton = new NativeButton("Remove third item", e -> {
            people.remove(2);
            personDataProvider.refreshAll();
        });
        removeButton.setId("remove-item");

        add(comboBox, setButton, componentRendererButton,
                itemLabelGeneratorButton, dataProviderButton, updateButton,
                removeButton);
    }

    private void createDataProviderWithCustomItemFilter() {
        addTitle("ListDataProvider with custom item filter");
        ComboBox<Person> comboBox = new ComboBox<>();
        comboBox.setId("custom-filter");

        List<Person> people = IntStream.range(0, 500)
                .mapToObj(i -> new Person("Person", i))
                .collect(Collectors.toList());
        ListDataProvider<Person> personDataProvider = new ListDataProvider<>(
                people);

        comboBox.setRenderer(new ComponentRenderer<Div, Person>(person -> {
            return new Div(new H4(person.getName()),
                    new Label("Born: " + person.getBorn()));
        }));

        comboBox.setDataProvider((person, filter) -> String
                .valueOf(person.getBorn()).startsWith(filter),
                personDataProvider);
        add(comboBox);
    }

    private void createCallbackDataProvider() {
        addTitle("CallbackDataProvider");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("callback-dataprovider");

        CallbackDataProvider<String, String> dataProvider = new CallbackDataProvider<String, String>(
                query -> IntStream
                        .range(query.getOffset(),
                                query.getOffset() + query.getLimit())
                        .mapToObj(i -> "Item " + i),
                query -> 210);

        comboBox.setDataProvider(dataProvider);

        add(comboBox);
    }

    private void createComboBoxInATemplate() {
        addTitle("Combobox inside a template");
        ComboBoxInATemplate comboBoxInATemplate = new ComboBoxInATemplate();
        comboBoxInATemplate.setId("template");
        ComboBox<String> comboBox = comboBoxInATemplate.getComboBox();

        comboBox.setDataProvider(
                DataProvider.ofCollection(generateStrings(1000)));
        comboBox.addValueChangeListener(e -> message.setText(e.getValue()));

        add(comboBoxInATemplate);
    }

    private void createCallbackDataProviderWhichReturnsZeroItems() {
        addTitle("Callback data provider returns zero items");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("empty-callback");

        List<String> items = Collections.emptyList();
        CallbackDataProvider.FetchCallback<String, String> fetch = query -> items
                .stream().limit(query.getLimit()).skip(query.getOffset());
        CallbackDataProvider.CountCallback<String, String> count = query -> 0;
        comboBox.setDataProvider(
                DataProvider.fromFilteringCallbacks(fetch, count));

        add(comboBox);
    }

    private void createComboBoxWithCustomPageSizeAndLazyLoading() {
        addTitle("Callback data provider with custom page size 42");
        ComboBox<String> comboBox = new ComboBox<>(42);
        comboBox.setId("lazy-custom-page-size");

        comboBox.setDataProvider(
                DataProvider.fromFilteringCallbacks(query -> {
                    Stream<String> stream = IntStream.of(0, 100)
                            .mapToObj(String::valueOf);
                    message.setText(String.valueOf(query.getPageSize()));
                    return stream.skip(query.getOffset())
                            .limit(query.getLimit());
                }, query -> {
                    Stream<String> stream = IntStream.of(0, 100)
                            .mapToObj(String::valueOf);
                    return (int) stream.limit(query.getLimit()).count();
                }));

        add(comboBox);
    }

    public static List<String> generateStrings(int count) {
        List<String> items = IntStream.range(0, count)
                .mapToObj(i -> "Item " + i).collect(Collectors.toList());
        return items;
    }

    private void addSeparator() {
        getElement().appendChild(new Element("hr"));
    }

    private void addTitle(String title) {
        add(new Paragraph(title));
    }

    public static class Person implements Serializable {
        private String name;
        private final int born;

        public Person(String name, int born) {
            this.name = name;
            this.born = born;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBorn() {
            return born;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link VirtualList}
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-virtual-list/virtual-list-test")
public class VirtualListPage extends Div {

    private static final List<String> ITEMS;
    static {
        ITEMS = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            ITEMS.add("Item " + (i + 1));
        }
    }

    public static class Person {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    /**
     * Creates all the components needed for the tests.
     */
    public VirtualListPage() {
        createListWithStrings();
        createDataProviderWithStrings();
        createTemplateFromValueProviderWithPeople();
        createTemplateFromRendererWithPeople();
        createLazyLoadingDataProvider();
        createTemplateWithEventHandlers();
        createListWithComponentRenderer();
        createListWithComponentRendererWithBeansAndPlaceholder();
        createDetachableList();
        createListsWithBasicRenderers();
        createListInsideFlexContainer();
    }

    private void createListWithStrings() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("100px");

        list.setItems("Item 1", "Item 2", "Item 3");

        NativeButton setListWith3Items = new NativeButton("Change list 1",
                evt -> list.setItems("Item 1", "Item 2", "Item 3"));
        NativeButton setListWith2Items = new NativeButton("Change list 2",
                evt -> list.setItems("Another item 1", "Another item 2"));
        NativeButton setEmptyList = new NativeButton("Change list 3",
                evt -> list.setItems());

        list.setId("list-with-strings");
        setListWith3Items.setId("list-with-strings-3-items");
        setListWith2Items.setId("list-with-strings-2-items");
        setEmptyList.setId("list-with-strings-0-items");

        add(list, setListWith3Items, setListWith2Items, setEmptyList);
    }

    private void createDataProviderWithStrings() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("100px");

        DataProvider<String, ?> dataProvider1 = DataProvider.ofItems("Item 1",
                "Item 2", "Item 3");
        DataProvider<String, ?> dataProvider2 = DataProvider
                .ofItems("Another item 1", "Another item 2");
        DataProvider<String, ?> dataProvider3 = DataProvider.ofItems();

        list.setDataProvider(dataProvider1);

        NativeButton setProviderWith3Items = new NativeButton(
                "Change dataprovider 1",
                evt -> list.setDataProvider(dataProvider1));
        NativeButton setProviderWith2Items = new NativeButton(
                "Change dataprovider 2",
                evt -> list.setDataProvider(dataProvider2));
        NativeButton setEmptyProvider = new NativeButton(
                "Change dataprovider 3",
                evt -> list.setDataProvider(dataProvider3));

        list.setId("dataprovider-with-strings");
        setProviderWith3Items.setId("dataprovider-with-strings-3-items");
        setProviderWith2Items.setId("dataprovider-with-strings-2-items");
        setEmptyProvider.setId("dataprovider-with-strings-0-items");

        add(list, setProviderWith3Items, setProviderWith2Items,
                setEmptyProvider);
    }

    private void createTemplateFromValueProviderWithPeople() {
        VirtualList<Person> list = new VirtualList<>();
        list.setHeight("100px");

        DataProvider<Person, ?> dataProvider1 = DataProvider
                .ofCollection(createPeople(3));
        DataProvider<Person, ?> dataProvider2 = DataProvider
                .ofCollection(createPeople(2));
        DataProvider<Person, ?> dataProvider3 = DataProvider
                .ofCollection(createPeople(0));

        list.setDataProvider(dataProvider1);
        list.setRenderer(Person::getName);

        NativeButton setProviderWith3Items = new NativeButton(
                "Change dataprovider 1", evt -> {
                    list.setRenderer(Person::getName);
                    list.setDataProvider(dataProvider1);
                });
        NativeButton setProviderWith2Items = new NativeButton(
                "Change dataprovider 2", evt -> {
                    list.setDataProvider(dataProvider2);
                    list.setRenderer(person -> String.valueOf(person.getAge()));
                });
        NativeButton setEmptyProvider = new NativeButton(
                "Change dataprovider 3",
                evt -> list.setDataProvider(dataProvider3));

        list.setId("dataprovider-with-people");
        setProviderWith3Items.setId("dataprovider-with-people-3-items");
        setProviderWith2Items.setId("dataprovider-with-people-2-items");
        setEmptyProvider.setId("dataprovider-with-people-0-items");

        add(list, setProviderWith3Items, setProviderWith2Items,
                setEmptyProvider);
    }

    private void createTemplateFromRendererWithPeople() {
        VirtualList<Person> list = new VirtualList<>();
        list.setHeight("100px");

        List<Person> people = createPeople(3);
        DataProvider<Person, ?> dataProvider = DataProvider
                .ofCollection(people);

        list.setDataProvider(dataProvider);
        list.setRenderer(TemplateRenderer
                .<Person> of("[[item.name]] - [[item.age]] - [[item.user]]")
                .withProperty("name", Person::getName)
                .withProperty("age", Person::getAge)
                .withProperty("user", person -> person.getName().toLowerCase()
                        .replace(" ", "_")));

        NativeButton update = new NativeButton("Update item 1", evt -> {
            Person item = people.get(0);
            item.setName(item.getName() + " Updated");
            list.getDataProvider().refreshItem(item);
        });

        list.setId("template-renderer-with-people");
        update.setId("template-renderer-with-people-update-item");

        add(list, update);
    }

    private void createLazyLoadingDataProvider() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("100px");

        Div message = new Div();

        DataProvider<String, ?> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    List<String> result = queryStrings(query);
                    message.setText("Sent " + result.size() + " items");
                    return result.stream();
                }, this::countStrings);

        list.setDataProvider(dataProvider);

        list.setId("lazy-loaded");
        message.setId("lazy-loaded-message");

        add(list, message);
    }

    private void createTemplateWithEventHandlers() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("100px");

        Div message = new Div();

        List<String> items = new ArrayList<>(Arrays.asList("Clickable item 1",
                "Clickable item 2", "Clickable item 3"));

        list.setRenderer(TemplateRenderer.<String> of(
                "<div on-click='remove' id='template-events-item-[[index]]'>[[item.label]]</div>")
                .withProperty("label", ValueProvider.identity())
                .withEventHandler("remove", item -> {
                    items.remove(item);
                    list.getDataCommunicator().reset();
                    message.setText(item + " removed");
                }));

        list.setItems(items);
        list.setId("template-events");
        message.setId("template-events-message");

        add(list, message);
    }

    private void createListWithComponentRenderer() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("100px");

        List<String> items = IntStream.range(1, 101).mapToObj(i -> "Item " + i)
                .collect(Collectors.toList());

        list.setRenderer(new ComponentRenderer<>(item -> {
            Div text = new Div(new Text(item));
            text.addClassName("component-rendered");
            text.setHeight("18px");
            return text;
        }));

        list.setItems(items);
        list.setId("component-renderer");

        add(list);
    }

    private void createListWithComponentRendererWithBeansAndPlaceholder() {
        VirtualList<Person> list = new VirtualList<>();
        list.setHeight("100px");

        List<Person> people = createPeople(100);

        list.setRenderer(new ComponentRenderer<Div, Person>(person -> {
            Div text = new Div(new Text(person.getName()));
            text.addClassName("component-rendered");
            return text;
        }));

        list.setItems(people);
        list.setId("component-renderer-with-beans");

        Person placeholder = new Person();
        placeholder.setName("the-placeholder");
        list.setPlaceholderItem(placeholder);

        add(list);
    }

    private void createDetachableList() {
        Div container1 = new Div(new Text("Container 1"));
        container1.setId("detachable-list-container-1");
        Div container2 = new Div(new Text("Container 2"));
        container2.setId("detachable-list-container-2");

        VirtualList<Person> list = new VirtualList<>();
        list.setId("detachable-list");

        list.setItems(createPeople(20));
        list.setRenderer(Person::getName);
        container1.add(list);
        add(container1);
        NativeButton detach = new NativeButton("Detach list",
                e -> list.getParent().ifPresent(
                        parent -> ((HasComponents) parent).remove(list)));
        detach.setId("detachable-list-detach");
        NativeButton attach1 = new NativeButton("Attach list to container 1",
                e -> container1.add(list));
        attach1.setId("detachable-list-attach-1");
        NativeButton attach2 = new NativeButton("Attach list to container 2",
                e -> container2.add(list));
        attach2.setId("detachable-list-attach-2");
        NativeButton invisible = new NativeButton("Set list invisble",
                e -> list.setVisible(false));
        invisible.setId("detachable-list-invisible");
        NativeButton visible = new NativeButton("Set list visible",
                e -> list.setVisible(true));
        visible.setId("detachable-list-visible");
        add(container1, container2, detach, attach1, attach2, invisible,
                visible);
    }

    private void createListsWithBasicRenderers() {
        VirtualList<Person> listWithButtons = createVirtualList(
                new NativeButtonRenderer<>(Person::getName));
        listWithButtons.setId("list-with-buttons");

        VirtualList<Person> listWithNumbers = createVirtualList(
                new NumberRenderer<>(Person::getAge, Locale.ROOT));
        listWithNumbers.setId("list-with-numbers");

        VirtualList<Person> listWithLocalDates = createVirtualList(
                new LocalDateRenderer<>(
                        person -> LocalDate.of(2000 + person.getAge(),
                                person.getAge(), person.getAge())));
        listWithLocalDates.setId("list-with-local-dates");

        VirtualList<Person> listWithLocalDateTimes = createVirtualList(
                new LocalDateTimeRenderer<>(person -> LocalDateTime.of(
                        2000 + person.getAge(), person.getAge(),
                        person.getAge(), person.getAge(), person.getAge())));
        listWithLocalDateTimes.setId("list-with-local-date-times");

        add(listWithButtons, listWithNumbers, listWithLocalDates,
                listWithLocalDateTimes);
    }

    private VirtualList<Person> createVirtualList(Renderer<Person> renderer) {
        VirtualList<Person> list = new VirtualList<>();
        list.setHeight("100px");

        List<Person> people = createPeople(3);
        list.setItems(people);

        list.setRenderer(renderer);
        return list;
    }

    private void createListInsideFlexContainer() {
        VirtualList<String> list = new VirtualList<>();
        list.setId("list-inside-flex-container");
        list.setItems("Item 1", "Item 2", "Item 3");

        Div flexContainer = new Div(list);
        flexContainer.getStyle().set("display", "flex");

        NativeButton setFlexDirectionColumn = new NativeButton(
                "Set 'flex-direction: column'",
                e -> flexContainer.getStyle().set("flex-direction", "column"));
        setFlexDirectionColumn.setId("set-flex-direction-column");

        add(flexContainer, setFlexDirectionColumn);
    }

    private List<Person> createPeople(int amount) {
        List<Person> people = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            Person person = new Person();
            person.setName("Person " + (i + 1));
            person.setAge(i + 1);
            people.add(person);
        }
        return people;
    }

    private List<String> queryStrings(Query<String, Void> query) {
        return ITEMS.subList(Math.min(query.getOffset(), ITEMS.size() - 1),
                Math.min(query.getOffset() + query.getLimit(), ITEMS.size()));
    }

    private int countStrings(Query<String, Void> query) {
        return ITEMS.size();
    }

}

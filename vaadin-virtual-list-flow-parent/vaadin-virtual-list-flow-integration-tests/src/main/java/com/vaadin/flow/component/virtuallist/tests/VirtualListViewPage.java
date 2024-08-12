/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.javafaker.Faker;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-virtual-list/virtual-list-view")
public class VirtualListViewPage extends Div {

    private static final List<String> LIST_OF_BOOKS;
    static {
        Faker faker = Faker.instance(new SecureRandom());
        LIST_OF_BOOKS = createListOfStrings(1000, () -> faker.book().title());
    }

    /**
     * Example object used in the demo.
     */
    public static class Person implements Serializable {

        private String firstName;
        private String lastName;
        private String email;
        private String picture;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }
    }

    /**
     * Component to render a person card, with picture, name and email.
     */
    public static class PersonCard extends HorizontalLayout {

        public PersonCard(Person person) {
            setAlignItems(Alignment.CENTER);
            getStyle().set("minWidth", "300px").set("padding", "10px")
                    .set("display", "flex");

            Image picture = new Image();
            picture.setWidth("40px");
            picture.setHeight("40px");
            picture.getStyle().set("borderRadius", "50%");
            picture.getStyle().set("backgroundColor", "lightgray");

            Div pictureContainer = new Div(picture);
            pictureContainer.getStyle().set("marginRight", "10px");
            pictureContainer.setWidth("40px");
            pictureContainer.setHeight("40px");

            Div name = new Div();
            Div email = new Div();
            email.getStyle().set("fontSize", "13px");

            VerticalLayout nameContainer = new VerticalLayout(name, email);

            add(pictureContainer, nameContainer);

            boolean isPlaceHolder = person.getPicture() == null;

            if (isPlaceHolder) {
                picture.setSrc("//:0");
                name.setText(person.getFirstName());
            } else {
                picture.setSrc(person.getPicture());
                name.setText(
                        person.getFirstName() + " " + person.getLastName());
                email.setText(person.getEmail());
            }
        }
    }

    public VirtualListViewPage() {
        createStringList();
        createStringListWithDataProvider();
        createPeopleListWithDataProvider();
        createChuckNorrisFacts();
        createRankedListWithEventHandling();
        createDisabledStringsList();
        createPeopleListWithDataProviderAndComponentRenderer();
    }

    private void createStringList() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("100px");
        list.getStyle().set("border", "1px solid lightgray");

        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        list.setItems(items);

        list.setId("list-of-strings");
        add(list);
    }

    private void createStringListWithDataProvider() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("200px");
        list.getStyle().set("border", "1px solid lightgray");

        DataProvider<String, ?> dataProvider = DataProvider.fromCallbacks(
                query -> queryStringsFromDatabase(query),
                query -> countStringsFromDatabase(query));

        list.setDataProvider(dataProvider);

        list.setId("list-of-strings-with-dataprovider");
        add(new Div(new Text("List of books lazy loaded from the database")),
                list);
    }

    private void createChuckNorrisFacts() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("400px");
        int totalCount = 1000;
        List<String> facts = createFacts(totalCount);

        DataProvider<String, ?> dataProvider = DataProvider
                .fromCallbacks(query -> facts.stream().skip(query.getOffset())
                        .limit(query.getLimit()), query -> totalCount);

        list.setDataProvider(dataProvider);
        list.setRenderer(LitRenderer.<String> of(
                "<div style='font-size:20px; margin:10px; padding:10px; "
                        + "border:1px solid lightgray; border-radius:5px;'>"
                        + "#${index}. ${item.fact}</div>")
                .withProperty("fact", ValueProvider.identity()));

        list.setId("chuck-norris-facts");
        add(list);
    }

    private void createPeopleListWithDataProvider() {
        VirtualList<Person> list = new VirtualList<>();
        list.setHeight("400px");
        list.getStyle().set("border", "1px solid lightgray");

        DataProvider<Person, ?> dataProvider = DataProvider
                .ofCollection(createListOfPeople());

        list.setDataProvider(dataProvider);
        list.setRenderer(LitRenderer.<Person> of(
                "<div style='padding:10px; display:flex; min-width:250px'>"
                        + "<div style='margin-right:10px; width:40px; height:40px'>"
                        + "<img src='${item.picture}' style='border-radius:50%; width:40px; height:40px; background-color:lightgray'/>"
                        + "</div>" + "<div>"
                        + "${item.firstName} ${item.lastName}"
                        + "<br><small>${item.email}</small>" + "</div>"
                        + "</div>")
                .withProperty("firstName", Person::getFirstName)
                .withProperty("lastName", Person::getLastName)
                .withProperty("email", Person::getEmail)
                .withProperty("picture", Person::getPicture));

        // For a smooth scrolling experience use a placeholder item
        Person placeholder = new Person();
        placeholder.setFirstName("-----");
        placeholder.setPicture("//:0");
        list.setPlaceholderItem(placeholder);

        list.setId("list-of-people-with-dataprovider");
        add(list);
    }

    private void createRankedListWithEventHandling() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("400px");
        list.getStyle().set("border", "1px solid lightgray");

        List<String> items = getLordOfTheRingsCharacters();
        list.setItems(items);

        /*
         * The name of the event handlers defined at '@click' are used inside
         * the 'withFunction' calls.
         */
        list.setRenderer(LitRenderer.<String> of(
                "<div style='display:flex; justify-content:space-between; padding:10px;'>"
                        + "<div style='flex-grow:1'>#${item.rank}: ${item.name}</div>"
                        + "<div><button @click=${up} hidden='${item.upHidden}'>&uarr;</button>"
                        + "<button @click=${down} hidden='${item.downHidden}'>&darr;</button>"
                        + "<button @click=${remove} style='color:red'>X</button></div>"
                        + "<div>")
                .withProperty("name", ValueProvider.identity())
                .withProperty("rank", item -> items.indexOf(item) + 1)
                .withProperty("upHidden", item -> items.indexOf(item) == 0)
                .withProperty("downHidden",
                        item -> items.indexOf(item) == items.size() - 1)
                .withFunction("up", item -> {
                    int previousRank = items.indexOf(item);
                    if (previousRank == 0) {
                        return;
                    }
                    String previousItem = items.set(previousRank - 1, item);
                    items.set(previousRank, previousItem);
                    list.getDataCommunicator().reset();
                }).withFunction("down", item -> {
                    int previousRank = items.indexOf(item);
                    if (previousRank == items.size() - 1) {
                        return;
                    }
                    String previousItem = items.set(previousRank + 1, item);
                    items.set(previousRank, previousItem);
                    list.getDataCommunicator().reset();
                }).withFunction("remove", item -> {
                    items.remove(item);
                    list.getDataCommunicator().reset();
                }));

        list.setId("using-events-with-templates");
        add(new Div(new Text(
                "Rank up/down your favorite Lord of the Rings characters")),
                list, new NativeButton("Reset", evt -> {
                    items.clear();
                    items.addAll(getLordOfTheRingsCharacters());
                    list.getDataCommunicator().reset();
                }));
    }

    private void createDisabledStringsList() {
        VirtualList<String> list = new VirtualList<>();
        list.setHeight("400px");
        list.getStyle().set("border", "1px solid lightgray");

        Div removalResult = new Div();
        removalResult.setId("disabled-removal-result");

        DataProvider<String, ?> dataProvider = DataProvider.fromCallbacks(
                query -> queryStringsFromDatabase(query),
                query -> countStringsFromDatabase(query));

        list.setDataProvider(dataProvider);

        // Disable the list so that scrolling still works but events are not
        // handled
        list.setEnabled(false);

        /*
         * The name of the event handlers defined at '@click' are used inside
         * the 'withFunction' calls.
         */
        list.setRenderer(LitRenderer.<String> of(
                "<div style='display:flex; justify-content:space-between; padding:10px;'>"
                        + "<div style='flex-grow:1'>${item.name}</div>"
                        + "<div><button @click=${removeItem} style='color:red'>X</button></div>"
                        + "<div>")
                .withProperty("name", ValueProvider.identity())
                .withFunction("removeItem", item -> {
                    removalResult.setText(item);
                }));
        NativeButton switchEnabled = new NativeButton("Switch enabled state",
                event -> list.setEnabled(!list.isEnabled()));

        list.setId("disabled-list-with-templates");
        switchEnabled.setId("switch-enabled-state-string-list");
        add(new Div(new Text(
                "Rank up/down your favorite Lord of the Rings characters")),
                list, removalResult, switchEnabled);
    }

    private void createPeopleListWithDataProviderAndComponentRenderer() {
        /* VirtualList that uses the component above */
        VirtualList<Person> list = new VirtualList<>();
        list.setHeight("400px");
        list.getStyle().set("border", "1px solid lightgray");

        DataProvider<Person, ?> dataProvider = DataProvider
                .ofCollection(createListOfPeople());

        list.setDataProvider(dataProvider);

        // Uses the constructor of the PersonCard for each item in the list
        list.setRenderer(new ComponentRenderer<>(PersonCard::new));

        // For a smooth scrolling experience use a placeholder item
        Person placeholder = new Person();
        placeholder.setFirstName("-----");
        list.setPlaceholderItem(placeholder);

        NativeButton switchEnabled = new NativeButton("Switch enabled state",
                event -> list.setEnabled(!list.isEnabled()));

        list.setId("list-of-people-with-dataprovider-and-component-renderer");
        switchEnabled.setId("switch-enabled-people-list");
        add(new Div(new Text("List of people with grid layout")), list,
                switchEnabled);
    }

    private Stream<String> queryStringsFromDatabase(Query<String, Void> query) {
        return LIST_OF_BOOKS
                .subList(Math.min(query.getOffset(), LIST_OF_BOOKS.size() - 1),
                        Math.min(query.getOffset() + query.getLimit(),
                                LIST_OF_BOOKS.size()))
                .stream();
    }

    private int countStringsFromDatabase(Query<String, Void> query) {
        query.getOffset();
        return LIST_OF_BOOKS.size();
    }

    private List<String> getLordOfTheRingsCharacters() {
        Set<String> characters = new HashSet<>();
        Faker instance = Faker.instance(new SecureRandom());

        // We need to return a fixed size of characters.
        // 20 is ok, since in database there are only 30 distinct names
        while (characters.size() < 20) {
            characters.add(instance.lordOfTheRings().character());
        }

        List<String> list = new ArrayList<>(characters);
        Collections.sort(list);
        return list;
    }

    private static List<String> createFacts(int number) {
        number = Math.min(number, 1000);
        List<String> list = new ArrayList<>(number);
        Faker faker = Faker.instance();
        for (int i = 0; i < number; i++) {
            list.add(faker.chuckNorris().fact());
        }
        return list;
    }

    private static List<String> createListOfStrings(int number,
            Supplier<String> supplier) {
        List<String> list = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    private List<Person> createListOfPeople() {
        final int numberToGenerate = 500;
        List<Person> people = new ArrayList<>(numberToGenerate);
        Faker faker = Faker.instance(new SecureRandom());
        for (int i = 0; i < numberToGenerate; i++) {
            Person person = new Person();
            person.setFirstName(faker.name().firstName());
            person.setLastName(faker.name().lastName());
            person.setEmail(faker.internet().safeEmailAddress());
            person.setPicture(
                    "data:image/gif;base64,R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKToQGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7");
            people.add(person);
        }

        return people;
    }
}

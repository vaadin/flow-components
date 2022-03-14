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
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.combobox.test.data.DepartmentData;
import com.vaadin.flow.component.combobox.test.data.ElementData;
import com.vaadin.flow.component.combobox.test.data.ProjectData;
import com.vaadin.flow.component.combobox.test.entity.Department;
import com.vaadin.flow.component.combobox.test.entity.Element;
import com.vaadin.flow.component.combobox.test.entity.Person;
import com.vaadin.flow.component.combobox.test.entity.Project;
import com.vaadin.flow.component.combobox.test.entity.Song;
import com.vaadin.flow.component.combobox.test.entity.Ticket;
import com.vaadin.flow.component.combobox.test.service.PersonService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ComboBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-combo-box")
public class ComboBoxView extends Div {

    private static final String WIDTH_STRING = "250px";
    private transient ProjectData projectData = new ProjectData();

    public ComboBoxView() {
        basicDemo(); // Basic usage
        disabledAndReadonly();
        entityList();
        displayClearButton();
        valueChangeEvent();
        customValues();
        storingCustomValues();
        autoOpenDisabled();
        itemCountChangeNotification();
        lazyLoading(); // Lazy loading
        lazyLoadingWithExactItemCount();
        lazyLoadingWithCustomItemCountEstimate();
        pagedRepository();
        helperText();
        configurationForRequired(); // Validation
        customFiltering(); // Filtering
        filteringAndSortingWithDataView();
        filteringWithTypesOtherThanString();
        customOptionsDemo(); // Presentation
        usingTemplateRenderer();
        themeVariantsTextAlign(); // Theme variants
        themeVariantsSmallSize();
        helperTextAbove();
        styling(); // Styling
    }

    private void basicDemo() {
        Div div = new Div();
        ComboBox<String> labelComboBox = new ComboBox<>();
        labelComboBox.setItems("Option one", "Option two");
        labelComboBox.setLabel("Label");

        ComboBox<String> placeHolderComboBox = new ComboBox<>();
        placeHolderComboBox.setItems("Option one", "Option two");
        placeHolderComboBox.setPlaceholder("Placeholder");

        ComboBox<String> valueComboBox = new ComboBox<>();
        valueComboBox.setItems("Value", "Option one", "Option two");
        valueComboBox.setValue("Value");

        labelComboBox.getStyle().set("margin-right", "5px");
        placeHolderComboBox.getStyle().set("margin-right", "5px");
        div.add(labelComboBox, placeHolderComboBox, valueComboBox);
        addCard("Basic usage", div);
    }

    private void disabledAndReadonly() {
        Div div = new Div();
        ComboBox<String> disabledComboBox = new ComboBox<>();
        disabledComboBox.setItems("Value", "Option one", "Option two");
        disabledComboBox.setEnabled(false);
        disabledComboBox.setValue("Value");
        disabledComboBox.setLabel("Disabled");

        ComboBox<String> readOnlyComboBox = new ComboBox<>();
        readOnlyComboBox.setItems("Value", "Option one", "Option two");
        readOnlyComboBox.setReadOnly(true);
        readOnlyComboBox.setValue("Value");
        readOnlyComboBox.setLabel("Read-only");
        disabledComboBox.getStyle().set("margin-right", "5px");
        div.add(disabledComboBox, readOnlyComboBox);
        addCard("Disabled and read-only", div);
    }

    private List<Department> getDepartments() {

        DepartmentData departmentData = new DepartmentData();
        return departmentData.getDepartments();
    }

    private List<Element> getElements() {
        ElementData elementData = new ElementData();
        return elementData.getElements();
    }

    private void entityList() {
        ComboBox<Department> comboBox = new ComboBox<>();
        comboBox.setLabel("Department");
        List<Department> departmentList = getDepartments();

        // Choose which property from Department is the presentation value
        comboBox.setItemLabelGenerator(Department::getName);
        comboBox.setItems(departmentList);
        addCard("Entity list", comboBox);
    }

    private void displayClearButton() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("Option one", "Option two");
        comboBox.setClearButtonVisible(true);

        addCard("Display the clear button", comboBox);
    }

    private void autoOpenDisabled() {
        Span note = new Span(
                "Dropdown is only opened when clicking the toggle button or pressing Up or Down arrow keys.");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("Option one", "Option two");
        comboBox.setAutoOpen(false);

        addCard("Auto open disabled", note, comboBox);
    }

    private void itemCountChangeNotification() {
        ComboBox<Ticket> comboBox = new ComboBox<>("Available tickets");
        comboBox.setPlaceholder("Select a ticket");

        Collection<Ticket> tickets = generateTickets();

        ComboBoxListDataView<Ticket> dataView = comboBox.setItems(tickets);

        Button buyTicketButton = new Button("Buy a ticket", click -> comboBox
                .getOptionalValue().ifPresent(dataView::removeItem));

        /*
         * If you want to get notified when the ComboBox's items count has
         * changed on the server-side, i.e. due to adding or removing an
         * item(s), or by changing the server-side filtering, you can add a
         * listener using a data view API.
         *
         * Please note that the ComboBox's client-side filter change won't fire
         * the event, since it doesn't change the item count on the server-side,
         * but only reduces the item list in UI and makes it easier to search
         * through the items.
         */
        dataView.addItemCountChangeListener(
                event -> comboBox.getOptionalValue().ifPresent(ticket -> {
                    if (event.getItemCount() > 0) {
                        Notification.show(String.format(
                                "Ticket with %s is sold. %d ticket(s) left",
                                ticket, event.getItemCount()), 3000,
                                Notification.Position.MIDDLE);
                    } else {
                        Notification.show("All tickets were sold out", 3000,
                                Notification.Position.MIDDLE);
                        buyTicketButton.setEnabled(false);
                    }
                    comboBox.clear();
                }));

        HorizontalLayout layout = new HorizontalLayout(comboBox,
                buyTicketButton);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        addCard("Item Count Change Notification", layout);
    }

    private void valueChangeEvent() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setLabel("Label");
        comboBox.setItems("Option one", "Option two");
        comboBox.setClearButtonVisible(true);

        Div value = new Div();
        value.setText("Select a value");
        comboBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                value.setText("No option selected");
            } else {
                value.setText("Selected: " + event.getValue());
            }
        });
        VerticalLayout verticalLayout = new VerticalLayout(comboBox, value);
        verticalLayout.setAlignItems(FlexComponent.Alignment.START);
        addCard("Value change event", verticalLayout);
    }

    private void customValues() {
        Div message = createMessageDiv("custom-value-message");

        ComboBox<String> comboBox = new ComboBox<>("Fruit");
        comboBox.setItems("Apple", "Orange", "Banana");

        /**
         * Allow users to enter a value which doesn't exist in the data set, and
         * set it as the value of the ComboBox.
         */
        comboBox.addCustomValueSetListener(
                event -> comboBox.setValue(event.getDetail()));

        comboBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                message.setText("No fruit selected");
            } else {
                message.setText("Selected value: " + event.getValue());
            }
        });

        comboBox.setId("custom-value-box");
        addCard("Allow custom values", comboBox, message);
    }

    private Stream<Project> fetchProjects(Query<Project, String> query) {
        return projectData.getProjects().stream()
                .filter(project -> !query.getFilter().isPresent() || project
                        .getName().startsWith(query.getFilter().get()))
                .skip(query.getOffset()).limit(query.getLimit());
    }

    private int countProjects(Query<Project, String> query) {
        return (int) projectData.getProjects().stream()
                .filter(project -> !query.getFilter().isPresent() || project
                        .getName().startsWith(query.getFilter().get()))
                .count();
    }

    private void storingCustomValues() {
        Div message = createMessageDiv("custom-value-message");
        ComboBox<Project> comboBox = new ComboBox<>("Project");
        comboBox.setItems(this::fetchProjects, this::countProjects);
        comboBox.setItemLabelGenerator(Project::getName);

        comboBox.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.getValue() == null) {
                message.setText("No project selected");
            } else {
                message.setText(
                        "Selected value: " + valueChangeEvent.getValue());
            }
        });

        comboBox.addCustomValueSetListener(event -> {
            Project project = projectData.addProject(event.getDetail());
            comboBox.setValue(project);
        });

        addCard("Storing custom values", comboBox, message);

    }

    private void lazyLoading() {
        ComboBox<Person> comboBox = new ComboBox<>();
        PersonService service = new PersonService(500);
        /*
         * When provided a callback, the combo box doesn't load all items from
         * backend to server memory right away. It will request only the data
         * that is shown in its current view "window". The data is provided
         * based on string filter, offset and limit.
         *
         * When the user scrolls to the end, combo box will automatically extend
         * and fetch more items until the backend runs out of items.
         */
        comboBox.setItems(query -> service.fetch(query.getFilter().orElse(null),
                query.getOffset(), query.getLimit()));

        comboBox.setId("fetch-callback");
        addCard("Lazy Loading", "Lazy Loading with Callback", comboBox);
    }

    private void lazyLoadingWithExactItemCount() {
        ComboBox<Person> comboBox = new ComboBox<>();
        PersonService service = new PersonService();
        /*
         * By using these callbacks the ComboBox doesn't load all the items to
         * the server memory right away. The ComboBox calls the first provided
         * callback to fetch items from the given range with the given filter.
         * The second callback is optional and can be used to determine an exact
         * count of items that match the query, if the exact count is desired.
         */
        comboBox.setItems(
                query -> service.fetch(query.getFilter().orElse(null),
                        query.getOffset(), query.getLimit()),
                query -> service.count(query.getFilter().orElse(null)));

        comboBox.setId("with-exact-items-count");
        addCard("Lazy Loading", "Lazy Loading with Exact Items Count",
                comboBox);
    }

    private void lazyLoadingWithCustomItemCountEstimate() {
        // The backend will have 12345 items
        PersonService service = new PersonService(12345);
        ComboBox<Person> comboBox = new ComboBox<>();

        ComboBoxLazyDataView<Person> lazyDataView = comboBox
                .setItems(query -> service.fetch(query.getFilter().orElse(null),
                        query.getOffset(), query.getLimit()));

        /*
         * By default, the combo box will initially adjust the scrollbar to 200
         * items and as the user scrolls down it automatically increases the
         * size by 200 until the backend runs out of items.
         *
         * Depending on the desired UX and the backend performance, the
         * scrolling experience and the number of items in the drop down can be
         * customized accordingly by constraining the page size, estimated item
         * count and its increase.
         */
        comboBox.setPageSize(10);
        lazyDataView.setItemCountEstimate(50);
        lazyDataView.setItemCountEstimateIncrease(50);

        // Showing the item count for demo purposes
        Div countText = new Div();
        lazyDataView.addItemCountChangeListener(event -> {
            if (event.isItemCountEstimated()) {
                countText.setText(
                        "Person Count Estimate: " + event.getItemCount());
            } else {
                countText
                        .setText("Exact Person Count: " + event.getItemCount());
            }
        });

        HorizontalLayout layout = new HorizontalLayout(comboBox, countText);

        comboBox.setId("custom-item-count-estimate");
        addCard("Lazy Loading", "Custom Item Count Estimate And Increase",
                layout);
    }

    private void pagedRepository() {
        ComboBox<Person> comboBox = new ComboBox<>();
        PersonService service = new PersonService();
        /*
         * For those backend repositories which use paged data fetching, it is
         * possible to get the page number and page size from Query API.
         */
        comboBox.setItems(
                query -> service.fetchPage(query.getFilter().orElse(null),
                        query.getPage(), query.getPageSize()));

        comboBox.setId("paged-box");
        addCard("Lazy Loading", "Lazy Loading from Paged Repository", comboBox);
    }

    private void helperText() {
        Div div = new Div();
        ComboBox<String> helperTextCombobox = new ComboBox<>("Language");
        helperTextCombobox.setItems("Java", "Python", "C++", "Scala",
                "JavaScript");
        helperTextCombobox.setHelperText(
                "Select the language you are most familiar with");

        ComboBox<String> helperComponentCombobox = new ComboBox<>("Continent");
        helperComponentCombobox.setItems("North America", "South America",
                "Africa", "Europe", "Asia", "Australia", "Antarctica");
        helperComponentCombobox.setHelperComponent(
                new Span("Select the continent of your residence"));

        add(helperTextCombobox, helperComponentCombobox);

        helperTextCombobox.getStyle().set("margin-right", "15px");
        div.add(helperTextCombobox, helperComponentCombobox);

        addCard("Helper text and helper component", div);
    }

    private void configurationForRequired() {
        ComboBox<String> requiredComboBox = new ComboBox<>();
        requiredComboBox.setItems("Option one", "Option two", "Option three");
        requiredComboBox.setLabel("Required");
        requiredComboBox.setPlaceholder("Select an option");

        requiredComboBox.setRequired(true);
        requiredComboBox.setClearButtonVisible(true);
        FlexLayout layout = new FlexLayout(requiredComboBox);
        layout.getStyle().set("flex-wrap", "wrap");
        addCard("Validation", "Required", layout);
    }

    private void customFiltering() {
        Div div = new Div();
        div.setText("Example uses case-sensitive starts-with filtering");
        ComboBox<Element> filteringComboBox = new ComboBox<>();
        List<Element> elementsList = getElements();

        /*
         * Providing a custom item filter allows filtering based on all of the
         * rendered properties:
         */
        ItemFilter<Element> filter = (element, filterString) -> element
                .getName().startsWith(filterString);

        filteringComboBox.setItems(filter, elementsList);
        filteringComboBox.setItemLabelGenerator(Element::getName);
        filteringComboBox.setClearButtonVisible(true);
        addCard("Filtering", "Custom filtering", div, filteringComboBox);

    }

    private void filteringAndSortingWithDataView() {
        // PersonService can be found:
        // https://github.com/vaadin/vaadin-combo-box-flow/tree/master/vaadin-combo-box-flow-demo/src/main/java/com/vaadin/flow/component/combobox/demo/service/PersonService.java
        ComboBox<Person> comboBox = new ComboBox<>("Persons");
        PersonService personService = new PersonService();

        // We fetch the items to the memory and bind the obtained collection
        // to the combo box
        Collection<Person> persons = personService.fetchAll();

        ComboBoxListDataView<Person> dataView = comboBox.setItems(persons);

        /*
         * Providing a predicate item filter allows filtering by any field of
         * the business entity and apply a combo box's text filter independently
         */
        IntegerField personAgeFilter = new IntegerField(
                event -> dataView.setFilter(person -> event.getValue() == null
                        || person.getAge() > event.getValue()));

        /*
         * Providing a value provider or comparator allows sorting combo box's
         * items by custom field, or combination of fields
         */
        Button sortPersons = new Button("Sort Persons by Name",
                event -> dataView.setSortOrder(Person::toString,
                        SortDirection.ASCENDING));

        personAgeFilter.setLabel("Filter Persons with age more than:");
        personAgeFilter.setWidth(WIDTH_STRING);
        addCard("Filtering", "Filtering and Sorting with Data View", comboBox,
                personAgeFilter, sortPersons);
    }

    private void filteringWithTypesOtherThanString() {
        // PersonService can be found:
        // https://github.com/vaadin/vaadin-combo-box-flow/tree/master/vaadin-combo-box-flow-demo/src/main/java/com/vaadin/flow/component/combobox/demo/service/PersonService.java
        PersonService personService = new PersonService(500);

        ComboBox<Person> comboBox = new ComboBox<>("Person");
        comboBox.setPlaceholder("Enter minimum age to filter");
        comboBox.setPattern("^\\d+$");
        comboBox.setPreventInvalidInput(true);

        // Configuring fetch callback with a filter converter, so entered filter
        // strings can refer also to other typed properties like age (integer):
        comboBox.setItemsWithFilterConverter(
                query -> personService.fetchOlderThan(
                        query.getFilter().orElse(null), query.getOffset(),
                        query.getLimit()),
                ageStr -> ageStr.trim().isEmpty() ? null
                        : Integer.parseInt(ageStr));
        comboBox.setItemLabelGenerator(person -> person.getFirstName() + " "
                + person.getLastName() + " - " + person.getAge());
        comboBox.setClearButtonVisible(true);
        comboBox.setWidth(WIDTH_STRING);
        addCard("Filtering", "Filtering with types other than String",
                comboBox);
    }

    private void customOptionsDemo() {
        // ComponentRenderer
        ComboBox<Information> comboBox = new ComboBox<>();
        comboBox.setLabel("User");
        comboBox.setItems(
                new Information("Gabriella",
                        "https://randomuser.me/api/portraits/women/43.jpg"),
                new Information("Rudi",
                        "https://randomuser.me/api/portraits/men/77.jpg"),
                new Information("Hamsa",
                        "https://randomuser.me/api/portraits/men/35.jpg"),
                new Information("Jacob",
                        "https://randomuser.me/api/portraits/men/76.jpg"));

        comboBox.setRenderer(new ComponentRenderer<>(information -> {
            Div text = new Div();
            text.setText(information.getText());

            Image image = new Image();
            image.setWidth("21px");
            image.setHeight("21px");
            image.setSrc(information.getImage());

            FlexLayout wrapper = new FlexLayout();
            text.getStyle().set("margin-left", "0.5em");
            wrapper.add(image, text);
            return wrapper;
        }));

        comboBox.setItemLabelGenerator(Information::getText);

        addCard("Presentation",
                "Customizing drop down items with ComponentRenderer", comboBox);
    }

    private void usingTemplateRenderer() {

        ComboBox<Song> comboBox = new ComboBox<>();
        comboBox.setLabel("Song");
        List<Song> listOfSongs = createListOfSongs();

        /*
         * Providing a custom item filter allows filtering based on all of the
         * rendered properties:
         */
        ItemFilter<Song> filter = (song,
                filterString) -> song.getName().toLowerCase()
                        .contains(filterString.toLowerCase())
                        || song.getArtist().toLowerCase()
                                .contains(filterString.toLowerCase());

        comboBox.setItems(filter, listOfSongs);
        comboBox.setClearButtonVisible(true);
        comboBox.setItemLabelGenerator(Song::getName);
        comboBox.setRenderer(TemplateRenderer.<Song> of(
                "<div>[[item.song]]<br><small>[[item.artist]]</small></div>")
                .withProperty("song", Song::getName)
                .withProperty("artist", Song::getArtist));

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("template-selection-box");
        addCard("Presentation",
                "Customizing drop down items with TemplateRenderer", comboBox);
    }

    private void themeVariantsTextAlign() {
        Div div = new Div();
        ComboBox<String> leftComboBox = new ComboBox<>();
        leftComboBox.setItems("Left", "Center", "Right");
        leftComboBox.setValue("Left");
        leftComboBox.getElement().setAttribute("theme", "align-left");

        ComboBox<String> centerComboBox = new ComboBox<>();
        centerComboBox.setItems("Left", "Center", "Right");
        centerComboBox.setValue("Center");
        centerComboBox.getElement().setAttribute("theme", "align-center");

        ComboBox<String> rightComboBox = new ComboBox<>();
        rightComboBox.setItems("Left", "Center", "Right");
        rightComboBox.setValue("Right");
        rightComboBox.getElement().setAttribute("theme", "align-right");
        div.add(leftComboBox, centerComboBox, rightComboBox);
        leftComboBox.getStyle().set("margin-right", "5px");
        centerComboBox.getStyle().set("margin-right", "5px");
        addCard("Theme Variants", "Text align", div);

    }

    private void themeVariantsSmallSize() {
        ComboBox<String> comboBox = new ComboBox<>("Label");
        comboBox.setItems("Option one", "Option two");
        comboBox.setPlaceholder("Placeholder");
        comboBox.getElement().setAttribute("theme", "small");
        addCard("Theme Variants", "Small size", comboBox);
    }

    private void helperTextAbove() {

        ComboBox<String> helperTextAbove = new ComboBox<>();
        helperTextAbove.setLabel("Label");
        helperTextAbove.setItems("Option 1", "Option 2");
        helperTextAbove.setHelperText(
                "Helper text positioned above the field using `helper-above-field` theme");
        helperTextAbove.getElement().getThemeList().set("helper-above-field",
                true);

        add(helperTextAbove);

        addCard("Theme Variants", "Helper text above the component",
                helperTextAbove);
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
                + "vaadin-combo-box/html-examples/combo-box-styling-demos",
                "HTML Styling Demos"));

        addCard("Styling", "Styling references", p1, p2);
    }

    private List<Song> createListOfSongs() {
        List<Song> listOfSongs = new ArrayList<>();
        listOfSongs.add(new Song("A V Club Disagrees", "Haircuts for Men",
                "Physical Fitness"));
        listOfSongs.add(new Song("Sculpted", "Haywyre", "Two Fold Pt.1"));
        listOfSongs.add(
                new Song("Voices of a Distant Star", "Killigrew", "Animus II"));
        return listOfSongs;
    }

    private Collection<Ticket> generateTickets() {
        Collection<Ticket> tickets = new ArrayList<>();
        for (int row = 1; row < 51; row++) {
            for (int seat = 1; seat < 51; seat++) {
                tickets.add(new Ticket(row, seat));
            }
        }
        return tickets;
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private static class Information {
        private String text;
        private String image;

        private Information(String text, String image) {
            this.text = text;
            this.image = image;
        }

        public String getText() {
            return text;
        }

        public String getImage() {
            return image;
        }
    }

    private void addCard(String title, Component... components) {
        addCard(title, null, components);
    }

    private void addCard(String title, String description,
            Component... components) {
        if (description != null) {
            title = title + ": " + description;
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}

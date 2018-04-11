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
package com.vaadin.flow.component.grid.demo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.ColumnGroup;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Grid} demo.
 */
@Route("vaadin-grid")
public class GridView extends DemoView {

    public static List<Person> items = new ArrayList<>();

    static {
        items = createItems();
    }

    // begin-source-example
    // source-example-heading: Grid example model
    /**
     * Example object.
     */
    public static class Person {
        private int id;
        private int age;
        private String name;
        private Address address;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

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

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Person)) {
                return false;
            }
            Person other = (Person) obj;
            return id == other.id;
        }

        @Override
        public String toString() {
            return String.format("Person [name=%s, age=%s]", name, age);
        }
    }

    /**
     * Example object.
     */
    public static class Address {
        private String street;
        private int number;
        private String postalCode;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        @Override
        public String toString() {
            return String.format("%s %s", street, number);
        }
    }
    // end-source-example

    private static class Item {
        private String name;
        private double price;
        private LocalDateTime purchaseDate;
        private LocalDate estimatedDeliveryDate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public LocalDateTime getPurchaseDate() {
            return purchaseDate;
        }

        public void setPurchaseDate(LocalDateTime purchaseDate) {
            this.purchaseDate = purchaseDate;
        }

        public LocalDate getEstimatedDeliveryDate() {
            return estimatedDeliveryDate;
        }

        public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
            this.estimatedDeliveryDate = estimatedDeliveryDate;
        }
    }

    // begin-source-example
    // source-example-heading: Grid with columns using component renderer
    /**
     * Component used for the cell rendering.
     */
    public static class PersonComponent extends Div {

        private String text;
        private int timesClicked;

        /**
         * Creates a new component with the given item.
         *
         * @param person the person to set
         */
        public PersonComponent(Person person) {
            this.addClickListener(event -> {
                timesClicked++;
                setText(text + "\nClicked " + timesClicked);
            });
            setPerson(person);
        }

        /**
         * Sets the person for the component.
         *
         * @param person
         *            the person to be inside inside the cell
         */
        public void setPerson(Person person) {
            text = "Hi, I'm " + person.getName() + "!";
            setText(text);
        }

        @Override
        public int hashCode() {
            return text == null ? 0 : text.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PersonComponent)) {
                return false;
            }
            PersonComponent other = (PersonComponent) obj;
            if (text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!text.equals(other.text)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Component used for the details row.
     */
    public static class PersonCard extends Div {

        /**
         * Constructor that takes a Person as parameter.
         *
         * @param person
         *            the person to be used inside the card
         */
        public PersonCard(Person person) {
            this.addClassName("custom-details");

            VerticalLayout layout1 = new VerticalLayout();
            layout1.add(new Label("Name: " + person.getName()));
            layout1.add(new Label("Id: " + person.getId()));
            layout1.add(new Label("Age: " + person.getAge()));

            VerticalLayout layout2 = new VerticalLayout();
            layout2.add(
                    new Label("Street: " + person.getAddress().getStreet()));
            layout2.add(new Label(
                    "Address number: " + person.getAddress().getNumber()));
            layout2.add(new Label(
                    "Postal Code: " + person.getAddress().getPostalCode()));

            HorizontalLayout hlayout = new HorizontalLayout(layout1, layout2);
            hlayout.getStyle().set("border", "1px solid gray")
                    .set("padding", "10px").set("boxSizing", "border-box")
                    .set("width", "100%");

            this.add(hlayout);
        }
    }

    // end-source-example

    @Override
    protected void initView() {
        createBasicUsage();
        createCallBackDataProvider();
        createSingleSelect();
        createMultiSelect();
        createNoneSelect();
        createColumnApiExample();
        createBasicRenderers();
        createColumnTemplate();
        createColumnGroup();
        createColumnComponentTemplateRenderer();
        createItemDetails();
        createItemDetailsOpenedProgrammatically();
        createSorting();
        createHeaderAndFooterUsingTemplates();
        createHeaderAndFooterUsingComponents();
        createBeanGrid();
        createHeightByRows();
        createBasicFeatures();

        addCard("Grid example model",
                new Label("These objects are used in the examples above"));
    }

    private void createBasicUsage() {
        // begin-source-example
        // source-example-heading: Grid Basics
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        // end-source-example
        grid.setId("basic");

        addCard("Grid Basics", grid);
    }

    private void createCallBackDataProvider() {
        // begin-source-example
        // source-example-heading: Grid with lazy loading
        Grid<Person> grid = new Grid<>();

        /*
         * This Data Provider doesn't load all items into the memory right away.
         * Grid will request only the data that should be shown in its current
         * view "window". The Data Provider will use callbacks to load only a
         * portion of the data.
         */
        Random random = new Random(0);
        grid.setDataProvider(DataProvider.fromCallbacks(
                query -> IntStream
                        .range(query.getOffset(),
                                query.getOffset() + query.getLimit())
                        .mapToObj(index -> createPerson(index + 1, random)),
                query -> 100 * 1000 * 1000));

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        // end-source-example

        grid.setId("lazy-loading");

        addCard("Grid with lazy loading", grid);
    }

    private void createSingleSelect() {
        Div messageDiv = new Div();
        // begin-source-example
        // source-example-heading: Grid Single Selection
        List<Person> people = getItems();
        Grid<Person> grid = new Grid<>();
        grid.setItems(people);

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.asSingleSelect().addValueChangeListener(
                event -> messageDiv.setText(String.format(
                        "Selection changed from %s to %s, selection is from client: %s",
                        event.getOldValue(), event.getValue(),
                        event.isFromClient())));

        NativeButton toggleSelect = new NativeButton(
                "Toggle selection of the first person");
        Person firstPerson = people.get(0);
        toggleSelect.addClickListener(event -> {
            GridSelectionModel<Person> selectionModel = grid
                    .getSelectionModel();
            if (selectionModel.isSelected(firstPerson)) {
                selectionModel.deselect(firstPerson);
            } else {
                selectionModel.select(firstPerson);
            }
        });
        // end-source-example
        grid.setId("single-selection");
        toggleSelect.setId("single-selection-toggle");
        messageDiv.setId("single-selection-message");
        addCard("Selection", "Grid Single Selection", grid, toggleSelect,
                messageDiv);
    }

    private void createMultiSelect() {
        Div messageDiv = new Div();
        // begin-source-example
        // source-example-heading: Grid Multi Selection
        List<Person> people = getItems();
        Grid<Person> grid = new Grid<>();
        grid.setItems(people);

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.MULTI);

        grid.asMultiSelect().addValueChangeListener(
                event -> messageDiv.setText(String.format(
                        "Selection changed from %s to %s, selection is from client: %s",
                        event.getOldValue(), event.getValue(),
                        event.isFromClient())));

        NativeButton selectBtn = new NativeButton("Select first five persons");
        selectBtn.addClickListener(event -> grid.asMultiSelect()
                .setValue(new LinkedHashSet<>(people.subList(0, 5))));
        NativeButton deselectBtn = new NativeButton(
                "Deselect first five persons");
        deselectBtn.addClickListener(
                event -> grid.asMultiSelect().setValue(new LinkedHashSet<>()));
        NativeButton selectAllBtn = new NativeButton("Select all");
        selectAllBtn.addClickListener(
                event -> ((GridMultiSelectionModel<Person>) grid
                        .getSelectionModel()).selectAll());
        // end-source-example
        grid.setId("multi-selection");
        selectBtn.setId("multi-selection-button");
        messageDiv.setId("multi-selection-message");
        addCard("Selection", "Grid Multi Selection", grid,
                new HorizontalLayout(selectBtn, deselectBtn, selectAllBtn),
                messageDiv);
    }

    private void createNoneSelect() {
        // begin-source-example
        // source-example-heading: Grid with No Selection Enabled
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);
        // end-source-example
        grid.setId("none-selection");
        addCard("Selection", "Grid with No Selection Enabled", grid);
    }

    private void createColumnTemplate() {
        // begin-source-example
        // source-example-heading: Grid with columns using template renderer
        Grid<Person> grid = new Grid<>();
        grid.setItems(createItems());

        // You can use the [[index]] variable to print the row index (0 based)
        grid.addColumn(TemplateRenderer.of("[[index]]")).setHeader("#");

        // You can set any property by using `withProperty`, including
        // properties not present on the original bean.
        grid.addColumn(TemplateRenderer.<Person> of(
                "<div title='[[item.name]]'>[[item.name]]<br><small>[[item.yearsOld]]</small></div>")
                .withProperty("name", Person::getName).withProperty("yearsOld",
                        person -> person.getAge() > 1
                                ? person.getAge() + " years old"
                                : person.getAge() + " year old"))
                .setHeader("Person");

        // You can also set complex objects directly. Internal properties of the
        // bean are accessible in the template.
        grid.addColumn(TemplateRenderer.<Person> of(
                "<div>[[item.address.street]], number [[item.address.number]]<br><small>[[item.address.postalCode]]</small></div>")
                .withProperty("address", Person::getAddress))
                .setHeader("Address");

        // You can set events handlers associated with the template. The syntax
        // follows the Polymer convention "on-event", such as "on-click".
        grid.addColumn(TemplateRenderer.<Person> of(
                "<button on-click='handleUpdate'>Update</button><button on-click='handleRemove'>Remove</button>")
                .withEventHandler("handleUpdate", person -> {
                    person.setName(person.getName() + " Updated");
                    grid.getDataProvider().refreshItem(person);
                }).withEventHandler("handleRemove", person -> {
                    ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                            .getDataProvider();
                    dataProvider.getItems().remove(person);
                    dataProvider.refreshAll();
                })).setHeader("Actions");

        grid.setSelectionMode(SelectionMode.NONE);
        // end-source-example
        grid.setId("template-renderer");
        addCard("Using templates", "Grid with columns using template renderer",
                grid);
    }

    private void createColumnComponentTemplateRenderer() {
        // begin-source-example
        // source-example-heading: Grid with columns using component renderer
        Grid<Person> grid = new Grid<>();
        grid.setItems(createItems());

        // Use the component constructor that accepts an item ->
        // new PersonComponent(Person person)
        grid.addComponentColumn(PersonComponent::new).setHeader("Person");

        // Or you can use an ordinary function to setup the component
        grid.addComponentColumn(item -> new NativeButton("Remove", evt -> {
            ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                    .getDataProvider();
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        })).setHeader("Actions");

        // Item details can also use components
        grid.setItemDetailsRenderer(new ComponentRenderer<>(PersonCard::new));

        // When items are updated, new components are generated
        TextField idField = new TextField("", "Person id");
        TextField nameField = new TextField("", "New name");

        NativeButton updateButton = new NativeButton("Update person", event -> {
            String id = idField.getValue();
            String name = nameField.getValue();
            ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                    .getDataProvider();

            dataProvider.getItems().stream()
                    .filter(person -> String.valueOf(person.getId()).equals(id))
                    .findFirst().ifPresent(person -> {
                        person.setName(name);
                        dataProvider.refreshItem(person);
                    });

        });

        grid.setSelectionMode(SelectionMode.NONE);
        // end-source-example

        grid.setId("component-renderer");
        idField.setId("component-renderer-id-field");
        nameField.setId("component-renderer-name-field");
        updateButton.setId("component-renderer-update-button");
        addCard("Using components",
                "Grid with columns using component renderer", grid, idField,
                nameField, updateButton);
    }

    private void createColumnApiExample() {
        // begin-source-example
        // source-example-heading: Column API example
        Grid<Person> grid = new Grid<>();
        GridSelectionModel<Person> selectionMode = grid
                .setSelectionMode(SelectionMode.MULTI);
        grid.setItems(getItems());

        Column<Person> idColumn = grid.addColumn(Person::getId).setHeader("ID")
                .setFlexGrow(0).setWidth("75px");
        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setHeader("Name").setResizable(true);

        // Setting a column-key allows fetching the column later
        grid.addColumn(Person::getAge).setHeader("Age").setKey("age");
        grid.getColumnByKey("age").setResizable(true);

        NativeButton idColumnVisibility = new NativeButton(
                "Toggle visibility of the ID column");
        idColumnVisibility.addClickListener(
                event -> idColumn.setVisible(!idColumn.isVisible()));

        NativeButton userReordering = new NativeButton(
                "Toggle user reordering of columns");
        userReordering.addClickListener(event -> grid
                .setColumnReorderingAllowed(!grid.isColumnReorderingAllowed()));

        NativeButton freezeIdColumn = new NativeButton(
                "Toggle frozen state of ID column");
        freezeIdColumn.addClickListener(
                event -> idColumn.setFrozen(!idColumn.isFrozen()));

        NativeButton freezeSelectionColumn = new NativeButton(
                "Toggle frozen state of selection column");
        GridMultiSelectionModel<?> multiSlection = (GridMultiSelectionModel<?>) selectionMode;
        freezeSelectionColumn.addClickListener(
                event -> multiSlection.setSelectionColumnFrozen(
                        !multiSlection.isSelectionColumnFrozen()));

        NativeButton merge = new NativeButton("Merge ID and name columns");
        merge.addClickListener(event -> {
            grid.mergeColumns(idColumn, nameColumn)
                    .setHeader("ID, Name column group");
            // Remove this button from the layout
            merge.getParent().ifPresent(
                    component -> ((HasComponents) component).remove(merge));
        });
        // end-source-example

        grid.setId("column-api-example");
        idColumnVisibility.setId("toggle-id-column-visibility");
        userReordering.setId("toggle-user-reordering");
        freezeIdColumn.setId("toggle-id-column-frozen");
        freezeSelectionColumn.setId("toggle-selection-column-frozen");
        addCard("Configuring columns", "Column API example", grid,
                new VerticalLayout(idColumnVisibility, userReordering,
                        freezeIdColumn, freezeSelectionColumn, merge));
    }

    private Grid<Person> createGridWithDetails() {
        // begin-source-example
        // source-example-heading: Grid with item details
        Grid<Person> grid = new Grid<>();
        List<Person> people = createItems();
        grid.setItems(people);

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);

        // You can use any renderer for the item details. By default, the
        // details are opened and closed by clicking the rows.
        grid.setItemDetailsRenderer(TemplateRenderer.<Person> of(
                "<div class='custom-details' style='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>Hi! My name is <b>[[item.name]]!</b></div>"
                        + "<div><button on-click='handleClick'>Update Person</button></div>"
                        + "</div>")
                .withProperty("name", Person::getName)
                .withEventHandler("handleClick", person -> {
                    person.setName(person.getName() + " Updated");
                    grid.getDataProvider().refreshItem(person);
                }));
        // end-source-example
        return grid;
    }

    private void createItemDetails() {
        Grid<Person> grid = createGridWithDetails();
        grid.setId("grid-with-details-row");
        addCard("Item details", "Grid with item details", grid);
    }

    private void createItemDetailsOpenedProgrammatically() {
        Grid<Person> grid = createGridWithDetails();

        // begin-source-example
        // source-example-heading: Open details programmatically
        // Disable the default way of opening item details:
        grid.setDetailsVisibleOnClick(false);

        grid.addColumn(new NativeButtonRenderer<>("Toggle details open",
                item -> grid.setDetailsVisible(item,
                        !grid.isDetailsVisible(item))));

        // end-source-example
        grid.setId("grid-with-details-row-2");
        addCard("Item details", "Open details programmatically", grid);
    }

    private void createColumnGroup() {
        // begin-source-example
        // source-example-heading: Column grouping example
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setHeader("Name");
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader("Street");
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader("Postal Code");

        ColumnGroup informationColumnGroup = grid
                .mergeColumns(nameColumn, ageColumn)
                .setHeader("Basic Information")
                .setFooter("Total: " + getItems().size() + " people");
        ColumnGroup addressColumnGroup = grid
                .mergeColumns(streetColumn, postalCodeColumn)
                .setHeader("Address Information");
        grid.mergeColumns(informationColumnGroup, addressColumnGroup);
        // end-source-example
        grid.setId("grid-column-grouping");
        addCard("Configuring columns", "Column grouping example", grid);
    }

    private void createSorting() {
        Div messageDiv = new Div();
        // begin-source-example
        // source-example-heading: Grid with sortable columns
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.setSelectionMode(SelectionMode.NONE);

        grid.addColumn(Person::getName, "name").setHeader("Name");
        grid.addColumn(Person::getAge, "age").setHeader("Age");

        grid.addColumn(TemplateRenderer.<Person> of(
                "<div>[[item.street]], number [[item.number]]<br><small>[[item.postalCode]]</small></div>")
                .withProperty("street",
                        person -> person.getAddress().getStreet())
                .withProperty("number",
                        person -> person.getAddress().getNumber())
                .withProperty("postalCode",
                        person -> person.getAddress().getPostalCode()),
                "street", "number").setHeader("Address");

        Checkbox multiSort = new Checkbox("Multiple column sorting enabled");
        multiSort.addValueChangeListener(
                event -> grid.setMultiSort(event.getValue()));
        grid.addSortListener(event -> {
            String currentSortOrder = grid.getDataCommunicator()
                    .getBackEndSorting().stream()
                    .map(querySortOrder -> String.format(
                            "{sort property: %s, direction: %s}",
                            querySortOrder.getSorted(),
                            querySortOrder.getDirection()))
                    .collect(Collectors.joining(", "));
            messageDiv.setText(String.format(
                    "Current sort order: %s. Sort originates from the client: %s.",
                    currentSortOrder, event.isFromClient()));
        });
        // end-source-example
        grid.setId("grid-sortable-columns");
        multiSort.setId("grid-multi-sort-toggle");
        messageDiv.setId("grid-sortable-columns-message");
        addCard("Sorting", "Grid with sortable columns", grid, multiSort,
                messageDiv);
    }

    private void createHeaderAndFooterUsingTemplates() {
        // begin-source-example
        // source-example-heading: Column header and footer using templates
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setHeader("Name").setComparator((p1, p2) -> p1.getName()
                        .compareToIgnoreCase(p2.getName()));
        Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader("Age");
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader("Street");
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader("Postal Code");

        ColumnGroup informationColumnGroup = grid
                .mergeColumns(nameColumn, ageColumn)
                .setHeader("Basic Information")
                .setFooter("Total: " + getItems().size() + " people");
        ColumnGroup addressColumnGroup = grid
                .mergeColumns(streetColumn, postalCodeColumn)
                .setHeader("Address Information");
        grid.mergeColumns(informationColumnGroup, addressColumnGroup);

        // end-source-example
        grid.setId("grid-header-with-templates");
        addCard("Using templates", "Column header and footer using templates",
                grid);
    }

    private void createHeaderAndFooterUsingComponents() {
        // begin-source-example
        // source-example-heading: Column header and footer using components
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        Column<Person> nameColumn = grid.addColumn(Person::getName)
                .setHeader(new Label("Name")).setComparator((p1, p2) -> p1
                        .getName().compareToIgnoreCase(p2.getName()));
        Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader(new Label("Age"));
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader(new Label("Street"));
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader(new Label("Postal Code"));

        ColumnGroup informationColumnGroup = grid
                .mergeColumns(nameColumn, ageColumn)
                .setHeader(new Label("Basic Information")).setFooter(
                        new Label("Total: " + getItems().size() + " people"));
        ColumnGroup addressColumnGroup = grid
                .mergeColumns(streetColumn, postalCodeColumn)
                .setHeader(new Label("Address Information"));
        grid.mergeColumns(informationColumnGroup, addressColumnGroup);

        // end-source-example
        grid.setId("grid-header-with-components");
        addCard("Using components", "Column header and footer using components",
                grid);
    }

    private void createBeanGrid() {
        // begin-source-example
        // source-example-heading: Automatically adding columns
        // Providing a bean-type generates columns for all of it's properties
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setItems(getItems());

        // Property-names are automatically set as keys
        // You can remove undesired columns by using the key
        grid.removeColumnByKey("id");

        // Columns for sub-properties can be added easily
        grid.addColumn("address.postalCode");

        // end-source-example
        grid.setId("bean-grid");
        addCard("Configuring Columns", "Automatically adding columns", grid);
    }

    private void createBasicRenderers() {
        // begin-source-example
        // source-example-heading: Using basic renderers
        Grid<Item> grid = new Grid<>();
        grid.setItems(getShoppingCart());

        grid.addColumn(Item::getName).setHeader("Name");

        // NumberRenderer to render numbers in general
        grid.addColumn(new NumberRenderer<>(Item::getPrice, "$ %(,.2f",
                Locale.US, "$ 0.00")).setHeader("Price");

        // LocalDateTimeRenderer for date and time
        grid.addColumn(new LocalDateTimeRenderer<>(Item::getPurchaseDate,
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                        FormatStyle.MEDIUM)))
                .setHeader("Purchase date and time").setFlexGrow(2);

        // LocalDateRenderer for dates
        grid.addColumn(new LocalDateRenderer<>(Item::getEstimatedDeliveryDate,
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Estimated delivery date");

        // Icons
        grid.addColumn(new IconRenderer<>(
                item -> item.getPrice() > 50 ? new Label("$$$")
                        : new Label("$"),
                item -> ""));

        // NativeButtonRenderer for an easy clickable button,
        // without creating a component
        grid.addColumn(new NativeButtonRenderer<>("Remove", item -> {
            ListDataProvider<Item> dataProvider = (ListDataProvider<Item>) grid
                    .getDataProvider();
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        })).setWidth("100px").setFlexGrow(0);

        // end-source-example

        grid.setId("grid-basic-renderers");
        addCard("Using renderers", "Using basic renderers", grid);
    }

    private void createHeightByRows() {
        // begin-source-example
        // source-example-heading: Using height by rows
        Grid<Person> grid = new Grid<>();

        // When using heightByRows, all items are fetched and
        // Grid uses all the space needed to render everything.
        grid.setHeightByRows(true);

        List<Person> people = createItems(50);
        grid.setItems(people);

        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);
        // end-source-example

        grid.setId("grid-height-by-rows");
        addCard("Height by Rows", "Using height by rows", grid);
    }

    private void createBasicFeatures() {
        final int baseYear = 2015;
        final int numberOfYears = 5;

        // begin-source-example
        // source-example-heading: Gird Basic Features Demo
        DecimalFormat dollarFormat = new DecimalFormat("$#,##0.00");
        Grid<CompanyBudgetHistory> grid = new Grid<>();
        ListDataProvider<CompanyBudgetHistory> list = CompanyBudgetHistory
                .getBudgetDataProvider(baseYear, numberOfYears);

        grid.setColumnReorderingAllowed(true);

        grid.setDataProvider(list);
        grid.addColumn(CompanyBudgetHistory::getCompany).setHeader("Company");

        grid.setSelectionMode(SelectionMode.SINGLE);

        IntStream.range(baseYear, baseYear+numberOfYears).forEach(year ->{
            BigDecimal firstHalfSum = list.fetch(new Query<>())
                    .collect(Collectors.toList()).stream()
                    .map(
                    budgetHistory -> budgetHistory.getFirstHalfOfYear(year))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal secondHalfSum = list.fetch(new Query<>())
                    .collect(Collectors.toList()).stream()
                    .map(budgetHistory -> budgetHistory
                            .getSecondHalfOfYear(year))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Column<?> firstHalfColumn = grid
                    .addColumn(
                            new NumberRenderer<>(
                                    budgetHistory -> budgetHistory
                                            .getFirstHalfOfYear(year),
                                    dollarFormat))
                    .setHeader("H1")
                    .setFooter(dollarFormat.format(firstHalfSum))
             .setComparator((p1, p2) -> p1.getFirstHalfOfYear(year)
                            .compareTo(p2.getFirstHalfOfYear(year)));

            Column<?> secondHalfColumn = grid
                    .addColumn(
                            new NumberRenderer<>(
                                    budgetHistory -> budgetHistory
                                            .getSecondHalfOfYear(year),
                                    dollarFormat))
                    .setHeader("H2")
                    .setFooter(dollarFormat.format(secondHalfSum))
                    .setComparator((p1, p2) -> p1.getSecondHalfOfYear(year)
                            .compareTo(p2.getSecondHalfOfYear(year)));

            grid.mergeColumns(firstHalfColumn, secondHalfColumn)
                    .setHeader(year + "");
        });
        // end-source-example

        grid.setId("grid-basic-feature");
        addCard("Basic Features", "Grid Basic Features Demo", grid);
    }

    private List<Person> getItems() {
        return items;
    }

    private static List<Person> createItems() {
        return createItems(500);
    }

    private static List<Person> createItems(int number) {
        Random random = new Random(0);
        return IntStream.range(1, number)
                .mapToObj(index -> createPerson(index, random))
                .collect(Collectors.toList());
    }

    private static Person createPerson(int index, Random random) {
        Person person = new Person();
        person.setId(index);
        person.setName("Person " + index);
        person.setAge(13 + random.nextInt(50));

        Address address = new Address();
        address.setStreet("Street " + ((char) ('A' + random.nextInt(26))));
        address.setNumber(1 + random.nextInt(50));
        address.setPostalCode(String.valueOf(10000 + random.nextInt(8999)));
        person.setAddress(address);

        return person;
    }

    private static List<Item> getShoppingCart() {
        Random random = new Random(42);
        LocalDate baseDate = LocalDate.of(2018, 1, 10);
        return IntStream.range(1, 101)
                .mapToObj(index -> createItem(index, random, baseDate))
                .collect(Collectors.toList());

    }

    private static Item createItem(int index, Random random,
            LocalDate baseDate) {
        Item item = new Item();
        item.setName("Item " + index);
        item.setPrice(100 * random.nextDouble());
        item.setPurchaseDate(baseDate.atTime(12, 0)
                .minus(1 + random.nextInt(3600), ChronoUnit.SECONDS));
        item.setEstimatedDeliveryDate(
                baseDate.plus(1 + random.nextInt(15), ChronoUnit.DAYS));
        return item;
    }

    private static final String[] companies = new String[] { "Deomic",
            "Seumosis", "Feortor", "Deynazu", "Deynomia", "Leaudous",
            "Aembizio", "Rehyic", "Ceervous", "Ientralium", "Deicee", "Uenimbo",
            "Reetroyo", "Heemicy", "Aevinix", "Aemor", "Reoolane", "Keify",
            "Deisor", "Geradindu", "Teelembee", "Seysil", "Meutz", "Seubil",
            "Seylible", "Zeare", "Ceomescent", "Ceapill", "Heyperend",
            "Felinix", "Heyponte", "Veertent", "Ceentimbee", "Heomovu",
            "Deiante", "Meedido", "Perexo", "Neeotri", "Aecerile", "Meovive",
            "Ferontent", "Meultimbee", "Meisile", "Aerdonia", "Deiegen",
            "Meonible", "Oepe", "Aentemba", "Ceorore", "Peaner", "Seuril",
            "Oeutill", "Aenill", "Aezmie", "Ceheckmarks", "Aeponu",
            "Iesonoodle", "Ceogipe", "Beellescent", "Deuoveo", "Seufible",
            "Veicejo", "Aemphidel", "Ceryova", "Seucous", "Aeudoid", "Iediomba",
            "Deivagen", "Meicrofy", "Qeuinyx", "Seemizzy", "Eequzu", "Aebante",
            "Peedic", "Feelosis", "Meifix", "Pelanix", "Eeipe", "Deemoxo",
            "Eenity", "Peostil", "Ceogen", "Ienent", "Eeafix", "Sekicero",
            "Seugil", "Leunive", "Ceircumity", "Seupratri", "Eecofy", "Aentizz",
            "Peyrolium", "Ceryptonyx", "Seuposis", "Keayor", "Ceamiveo",
            "Neonise", "Kealith", "Aeloo", "Deelith", "Aequnu", "Peremose",
            "Qeuambo", "Tewimba", "Meanuta", "Veiva", "Veenity", "Eepimia",
            "Ueberore", "Mealible", "Ceonose", "Peortill", "Meidile", "Leupill",
            "Aeginoodle", "Seurosis", "Veerize", "Reedo", "Beiocy", "Geeodel",
            "Pearadel", "Yeajo", "Geenive", "Aeutonix", "Terimbu", "Seynend",
            "Ceedescent", "Ceanise", "Ceontranti", "Seuperoid", "Ueltradoo",
            "Veivity", "Mearil", "Peolyive", "Cealcose", "Leeenix", "Gearore",
            "Teaveo", "Seocinix", "Aestromba", "Meetanu", "Zeoodeo",
            "Iensulill", "Leaveo", "Peodible", "Meegatz", "Eesend", "Aevamba",
            "Veooloo", "Oectombo", "Neymba", "Qeuasinoodle", "Eexise", "Seusor",
            "Teenoid", "Seyill", "Pealeoveo", "Geefy", "Beonill", "Peerosis",
            "Teransise", "Aeurive", "Beinoodle", "Aerchile", "Ceolent",
            "Perosaria", "Meaxidoo", "Feinile", "Deemilane", "Leocill",
            "Deudel", "Aenimil", "Deominose", "Perondo", "Deifity", "Peeridoo",
            "Jeaxo", "Feafy", "Beefy", "Deolible", "Heydrombu", "Ienfratz",
            "Sekyic", "Meyil", "Ienterer", "Eexecure", "Feoril", "Seymist",
            "Peixope", "Aelbent", "Oemninoodle", "Uenose", "Secimbo", "Beovic",
            "Fealcoid", "Perotope", "Yeozz", "Aeicero", "Aelicy", "Eelectrombu",
            "Ceoracee", "Kewivu", "Weikiyo", "Meeevee", "Eeurodel", "Yeakitude",
            "Oeyovee", "Ceisic", "Terufix", "Meistijo", "Iedeofix", "Sekazu" };

    // begin-source-example
    // source-example-heading: Gird Basic Features Demo
    /**
     * Example Object for Basic Features Demo
     */
    public static class YearlyBudgetInfo {
        BigDecimal firstHalf;
        BigDecimal secondHalf;

        public YearlyBudgetInfo(BigDecimal firstHalf, BigDecimal secondHalf) {
            this.firstHalf = firstHalf;
            this.secondHalf = secondHalf;
        }

        public BigDecimal getFirstHalf() {
            return firstHalf;
        }

        public void setFirstHalf(BigDecimal firstHalf) {
            this.firstHalf = firstHalf;
        }

        public BigDecimal getSecondHalf() {
            return secondHalf;
        }

        public void setSecondHalf(BigDecimal secondHalf) {
            this.secondHalf = secondHalf;
        }
    }

    /**
     * Example Object for Basic Features Demo
     */
    public static class CompanyBudgetHistory {
        String company;
        Map<Integer, YearlyBudgetInfo> budgetHistory;

        public CompanyBudgetHistory(String company,
                Map<Integer, YearlyBudgetInfo> budgetHistory) {
            this.company = company;
            this.budgetHistory = budgetHistory;
        }

        public String getCompany() {
            return company;
        }

        public BigDecimal getFirstHalfOfYear(int year) {
            if (!budgetHistory.containsKey(year)) {
                return null;
            }
            return budgetHistory.get(year).getFirstHalf();
        }

        public BigDecimal getSecondHalfOfYear(int year) {
            if (!budgetHistory.containsKey(year)) {
                return null;
            }
            return budgetHistory.get(year).getSecondHalf();
        }

        public static ListDataProvider<CompanyBudgetHistory> getBudgetDataProvider(
                final int baseYear, final int numYears) {
            Collection<CompanyBudgetHistory> companyBudgetHistories = new ArrayList<>();

            for (String company : companies) {
                Map<Integer, YearlyBudgetInfo> budgetHistory = new HashMap<>();
                for (int year = baseYear; year < baseYear + numYears; year++) {
                    YearlyBudgetInfo budgetInfo = new YearlyBudgetInfo(
                            getRandomBigDecimal(), getRandomBigDecimal());
                    budgetHistory.put(year, budgetInfo);
                }
                companyBudgetHistories
                        .add(new CompanyBudgetHistory(company, budgetHistory));
            }
            return new ListDataProvider<>(companyBudgetHistories);
        }

        public static BigDecimal getRandomBigDecimal() {
            return new BigDecimal(
                    100 + Math.random() * 100 + Math.random() * 10000);
        }
    }
    // end-source-example
}

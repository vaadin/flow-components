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
package com.vaadin.flow.component.grid.it;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.GridContextMenu;
import com.vaadin.flow.component.contextmenu.GridMenuItem;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.bean.Item;
import com.vaadin.flow.data.bean.ItemGenerator;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.data.bean.PersonWithLevel;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Grid} demo.
 */
@Route("vaadin-grid-it-demo")
@HtmlImport("grid-demo-styles.html")
public class GridView extends DemoView {

    public static List<Person> items = new ArrayList<>();
    public static List<PersonWithLevel> rootItems = new ArrayList<>();
    static {
        items = createItems();
        rootItems = createRootItems();
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
         * @param person
         *            the person to set
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
            text = "Hi, I'm " + person.getFirstName() + "!";
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
            addClassName("custom-details");
            setId("person-card-" + person.getId());

            VerticalLayout layout1 = new VerticalLayout();
            layout1.add(new Label("Name: " + person.getFirstName()));
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

            add(hlayout);
        }
    }
    // end-source-example

    @Override
    protected void initView() {
        createBasicUsage();
        createNoHeaderGrid();
        createCallBackDataProvider();
        createSingleSelect();
        createMultiSelect();
        createNoneSelect();
        createColumnApiExample();
        createBasicRenderers();
        createColumnTemplate();
        createColumnComponentRenderer();
        createItemDetails();
        createItemDetailsOpenedProgrammatically();
        createSorting();
        createGridWithHeaderAndFooterRows();
        createHeaderAndFooterUsingComponents();
        createGridWithFilters();
        createStyling();
        createBeanGrid();
        createHeightByRows();
        createBasicFeatures();
        createDisabledGrid();
        createBasicTreeGridUsage();
        createLazyLoadingTreeGridUsage();
        createContextMenu();
        createContextSubMenu();
        addVariantFeature();
        createClickListener();
        createDoubleClickListener();
        createBufferedEditor();
        createNotBufferedEditor();
        createBufferedDynamicEditor();
        createNotBufferedDynamicEditor();

        addCard("Grid example model",
                new Label("These objects are used in the examples above"));
    }

    @Override
    public void populateSources() {
    }

    private void addVariantFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        Grid<Person> grid = new Grid<>();
        grid.setItems(createItems(50));
        grid.addColumn(Person::getFirstName).setHeader("NAME");
        grid.addColumn(Person::getAge).setHeader("AGE");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        // end-source-example

        addVariantsDemo(() -> {
            return grid;
        }, Grid::addThemeVariants, Grid::removeThemeVariants,
                GridVariant::getVariantName, GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);
    }

    private void createBasicUsage() {
        // begin-source-example
        // source-example-heading: Grid Basics
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        // end-source-example
        grid.setId("basic");

        addCard("Grid Basics", grid);
    }

    private void createNoHeaderGrid() {
        // begin-source-example
        // source-example-heading: Grid Basic with no header
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        grid.addColumn(Person::getFirstName);
        grid.addColumn(Person::getAge);

        // end-source-example
        grid.setId("noHeader");

        addCard("Grid Basic with no header", grid);
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
        PeopleGenerator generator = new PeopleGenerator();
        grid.setDataProvider(DataProvider.fromCallbacks(
                query -> IntStream
                        .range(query.getOffset(),
                                query.getOffset() + query.getLimit())
                        .mapToObj(index -> generator.createPerson(index + 1)),
                query -> 100 * 1000 * 1000));

        grid.addColumn(Person::getFirstName).setHeader("Name");
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

        grid.addColumn(Person::getFirstName).setHeader("Name");
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

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.MULTI);

        grid.asMultiSelect()
                .addSelectionListener(event -> messageDiv.setText(String.format(
                        "Selection changed from %s to %s, selection is from client: %s",
                        event.getOldValue(), event.getValue(),
                        event.isFromClient())));

        // You can pre-select items
        grid.asMultiSelect().select(people.get(0), people.get(1));

        NativeButton selectBtn = new NativeButton("Select first five persons");
        selectBtn.addClickListener(event -> grid.asMultiSelect()
                .select(people.subList(0, 5).toArray(new Person[5])));
        NativeButton deselectBtn = new NativeButton("Deselect all");
        deselectBtn
                .addClickListener(event -> grid.asMultiSelect().deselectAll());
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

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);
        // end-source-example
        grid.setId("none-selection");
        addCard("Selection", "Grid with No Selection Enabled", grid);
    }

    private void createColumnTemplate() {
        List<Person> items = new ArrayList<>();
        items.addAll(createItems());

        // begin-source-example
        // source-example-heading: Grid with columns using template renderer
        Grid<Person> grid = new Grid<>();
        grid.setItems(items);

        // You can use the [[index]] variable to print the row index (0 based)
        grid.addColumn(TemplateRenderer.of("[[index]]")).setHeader("#");

        // You can set any property by using `withProperty`, including
        // properties not present on the original bean.
        grid.addColumn(TemplateRenderer.<Person> of(
                "<div title='[[item.firstName]]'>[[item.firstName]]<br><small>[[item.yearsOld]]</small></div>")
                .withProperty("firstName", Person::getFirstName)
                .withProperty("yearsOld",
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
                    person.setFirstName(person.getFirstName() + " Updated");
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

    private void createColumnComponentRenderer() {
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
                        person.setFirstName(name);
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

    private void createGridWithHeaderAndFooterRows() {
        // begin-source-example
        // source-example-heading: Adding header and footer rows
        Grid<Person> grid = new Grid<>();
        grid.setItems(createItems());

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name").setComparator((p1, p2) -> p1.getFirstName()
                        .compareToIgnoreCase(p2.getFirstName()));
        Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader("Age");
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader("Street");
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader("Postal Code");

        HeaderRow topRow = grid.prependHeaderRow();

        HeaderCell informationCell = topRow.join(nameColumn, ageColumn);
        informationCell.setText("Basic Information");

        HeaderCell addressCell = topRow.join(streetColumn, postalCodeColumn);
        addressCell.setText("Address Information");

        grid.appendFooterRow().getCell(nameColumn)
                .setText("Total: " + getItems().size() + " people");
        // end-source-example
        grid.setId("grid-with-header-and-footer-rows");
        addCard("Header and footer rows", "Adding header and footer rows",
                grid);
    }

    private void createHeaderAndFooterUsingComponents() {
        // begin-source-example
        // source-example-heading: Header and footer using components
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader(new Label("Name")).setComparator((p1, p2) -> p1
                        .getFirstName().compareToIgnoreCase(p2.getFirstName()));
        Column<Person> ageColumn = grid.addColumn(Person::getAge, "age")
                .setHeader(new Label("Age"));
        Column<Person> streetColumn = grid
                .addColumn(person -> person.getAddress().getStreet())
                .setHeader(new Label("Street"));
        Column<Person> postalCodeColumn = grid
                .addColumn(person -> person.getAddress().getPostalCode())
                .setHeader(new Label("Postal Code"));

        HeaderRow topRow = grid.prependHeaderRow();

        HeaderCell informationCell = topRow.join(nameColumn, ageColumn);
        informationCell.setComponent(new Label("Basic Information"));

        HeaderCell addressCell = topRow.join(streetColumn, postalCodeColumn);
        addressCell.setComponent(new Label("Address Information"));

        grid.appendFooterRow().getCell(nameColumn).setComponent(
                new Label("Total: " + getItems().size() + " people"));
        // end-source-example
        grid.setId("grid-header-with-components");
        addCard("Header and footer rows", "Header and footer using components",
                grid);
    }

    private void createGridWithFilters() {
        // begin-source-example
        // source-example-heading: Using text fields for filtering items
        Grid<Person> grid = new Grid<>();
        ListDataProvider<Person> dataProvider = new ListDataProvider<>(
                createItems());
        grid.setDataProvider(dataProvider);

        List<ValueProvider<Person, String>> valueProviders = new ArrayList<>();
        valueProviders.add(Person::getFirstName);
        valueProviders.add(person -> String.valueOf(person.getAge()));
        valueProviders.add(person -> person.getAddress().getStreet());
        valueProviders.add(
                person -> String.valueOf(person.getAddress().getPostalCode()));

        Iterator<ValueProvider<Person, String>> iterator = valueProviders
                .iterator();

        grid.addColumn(iterator.next()).setHeader("Name");
        grid.addColumn(iterator.next()).setHeader("Age");
        grid.addColumn(iterator.next()).setHeader("Street");
        grid.addColumn(iterator.next()).setHeader("Postal Code");

        HeaderRow filterRow = grid.appendHeaderRow();

        Iterator<ValueProvider<Person, String>> iterator2 = valueProviders
                .iterator();

        grid.getColumns().forEach(column -> {
            TextField field = new TextField();
            ValueProvider<Person, String> valueProvider = iterator2.next();

            field.addValueChangeListener(event -> dataProvider
                    .addFilter(person -> StringUtils.containsIgnoreCase(
                            valueProvider.apply(person), field.getValue())));

            field.setValueChangeMode(ValueChangeMode.EAGER);

            filterRow.getCell(column).setComponent(field);
            field.setSizeFull();
            field.setPlaceholder("Filter");
        });
        // end-source-example
        grid.setId("grid-with-filters");
        addCard("Filtering", "Using text fields for filtering items", grid);
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
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
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

        RadioButtonGroup<ColumnTextAlign> alignments = new RadioButtonGroup<>();
        alignments.setItems(ColumnTextAlign.values());
        alignments.setLabel("Text alignment for the Age column");
        alignments.setValue(ColumnTextAlign.START);
        alignments.addValueChangeListener(event -> grid.getColumnByKey("age")
                .setTextAlign(event.getValue()));
        // end-source-example

        grid.setId("column-api-example");
        idColumnVisibility.setId("toggle-id-column-visibility");
        userReordering.setId("toggle-user-reordering");
        freezeIdColumn.setId("toggle-id-column-frozen");
        freezeSelectionColumn.setId("toggle-selection-column-frozen");
        alignments.setId("toggle-text-align");
        addCard("Configuring columns", "Column API example", grid,
                new VerticalLayout(idColumnVisibility, userReordering,
                        freezeIdColumn, freezeSelectionColumn, alignments));
    }

    private Grid<Person> createGridWithDetails() {
        // begin-source-example
        // source-example-heading: Grid with item details
        Grid<Person> grid = new Grid<>();
        List<Person> people = createItems();
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.setSelectionMode(SelectionMode.NONE);

        // You can use any renderer for the item details. By default, the
        // details are opened and closed by clicking the rows.
        grid.setItemDetailsRenderer(TemplateRenderer.<Person> of(
                "<div class='custom-details' style='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>Hi! My name is <b>[[item.firstName]]!</b></div>"
                        + "<div><button on-click='handleClick'>Update Person</button></div>"
                        + "</div>")
                .withProperty("firstName", Person::getFirstName)
                .withEventHandler("handleClick", person -> {
                    person.setFirstName(person.getFirstName() + " Updated");
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

    private void createSorting() {
        Div messageDiv = new Div();
        // begin-source-example
        // source-example-heading: Grid with sortable columns
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.setSelectionMode(SelectionMode.NONE);

        grid.addColumn(Person::getFirstName, "firstName").setHeader("Name");
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

        // you can set the sort order from server-side with the grid.sort method
        NativeButton invertAllSortings = new NativeButton(
                "Invert all sort directions", event -> {
                    List<GridSortOrder<Person>> orderList = grid.getSortOrder();
                    List<GridSortOrder<Person>> newOrderList = new ArrayList<>(
                            orderList.size());
                    for (GridSortOrder<Person> sort : orderList) {
                        newOrderList.add(new GridSortOrder<>(sort.getSorted(),
                                sort.getDirection().getOpposite()));
                    }
                    grid.sort(newOrderList);
                });

        NativeButton resetAllSortings = new NativeButton("Reset all sortings",
                event -> grid.sort(null));
        // end-source-example
        grid.setId("grid-sortable-columns");
        multiSort.setId("grid-multi-sort-toggle");
        invertAllSortings.setId("grid-sortable-columns-invert-sortings");
        resetAllSortings.setId("grid-sortable-columns-reset-sortings");
        messageDiv.setId("grid-sortable-columns-message");
        addCard("Sorting", "Grid with sortable columns", grid, multiSort,
                invertAllSortings, resetAllSortings, messageDiv);
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

        // You can also configure the included properties and their order with
        // a single method call
        NativeButton showBasicInformation = new NativeButton(
                "Show basic information",
                event -> grid.setColumns("firstName", "age", "address"));
        NativeButton showAddressInformation = new NativeButton(
                "Show address information",
                event -> grid.setColumns("address.street", "address.number",
                        "address.postalCode"));
        // end-source-example
        grid.setId("bean-grid");
        showBasicInformation.setId("show-basic-information");
        showAddressInformation.setId("show-address-information");
        addCard("Configuring Columns", "Automatically adding columns", grid,
                showBasicInformation, showAddressInformation);
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

        grid.addColumn(Person::getFirstName).setHeader("Name");
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
        // source-example-heading: Grid Basic Features Demo
        DecimalFormat dollarFormat = new DecimalFormat("$#,##0.00");
        Grid<CompanyBudgetHistory> grid = new Grid<>();

        ListDataProvider<CompanyBudgetHistory> list = CompanyBudgetHistory
                .getBudgetDataProvider(baseYear, numberOfYears);
        grid.setDataProvider(list);

        grid.setColumnReorderingAllowed(true);

        Column<CompanyBudgetHistory> companyNameColumn = grid
                .addColumn(CompanyBudgetHistory::getCompany)
                .setHeader("Company");
        companyNameColumn.setWidth("200px");

        grid.setSelectionMode(SelectionMode.SINGLE);

        HeaderRow topHeader = grid.prependHeaderRow();

        IntStream.range(baseYear, baseYear + numberOfYears).forEach(year -> {
            BigDecimal firstHalfSum = list.fetch(new Query<>())
                    .collect(Collectors.toList()).stream()
                    .map(budgetHistory -> budgetHistory
                            .getFirstHalfOfYear(year))
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
                    .setHeader("H1").setTextAlign(ColumnTextAlign.END)
                    .setFooter(dollarFormat.format(firstHalfSum))
                    .setComparator((p1, p2) -> p1.getFirstHalfOfYear(year)
                            .compareTo(p2.getFirstHalfOfYear(year)));

            Column<?> secondHalfColumn = grid
                    .addColumn(
                            new NumberRenderer<>(
                                    budgetHistory -> budgetHistory
                                            .getSecondHalfOfYear(year),
                                    dollarFormat))
                    .setHeader("H2").setTextAlign(ColumnTextAlign.END)
                    .setFooter(dollarFormat.format(secondHalfSum))
                    .setComparator((p1, p2) -> p1.getSecondHalfOfYear(year)
                            .compareTo(p2.getSecondHalfOfYear(year)));

            topHeader.join(firstHalfColumn, secondHalfColumn)
                    .setText(year + "");
        });

        HeaderRow filteringHeader = grid.appendHeaderRow();

        TextField filteringField = new TextField();
        filteringField.addValueChangeListener(event -> {
            list.setFilter(CompanyBudgetHistory::getCompany, company -> {
                if (company == null) {
                    return false;
                }
                String companyLower = company.toLowerCase(Locale.ENGLISH);
                String filterLower = event.getValue()
                        .toLowerCase(Locale.ENGLISH);
                return companyLower.contains(filterLower);
            });
        });
        filteringField.setPlaceholder("Filter");
        filteringField.setWidth("100%");

        filteringHeader.getCell(companyNameColumn).setComponent(filteringField);

        // end-source-example

        grid.setId("grid-basic-feature");
        addCard("Basic Features", "Grid Basic Features Demo", grid);
    }

    private void createDisabledGrid() {
        // begin-source-example
        // source-example-heading: Disabled grid
        Grid<Person> grid = new Grid<>();

        List<Person> people = createItems(500);
        grid.setItems(people);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        grid.addColumn(new NativeButtonRenderer<>("Button"))
                .setHeader("Action");

        grid.setSelectionMode(SelectionMode.SINGLE);

        // The selection and action button won't work, but the scrolling will
        grid.setEnabled(false);
        // end-source-example

        NativeButton toggleEnable = new NativeButton("Toggle enable",
                evt -> grid.setEnabled(!grid.isEnabled()));
        toggleEnable.setId("disabled-grid-toggle-enable");
        Div div = new Div(toggleEnable);

        grid.setId("disabled-grid");
        addCard("Disabled grid", grid, div);
    }

    private Map<PersonWithLevel, List<PersonWithLevel>> childMap;

    private void createBasicTreeGridUsage() {
        childMap = new HashMap<>();
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid Basics
        TreeGrid<PersonWithLevel> grid = new TreeGrid<>();
        grid.setItems(getRootItems(), item -> {
            if ((item.getLevel() == 0 && item.getId() > 10)
                    || item.getLevel() > 1) {
                return Collections.emptyList();
            }
            if (!childMap.containsKey(item)) {
                childMap.put(item, createSubItems(81, item.getLevel() + 1));
            }
            return childMap.get(item);
        });
        grid.addHierarchyColumn(Person::getFirstName).setHeader("Hierarchy");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        grid.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        // end-source-example
        grid.setId("treegridbasic");

        TextField name = new TextField("Name of selected person");
        grid.addSelectionListener(event -> name.setValue(event
                .getFirstSelectedItem().map(Person::getFirstName).orElse("")));
        NativeButton save = new NativeButton("Save", event -> {
            grid.getSelectionModel().getFirstSelectedItem()
                    .ifPresent(person -> person.setFirstName(name.getValue()));
            grid.getSelectionModel().getFirstSelectedItem().ifPresent(
                    person -> grid.getDataProvider().refreshItem(person));
        });
        HorizontalLayout nameEditor = new HorizontalLayout(name, save);

        addCard("TreeGrid", "TreeGrid Basics", withTreeGridToggleButtons(
                getRootItems(), grid, nameEditor, message));
    }

    private void createLazyLoadingTreeGridUsage() {
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid with lazy loading
        TreeGrid<HierarchicalTestBean> grid = new TreeGrid<>();
        grid.addHierarchyColumn(HierarchicalTestBean::toString)
                .setHeader("Hierarchy");
        grid.addColumn(HierarchicalTestBean::getDepth).setHeader("Depth");
        grid.addColumn(HierarchicalTestBean::getIndex)
                .setHeader("Index on this depth");
        grid.setDataProvider(
                new AbstractBackEndHierarchicalDataProvider<HierarchicalTestBean, Void>() {

                    private final int nodesPerLevel = 3;
                    private final int depth = 2;

                    @Override
                    public int getChildCount(
                            HierarchicalQuery<HierarchicalTestBean, Void> query) {

                        Optional<Integer> count = query.getParentOptional()
                                .flatMap(parent -> Optional.of(Integer
                                        .valueOf((internalHasChildren(parent)
                                                ? nodesPerLevel
                                                : 0))));

                        return count.orElse(nodesPerLevel);
                    }

                    @Override
                    public boolean hasChildren(HierarchicalTestBean item) {
                        return internalHasChildren(item);
                    }

                    private boolean internalHasChildren(
                            HierarchicalTestBean node) {
                        return node.getDepth() < depth;
                    }

                    @Override
                    protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
                            HierarchicalQuery<HierarchicalTestBean, Void> query) {
                        final int depth = query.getParentOptional().isPresent()
                                ? query.getParent().getDepth() + 1
                                : 0;
                        final Optional<String> parentKey = query
                                .getParentOptional()
                                .flatMap(parent -> Optional.of(parent.getId()));

                        List<HierarchicalTestBean> list = new ArrayList<>();
                        int limit = Math.min(query.getLimit(), nodesPerLevel);
                        for (int i = 0; i < limit; i++) {
                            list.add(new HierarchicalTestBean(
                                    parentKey.orElse(null), depth,
                                    i + query.getOffset()));
                        }
                        return list.stream();
                    }
                });

        // end-source-example
        grid.setId("treegridlazy");

        grid.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        grid.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        addCard("TreeGrid", "TreeGrid with lazy loading",
                withTreeGridToggleButtons(grid.getDataProvider().fetch(
                        new HierarchicalQuery<HierarchicalTestBean, SerializablePredicate<HierarchicalTestBean>>(
                                null, null))
                        .collect(Collectors.toList()), grid, message));
    }

    private void createContextMenu() {
        // begin-source-example
        // source-example-heading: Using ContextMenu With Grid
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        GridContextMenu<Person> contextMenu = new GridContextMenu<>(grid);
        contextMenu.addItem("Update", event -> {
            event.getItem().ifPresent(person -> {
                person.setFirstName(person.getFirstName() + " Updated");
                ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) event
                        .getGrid().getDataProvider();
                dataProvider.refreshItem(person);
            });
        });
        contextMenu.addItem("Remove", event -> {
            event.getItem().ifPresent(person -> {
                ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                        .getDataProvider();
                dataProvider.getItems().remove(person);
                dataProvider.refreshAll();
            });
        });
        // end-source-example
        grid.setId("context-menu-grid");
        addCard("Context Menu", "Using ContextMenu With Grid", grid,
                contextMenu);
    }

    // Context sub Menu begin
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void createContextSubMenu() {
        // begin-source-example
        // source-example-heading: Using Context Sub Menu With Grid
        Grid<Person> grid = new Grid<>();

        ListDataProvider<Person> dataProvider = DataProvider
                .ofCollection(getItems());

        grid.setDataProvider(dataProvider);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");
        GridContextMenu<Person> contextMenu = new GridContextMenu<>(grid);
        GridMenuItem<Person> insert = contextMenu.addItem("Insert");

        insert.getSubMenu().addItem("Insert a row above", event -> {
            Optional<Person> item = event.getItem();
            if (!item.isPresent()) {
                // no selected row
                return;
            }
            List<Person> items = (List) dataProvider.getItems();
            items.add(items.indexOf(item.get()),
                    new PeopleGenerator().createPerson(items.size() + 1));
            dataProvider.refreshAll();
        });
        insert.getSubMenu().add(new Hr());
        insert.getSubMenu().addItem("Insert a row below", event -> {
            Optional<Person> item = event.getItem();
            if (!item.isPresent()) {
                // no selected row
                return;
            }
            List<Person> items = (List) dataProvider.getItems();
            items.add(items.indexOf(item.get()) + 1,
                    new PeopleGenerator().createPerson(items.size() + 1));
            dataProvider.refreshAll();
        });
        // end-source-example
        grid.setId("context-submenu-grid");
        addCard("Context Menu", "Using Context Sub Menu With Grid", grid,
                contextMenu);
    }

    private void createClickListener() {
        Div message = new Div();
        message.setId("clicked-item");

        // begin-source-example
        // source-example-heading: Item Click Listener
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        // Disable selection: will receive only click events instead
        grid.setSelectionMode(SelectionMode.NONE);

        grid.addItemClickListener(event -> message
                .setText("Clicked Item: " + event.getItem().getFirstName()));

        // end-source-example
        grid.setId("item-click-listener");

        message.addClickListener(event -> message.setText(""));
        addCard("Click Listeners", "Item Click Listener", message, grid);
    }

    private void createDoubleClickListener() {
        Div message = new Div();
        message.setId("doubleclicked-item");

        // begin-source-example
        // source-example-heading: Item Double Click Listener
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.addColumn(Person::getFirstName).setHeader("Name");
        grid.addColumn(Person::getAge).setHeader("Age");

        grid.addItemDoubleClickListener(event -> message.setText(
                "Double Clicked Item: " + event.getItem().getFirstName()));

        // end-source-example
        grid.setId("item-doubleclick-listener");
        message.addClickListener(event -> message.setText(""));
        addCard("Click Listeners", "Item Double Click Listener", message, grid);
    }

    private void createBufferedEditor() {
        Div message = new Div();
        message.setId("buffered-editor-msg");

        // begin-source-example
        // source-example-heading: Editor in Buffered Mode
        Grid<Person> grid = new Grid<>();
        List<Person> persons = getItems();
        grid.setItems(persons);
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        Div validationStatus = new Div();
        validationStatus.setId("validation");

        TextField field = new TextField();
        binder.forField(field)
                .withValidator(name -> name.startsWith("Person"),
                        "Name should start with Person")
                .withStatusLabel(validationStatus).bind("firstName");
        nameColumn.setEditorComponent(field);

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Column<Person> editorColumn = grid.addComponentColumn(person -> {
            Button edit = new Button("Edit");
            edit.addClassName("edit");
            edit.addClickListener(e -> {
                editor.editItem(person);
                field.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        // Add a keypress listener that listens for an escape key up event.
        // Note! some browsers return key as Escape and some as Esc
        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || even.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        editor.addSaveListener(
                event -> message.setText(event.getItem().getFirstName() + ", "
                        + event.getItem().isSubscriber()));

        // end-source-example
        grid.setId("buffered-editor");
        addCard("Grid Editor", "Editor in Buffered Mode", message,
                validationStatus, grid);
    }

    private void createNotBufferedEditor() {
        Div message = new Div();
        message.setId("not-buffered-editor-msg");

        // begin-source-example
        // source-example-heading: Editor in Not Buffered Mode
        Grid<Person> grid = new Grid<>();
        List<Person> persons = getItems();
        grid.setItems(persons);
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");

        Binder<Person> binder = new Binder<>(Person.class);
        grid.getEditor().setBinder(binder);

        TextField field = new TextField();
        // Close the editor in case of backward between components
        field.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && event.shiftKey");

        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        // Close the editor in case of forward navigation between
        checkbox.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && !event.shiftKey");

        grid.addItemDoubleClickListener(event -> {
            grid.getEditor().editItem(event.getItem());
            field.focus();
        });

        grid.addItemClickListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getFirstName() + ", "
                        + binder.getBean().isSubscriber());
            }
        });

        // end-source-example
        grid.setId("not-buffered-editor");
        addCard("Grid Editor", "Editor in Not Buffered Mode", message, grid);
    }

    private void createBufferedDynamicEditor() {
        Div message = new Div();
        message.setId("buffered-dynamic-editor-msg");

        // begin-source-example
        // source-example-heading: Dynamic Editor in Buffered Mode
        Grid<Person> grid = new Grid<>();
        List<Person> persons = new ArrayList<>();
        persons.addAll(createItems());
        grid.setItems(persons);

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");
        Column<Person> emailColumn = grid.addColumn(Person::getEmail)
                .setHeader("E-mail");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField field = new TextField();
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Div validationStatus = new Div();
        validationStatus.getStyle().set("color", "red");
        validationStatus.setId("email-validation");

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);

        TextField emailField = new TextField();

        // When not a subscriber, we want to show a read-only text-field that
        // ignores whatever is set to it
        TextField readOnlyEmail = new TextField();
        readOnlyEmail.setValue("Not a subscriber");
        readOnlyEmail.setReadOnly(true);

        Runnable bindEmail = () -> binder.forField(emailField)
                .withValidator(new EmailValidator("Invalid email"))
                .withStatusLabel(validationStatus).bind("email");

        Runnable setEmail = () -> emailColumn.setEditorComponent(item -> {
            if (item.isSubscriber()) {
                bindEmail.run();
                return emailField;
            } else {
                return readOnlyEmail;
            }
        });

        // Sets the binding based on the Person bean state
        setEmail.run();

        // Refresh subscriber editor component when checkbox value is changed
        checkbox.addValueChangeListener(event -> {
            // Only updates from the client-side should be taken into account
            if (event.isFromClient()) {

                // When using buffered mode, the partial updates shouldn't be
                // propagated to the bean before the Save button is clicked, so
                // here we need to override the binding function to take the
                // checkbox state into consideration instead
                emailColumn.setEditorComponent(item -> {
                    if (checkbox.getValue()) {
                        bindEmail.run();
                        return emailField;
                    } else {
                        return readOnlyEmail;
                    }
                });
                grid.getEditor().refresh();
            }
        });

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        // Resets the binding function to use the bean state whenever the editor
        // is closed
        editor.addCloseListener(event -> {
            setEmail.run();
            editButtons.stream().forEach(button -> button.setEnabled(true));
        });

        Column<Person> editorColumn = grid.addComponentColumn(person -> {
            Button edit = new Button("Edit");
            edit.addClassName("edit");
            edit.addClickListener(e -> {
                editor.editItem(person);
                field.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        // Add a keypress listener that listens for an escape key up event.
        // Note! some browsers return key as Escape and some as Esc
        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || even.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        editor.addSaveListener(
                event -> message.setText(event.getItem().getFirstName() + ", "
                        + event.getItem().isSubscriber() + ", "
                        + event.getItem().getEmail()));

        // end-source-example
        grid.setId("buffered-dynamic-editor");
        addCard("Grid Editor", "Dynamic Editor in Buffered Mode", message,
                validationStatus, grid);
    }

    private void createNotBufferedDynamicEditor() {
        Div message = new Div();
        message.setId("not-buffered-dynamic-editor-msg");

        // begin-source-example
        // source-example-heading: Dynamic Editor in Not Buffered Mode
        Grid<Person> grid = new Grid<>();
        List<Person> persons = new ArrayList<>();
        persons.addAll(createItems());
        grid.setItems(persons);

        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> subscriberColumn = grid.addColumn(Person::isSubscriber)
                .setHeader("Subscriber");
        Column<Person> emailColumn = grid.addColumn(Person::getEmail)
                .setHeader("E-mail");

        Binder<Person> binder = new Binder<>(Person.class);
        Editor<Person> editor = grid.getEditor();
        editor.setBinder(binder);

        TextField field = new TextField();
        // Close the editor in case of backward navigation between components
        field.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && event.shiftKey");
        binder.bind(field, "firstName");
        nameColumn.setEditorComponent(field);

        Checkbox checkbox = new Checkbox();
        binder.bind(checkbox, "subscriber");
        subscriberColumn.setEditorComponent(checkbox);
        // Close the editor in case of forward navigation between components
        checkbox.getElement().addEventListener("keydown", event -> {
            if (!checkbox.getValue()) {
                grid.getEditor().closeEditor();
            }
        }).setFilter("event.key === 'Tab' && !event.shiftKey");

        TextField emailField = new TextField();
        emailColumn.setEditorComponent(item -> {
            if (item.isSubscriber()) {
                binder.bind(emailField, "email");
                return emailField;
            } else {
                return null;
            }
        });
        // Close the editor in case of forward navigation between components
        emailField.getElement()
                .addEventListener("keydown",
                        event -> grid.getEditor().closeEditor())
                .setFilter("event.key === 'Tab' && !event.shiftKey");

        grid.addItemDoubleClickListener(event -> {
            grid.getEditor().editItem(event.getItem());
            field.focus();
        });

        // Re-validates the editors every time something changes on the Binder.
        // This is needed for the email column to turn into nothing when the
        // checkbox is deselected, for example.
        binder.addValueChangeListener(event -> {
            grid.getEditor().refresh();
        });

        grid.addItemClickListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getFirstName() + ", "
                        + binder.getBean().isSubscriber() + ", "
                        + binder.getBean().getEmail());
            }
        });

        // end-source-example
        grid.setId("not-buffered-dynamic-editor");
        addCard("Grid Editor", "Dynamic Editor in Not Buffered Mode", message,
                grid);
    }

    private void createStyling() {
        //@formatter:off
        /*
        // begin-source-example
        // source-example-heading: Styling Grid Cells
        // source-example-type: HTML
        <dom-module id="my-grid-theme" theme-for="vaadin-grid">
          <template>
            <style>
              [part~="cell"].subscriber {
                 background: rgb(245, 245, 255);
              }
              [part~="cell"].minor {
                color: red;
                font-weight: bold;
              }
            </style>
          </template>
        </dom-module>
        // end-source-example
        */
        //@formatter:on
        String instructions = "<p>In order to inject styles into Grid cells, "
                + "create a style-module like in the snippet below, "
                + "put it into an html-file in your resources folder, "
                + "and import it with <code>@HtmlImport</code>. "
                + "After this you can apply the CSS classes "
                + "(<code>subscriber</code> and <code>minor</code> in this case) "
                + "into grid rows and cells as shown in the next example.</p>";
        addCard("Styling", "Styling Grid Cells", new Html(instructions));

        // begin-source-example
        // source-example-heading: Generating CSS Class Names for Cells
        Grid<Person> grid = new Grid<>();
        grid.setItems(getItems());
        grid.setSelectionMode(SelectionMode.NONE);

        grid.addColumn(Person::getFirstName).setHeader("Name");
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");
        grid.addColumn(person -> person.isSubscriber() ? "Yes" : "")
                .setHeader("Subscriber");

        grid.setClassNameGenerator(
                person -> person.isSubscriber() ? "subscriber" : "");

        ageColumn.setClassNameGenerator(
                person -> person.getAge() < 18 ? "minor" : "");

        // end-source-example
        grid.setId("class-name-generator");
        addCard("Styling", "Generating CSS Class Names for Cells", grid);
    }

    private <T> Component[] withTreeGridToggleButtons(List<T> roots,
            TreeGrid<T> grid, Component... other) {
        NativeButton toggleFirstItem = new NativeButton("Toggle first item",
                evt -> {
                    if (grid.isExpanded(roots.get(0))) {
                        grid.collapse(roots.get(0));
                    } else {
                        grid.expand(roots.get(0));
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item");
        Div div1 = new Div(toggleFirstItem);

        NativeButton toggleSeveralItems = new NativeButton(
                "Toggle first three items", evt -> {
                    List<T> collapse = new ArrayList<>();
                    List<T> expand = new ArrayList<>();
                    roots.stream().limit(3).collect(Collectors.toList())
                            .forEach(p -> {
                                if (grid.isExpanded(p)) {
                                    collapse.add(p);
                                } else {
                                    expand.add(p);
                                }
                            });
                    if (!expand.isEmpty()) {
                        grid.expand(expand);
                    }
                    if (!collapse.isEmpty()) {
                        grid.collapse(collapse);
                    }
                });
        toggleSeveralItems.setId("treegrid-toggle-first-five-item");
        Div div2 = new Div(toggleSeveralItems);

        NativeButton toggleRecursivelyFirstItem = new NativeButton(
                "Toggle first item recursively", evt -> {
                    if (grid.isExpanded(roots.get(0))) {
                        grid.collapseRecursively(roots.stream().limit(1), 2);
                    } else {
                        grid.expandRecursively(roots.stream().limit(1), 2);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item-recur");
        Div div3 = new Div(toggleRecursivelyFirstItem);

        NativeButton toggleAllRecursively = new NativeButton(
                "Toggle all recursively", evt -> {
                    List<T> collapse = new ArrayList<>();
                    List<T> expand = new ArrayList<>();
                    roots.forEach(p -> {
                        if (grid.isExpanded(p)) {
                            collapse.add(p);
                        } else {
                            expand.add(p);
                        }
                    });
                    if (!expand.isEmpty()) {
                        grid.expandRecursively(expand, 2);
                    }
                    if (!collapse.isEmpty()) {
                        grid.collapseRecursively(collapse, 2);
                    }
                });
        toggleAllRecursively.setId("treegrid-toggle-all-recur");
        Div div4 = new Div(toggleAllRecursively);

        return Stream.concat(Stream.of(grid, div1, div2, div3, div4),
                Stream.of(other)).toArray(Component[]::new);
    }

    private List<Person> getItems() {
        return items.stream().map(Person::clone).collect(Collectors.toList());
    }

    private List<PersonWithLevel> getRootItems() {
        return rootItems;
    }

    private static List<Person> createItems() {
        return createItems(500);
    }

    private static List<PersonWithLevel> createRootItems() {
        return createSubItems(500, 0);
    }

    private static List<Person> createItems(int number) {
        return new PeopleGenerator().generatePeople(number);
    }

    private static List<PersonWithLevel> createSubItems(int number, int level) {
        return new PeopleGenerator().generatePeopleWithLevels(number, level);
    }

    private static List<Item> getShoppingCart() {
        return new ItemGenerator().generateItems(100);
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
    // source-example-heading: Grid Basic Features Demo
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

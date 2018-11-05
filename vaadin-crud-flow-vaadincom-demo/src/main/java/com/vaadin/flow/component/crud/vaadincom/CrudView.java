package com.vaadin.flow.component.crud.vaadincom;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DebounceSettings;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.component.crud.CrudGrid;
import com.vaadin.flow.component.crud.CrudI18n;
import com.vaadin.flow.component.crud.CrudVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.DebouncePhase;
import com.vaadin.flow.router.Route;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("serial")
@Route("vaadin-crud")
public class CrudView extends DemoView {

    @Override
    protected void initView() {
        basicCrud();
        // Using unicode spaces so as card does not show any header
        addCard(" ");
        internationalization();
        addCard("  ");
        crudWithoutFilteringAndSorting();
        customToolbar();
        customGrid();
        customSearch();
        addCard("   ");

        addCard("Example Classes",
                new Label("These objects are used in the examples above"));
    }

    private void basicCrud() {
        // begin-source-example
        // source-example-heading: Basic CRUD
        Crud<Person> crud = new Crud<>(Person.class, createPersonEditor());

        PersonDataProvider dataProvider = new PersonDataProvider();

        crud.getGrid().removeColumnByKey("id");
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        crud.addThemeVariants(CrudVariant.NO_BORDER);
        // end-source-example

        addCard("Basic CRUD", crud);
    }

     // NOTE: heading is an unicode space
     // begin-source-example
     // source-example-heading:  
    private CrudEditor<Person> createPersonEditor() {
        TextField firstName = new TextField("First name");
        TextField lastName = new TextField("Last name");
        FormLayout form = new FormLayout(firstName, lastName);

        Binder<Person> binder = new Binder<>(Person.class);
        binder.bind(firstName, Person::getFirstName, Person::setFirstName);
        binder.bind(lastName, Person::getLastName, Person::setLastName);

        return new BinderCrudEditor<>(binder, form);
    }
    // end-source-example

    private void crudWithoutFilteringAndSorting() {
        // begin-source-example
        // source-example-heading: CRUD without filtering and sorting
        CrudGrid<Person> crudGrid = new CrudGrid<>(Person.class, false);
        Crud<Person> crud = new Crud<>(Person.class, crudGrid, createPersonEditor());

        PersonDataProvider dataProvider = new PersonDataProvider();

        crud.getGrid().removeColumnByKey("id");
        crud.getGrid().setSortableColumns();
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));
        // end-source-example

        addCard("CRUD without filtering and sorting", crud);
    }

    private void internationalization() {
        // begin-source-example
        // source-example-heading: CRUD with internationalization
        Crud<Person> crud = new Crud<>(Person.class, createPersonEditor());

        crud.getGrid().removeColumnByKey("id");
        crud.setDataProvider(new PersonDataProvider());

        Button updateI18nButton = new Button("Switch to Yorùbá",
                event -> crud.setI18n(createYorubaI18n()));
        // end-source-example

        addCard("CRUD with internationalization", crud, updateI18nButton);
    }

    // NOTE: heading is two unicode spaces
    // begin-source-example
    // source-example-heading:   
    private CrudI18n createYorubaI18n() {
        CrudI18n yorubaI18n = CrudI18n.createDefault();

        yorubaI18n.setNewItem("Eeyan titun");
        yorubaI18n.setEditItem("S'atunko eeyan");
        yorubaI18n.setSaveItem("Fi pamo");
        yorubaI18n.setDeleteItem("Paare");
        yorubaI18n.setCancel("Fa'gi lee");
        yorubaI18n.setEditLabel("S'atunko eeyan");

        yorubaI18n.getConfirm().getCancel().setTitle("Akosile");
        yorubaI18n.getConfirm().getCancel().setContent("Akosile ti a o tii fi pamo nbe");
        yorubaI18n.getConfirm().getCancel().getButton().setDismiss("Se atunko sii");
        yorubaI18n.getConfirm().getCancel().getButton().setConfirm("Fa'gi lee");

        yorubaI18n.getConfirm().getDelete().setTitle("Amudaju ipare");
        yorubaI18n.getConfirm().getDelete().setContent("Se o da o l'oju pe o fe pa eeyan yi re? Igbese yi o l'ayipada o.");
        yorubaI18n.getConfirm().getDelete().getButton().setDismiss("Da'wo duro");
        yorubaI18n.getConfirm().getDelete().getButton().setConfirm("Paare");

        return yorubaI18n;
    }
    // end-source-example

    private void customToolbar() {
        // begin-source-example
        // source-example-heading: CRUD with custom toolbar
        Crud<Person> crud = new Crud<>(Person.class, createPersonEditor());

        Span footer = new Span();
        footer.getElement().getStyle().set("flex", "1");

        Button newItemButton = new Button("Add person ...");
        // Required attribute to handle the new item action
        newItemButton.getElement().setAttribute("new-button", true);

        crud.setToolbar(footer, newItemButton);

        PersonDataProvider dataProvider = new PersonDataProvider();
        dataProvider.setSizeChangeListener(count -> footer.setText("Total: " + count));

        crud.getGrid().removeColumnByKey("id");
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));
        // end-source-example

        addCard("CRUD with custom toolbar", crud);
    }

    private void customGrid() {
        // begin-source-example
        // source-example-heading: CRUD with custom Grid
        Grid<Person> grid = new Grid<>();
        Crud<Person> crud = new Crud<>(Person.class, grid, createPersonEditor());

        PersonDataProvider dataProvider = new PersonDataProvider();
        crud.setDataProvider(dataProvider);
        crud.addSaveListener(e -> dataProvider.persist(e.getItem()));
        crud.addDeleteListener(e -> dataProvider.delete(e.getItem()));

        Crud.addEditColumn(grid);
        grid.addColumn(TemplateRenderer.<Person>
                of("<img src=[[item.photoSource]] style=\"height: 40px; border-radius: 50%;\">")
                .withProperty("photoSource", this::randomProfilePictureUrl))
                .setWidth("60px")
                .setFlexGrow(0);
        grid.addColumn(Person::getFirstName).setHeader("First name");
        grid.addColumn(Person::getLastName).setHeader("Last name");
        // end-source-example

        addCard("CRUD with custom Grid", crud);
    }

    private void customSearch() {
        // begin-source-example
        // source-example-heading: CRUD with custom search
        Grid<Person> grid = new Grid<>(Person.class);
        Crud<Person> crud = new Crud<>(Person.class, grid, createPersonEditor());

        List<Person> database = createPersonList();

        Function<String, Stream<Person>> filter = query -> {
            Stream<Person> result = database.stream();

            if (!query.isEmpty()) {
                final String f = query.toLowerCase();
                result = result.filter(p ->
                        (p.getFirstName() != null) && (p.getFirstName().toLowerCase().contains(f))
                                || p.getLastName().toLowerCase().contains(f));
            }

            return result;
        };

        DataProvider<Person, String> dataProvider = new CallbackDataProvider<>(
                query -> filter.apply(query.getFilter().orElse("")),
                query -> (int) filter.apply(query.getFilter().orElse("")).count());

        ConfigurableFilterDataProvider<Person, Void, String> filterableDataProvider
                = dataProvider.withConfigurableFilter();

        grid.setDataProvider(filterableDataProvider);
        grid.removeColumnByKey("id");

        TextField searchBar = new TextField();
        searchBar.setPlaceholder("Search...");
        searchBar.setWidth("calc(100% - var(--lumo-space-s))");
        searchBar.setValueChangeMode(ValueChangeMode.EAGER);
        searchBar.setPrefixComponent(VaadinIcon.SEARCH.create());

        Icon closeIcon = new Icon("lumo", "cross");
        closeIcon.setVisible(false);
        ComponentUtil.addListener(closeIcon, ClickEvent.class,
                (ComponentEventListener) e -> searchBar.clear());
        searchBar.setSuffixComponent(closeIcon);

        ComponentUtil.addListener(searchBar, FilterChanged.class, e -> {
            closeIcon.setVisible(!searchBar.getValue().isEmpty());
            filterableDataProvider.setFilter(searchBar.getValue());
        });

        crud.setToolbar(searchBar);
        crud.getElement().getStyle().set("flex-direction", "column-reverse");
        // end-source-example

        addCard("CRUD with custom search", crud);
    }

    // NOTE: heading has three unicode spaces
    // begin-source-example
    // source-example-heading:    
    @DomEvent(value = "value-changed",
              debounce = @DebounceSettings(timeout = 300,
              phases = DebouncePhase.TRAILING))
    public static class FilterChanged extends ComponentEvent<TextField> {
        public FilterChanged(TextField source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    // end-source-example

    // Dummy database
    private static final String[] FIRSTS = {"James", "Mary", "John", "Patricia", "Robert", "Jennifer"};
    private static final String[] LASTS = {"Smith", "Johnson", "Williams", "Brown"};

    private static List<Person> createPersonList() {
        return IntStream
                .rangeClosed(1, 50)
                .mapToObj(i -> new Person(i, FIRSTS[i % FIRSTS.length], LASTS[i % LASTS.length]))
                .collect(toList());
    }

    private String randomProfilePictureUrl(Object context) {
        return "https://randomuser.me/api/portraits/thumb/"
                + (Math.random() > 0.5 ? "men" : "women")
                + "/"
                + (1 + (int) (Math.random() * 100))
                + ".jpg";
    }

    // begin-source-example
    // source-example-heading: Example Classes
    // Person Bean
    public static class Person implements Cloneable {
        private Integer id;
        private String firstName;
        private String lastName;

        /**
         * No-arg constructor required by Crud to be able to instantiate a new bean
         * when the new item button is clicked.
         */
        public Person() {
        }

        public Person(Integer id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

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

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Override
        public Person clone() {
            try {
                return (Person)super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

    // Person data provider
    public static class PersonDataProvider extends AbstractBackEndDataProvider<Person, CrudFilter> {

        // A real app should hook up something like JPA
        final List<Person> DATABASE = createPersonList();

        private Consumer<Long> sizeChangeListener;

        @Override
        protected Stream<Person> fetchFromBackEnd(Query<Person, CrudFilter> query) {
            int offset = query.getOffset();
            int limit = query.getLimit();

            Stream<Person> stream = DATABASE.stream();

            if (query.getFilter().isPresent()) {
                stream = stream
                        .filter(predicate(query.getFilter().get()))
                        .sorted(comparator(query.getFilter().get()));
            }

            return stream.skip(offset).limit(limit);
        }

        @Override
        protected int sizeInBackEnd(Query<Person, CrudFilter> query) {
            // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
            long count = fetchFromBackEnd(query).count();

            if (sizeChangeListener != null) {
                sizeChangeListener.accept(count);
            }

            return (int) count;
        }

        void setSizeChangeListener(Consumer<Long> listener) {
            sizeChangeListener = listener;
        }

        private Predicate<Person> predicate(CrudFilter filter) {
            // For RDBMS just generate a WHERE clause
            return filter.getConstraints().entrySet().stream()
                    .map(constraint -> (Predicate<Person>) person -> {
                        try {
                            Object value = valueOf(constraint.getKey(), person);
                            return value != null && value.toString().toLowerCase()
                                    .contains(constraint.getValue().toLowerCase());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .reduce(Predicate::and)
                    .orElse(e -> true);
        }

        private Comparator<Person> comparator(CrudFilter filter) {
            // For RDBMS just generate an ORDER BY clause
            return filter.getSortOrders().entrySet().stream()
                    .map(sortClause -> {
                        try {
                            Comparator<Person> comparator
                                    = Comparator.comparing(person ->
                                    (Comparable) valueOf(sortClause.getKey(), person));

                            if (sortClause.getValue() == SortDirection.DESCENDING) {
                                comparator = comparator.reversed();
                            }

                            return comparator;
                        } catch (Exception ex) {
                            return (Comparator<Person>) (o1, o2) -> 0;
                        }
                    })
                    .reduce(Comparator::thenComparing)
                    .orElse((o1, o2) -> 0);
        }

        private Object valueOf(String fieldName, Person person) {
            try {
                Field field = Person.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(person);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        void persist(Person item) {
            if (item.getId() == null) {
                item.setId(DATABASE
                        .stream()
                        .map(Person::getId)
                        .max(naturalOrder())
                        .orElse(0) + 1);
            }

            final Optional<Person> existingItem = find(item.getId());
            if (existingItem.isPresent()) {
                int position = DATABASE.indexOf(existingItem.get());
                DATABASE.remove(existingItem.get());
                DATABASE.add(position, item);
            } else {
                DATABASE.add(item);
            }
        }

        Optional<Person> find(Integer id) {
            return DATABASE
                    .stream()
                    .filter(entity -> entity.getId().equals(id))
                    .findFirst();
        }

        void delete(Person item) {
            DATABASE.removeIf(entity -> entity.getId().equals(item.getId()));
        }
    }

    // end-source-example
}

# Vaadin Crud for Flow

Vaadin Crud for Flow is a UI component add-on for Vaadin which provides CRUD UI for any data backend.

## License & Author

This Add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3). For license terms, see LICENSE.txt.

Vaadin Crud is written by Vaadin Ltd.

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add Crud to your project
```xml
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-crud-flow</artifactId>
    <version>1.0.0.alpha3</version>
  </dependency>
</dependencies>
```

### Using Vaadin Crud

#### Basic use
In the most basic use case, Vaadin Crud requires the class of items to be processed
and an editor for the class.

```java
Crud<Person> crud = new Crud<>(Person.class, personEditor);
crud.setDataProvider(personDataProvider);

// Handle save and delete events.
crud.addSaveListener(e -> save(e.getItem()));
crud.addDeleteListener(e -> delete(e.getItem()));

// Set a footer text or component if desired.
crud.setFooter("Flight manifest for XX210");
```

[<img src="https://raw.githubusercontent.com/vaadin/vaadin-crud/master/screenshot.gif" width="700" alt="Screenshot of vaadin-crud">](https://vaadin.com/components/vaadin-crud)

#### Creating an editor
An editor can be created by implementing the `CrudEditor` interface. The editor's purpose
is to manage the currently edited item and present a UI (e.g a form) for manipulating it.

```java
public class PersonCrudEditor implements CrudEditor<Person> {

    private TextField firstName = new TextField("First name");
    private TextField lastName = new TextField("Last name");

    private FormLayout view = new FormLayout(firstName, lastName);

    private Binder<Person> binder;

    @Override
    public Person getItem() {
        return item;
    }

    @Override
    public void setItem(Person item) {
        // Setup bindings for the form fields. 
        // The binder can also be used for implementing validation.

        binder = new Binder<>(Person.class);
        binder.bind(firstName, Person::getFirstName, Person::setFirstName);
        binder.bind(lastName, Person::getLastName, Person::setLastName);

        // Bind the form to the item.
        binder.setBean(item);
    }

    @Override
    public void clear() {
        if (binder != null) {
            binder.removeBinding("firstName");
            binder.removeBinding("lastName");
            binder.removeBean();
            binder = null;
        }

        firstName.clear();
        lastName.clear();
    }

    @Override
    public boolean isValid() {
        return binder != null && binder.isValid();
    }

    @Override
    public Element getView() {
        return view.getElement();
    }
}
```

#### Disabling grid filters
Crud supports custom grids by accepting a `Grid` as a constructor parameter; 
however, a built-in `Grid` implementation called `CrudGrid` is provided.

When no `Grid` is supplied to the `Crud` constructor, it uses this `CrudGrid`. 
`CrudGrid` allows the search filters which are normally at the top of each column to be enabled or disabled. 
Setting the `enableDefaultFilters` constructor parameter to false disables it.

```java
CrudGrid<Person> grid = new CrudGrid<>(Person.class, false);
PersonEditor editor = new PersonEditor();

Crud<Person> crud = new Crud<>(Person.class, grid, editor);
```

#### Creating a data provider
`CrudGrid<Person>` for example expects a data provider of type `DataProvider<Person, CrudFilter>`.
The `CrudFilter` provides information about the filters and sort orders the user has applied to the grid.

A sample data provider can be seen [here](https://github.com/vaadin/vaadin-crud-flow/blob/master/vaadin-crud-flow-integration-tests/src/main/java/com/vaadin/flow/component/crud/examples/PersonCrudDataProvider.java).

#### Using a custom grid
As discussed above, Crud supports custom `Grid`s.
An important detail to pay attention to is that Crud listens to `edit` events from the grid to initiate editing
(i.e opening the editor and populating the form).

The easiest way to setup a grid to fire this event when an item is clicked is to use a built-in helper to add an
edit column to the grid.

```java
Grid<Person> myGrid = new Grid<>();
Crud.addEditColumn(myGrid);

// Add other columns to the grid as desired.

Crud<Person> crud = new Crud<>(Person.class, grid, editor);
```

## Setting up for development

Clone the project in GitHub (or fork it if you plan on contributing)

```
git clone git@github.com:vaadin/vaadin-crud-flow.git
```

To build and install the project into the local repository run 

```mvn install -DskipITs```

in the root directory. `-DskipITs` will skip the integration tests, which require a TestBench license. If you want to run all tests as part of the build, run

```mvn install```
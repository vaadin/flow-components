package com.vaadin.flow.component.grid.it;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Gender;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.bean.Person;

@Route("vaadin-grid/grid-filtering")
public class GridFilteringPage extends Div {

    private static final Set<String> DATA = getData();

    public GridFilteringPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("data-grid");

        DataProvider<String, String> dataProvider = new CallbackDataProvider<>(
                query -> findAnyMatching(query.getFilter()).stream(),
                query -> countAnyMatching(query.getFilter()));

        ConfigurableFilterDataProvider<String, Void, String> filteredDataProvider = dataProvider
                .withConfigurableFilter();

        grid.setDataProvider(filteredDataProvider);

        grid.addColumn(item -> item).setHeader("Data");

        TextField field = new TextField();
        field.setId("filter");
        field.addValueChangeListener(
                event -> filteredDataProvider.setFilter(field.getValue()));

        add(field, grid);
        createLazyLoadedGridWithFilterAndHidingColumn();
    }

    private void createLazyLoadedGridWithFilterAndHidingColumn() {
        Grid<Person> grid = new Grid<>();
        grid.setId("simple-grid-filtering");
        grid.addColumn(Person::getFirstName).setKey("firstName");
        grid.addColumn(Person::getAge).setKey("age");

        ArrayList<Person> items = new ArrayList<>();

        for (int i = 1; i < 400; i++) {
            items.add(new Person("Person " + i, "Last " + i, "site@site.com",
                    i + 1, Gender.MALE, null));
        }
        ListDataProvider<Person> dataProvider = new ListDataProvider<>(items);

        grid.setDataProvider(dataProvider);

        Button filterGridAndHideColumn = new Button("Filter");
        filterGridAndHideColumn.setId("filter-grid-and-hide-column");
        filterGridAndHideColumn.addClickListener(buttonClickEvent -> {
            setVisibleGrid(grid, false);
            addFilter(dataProvider, "Person 4");
        });

        Button clearFilterAndShowColumn = new Button("Clear");
        clearFilterAndShowColumn.setId("clear-filter-and-show-column");
        clearFilterAndShowColumn.addClickListener(event -> {
            setVisibleGrid(grid, true);
            addFilter(dataProvider, "");
        });

        add(filterGridAndHideColumn, clearFilterAndShowColumn, grid);
    }

    private void addFilter(ListDataProvider<Person> dataProvider,
            String value) {
        if (value.equals("")) {
            dataProvider.clearFilters();
        } else {
            dataProvider.addFilterByValue(Person::getFirstName, value);
        }
    }

    private void setVisibleGrid(Grid<Person> grid, boolean visible) {
        grid.getColumnByKey("age").setVisible(visible);
    }

    private Collection<String> findAnyMatching(Optional<String> filter) {
        if (filter.isPresent()) {
            return filter(filter).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private int countAnyMatching(Optional<String> filter) {
        if (filter.isPresent()) {
            return (int) filter(filter).count();
        }
        return 0;
    }

    private Stream<String> filter(Optional<String> filter) {
        return DATA.stream().filter(item -> item.contains(filter.get()));
    }

    private static final Set<String> getData() {
        Set<String> set = new LinkedHashSet<>();
        set.add("foo");
        set.add("bar");
        set.add("baz");
        return set;
    }
}

package com.vaadin.flow.component.grid.it;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;

@Route("vaadin-grid/grid-filtering")
public class GridFilteringPage extends Div {

    private static final Set<String> DATA = getData();

    static final String LAZY_FILTERABLE_GRID_ID = "lazy-filterable-grid-id";
    static final String GRID_FILTER_ID = "grid-filter-id";

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

        createLazyLoadingAndFilterableGrid();
        createCheckBoxFilterGrid();
    }

    private void createLazyLoadingAndFilterableGrid() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(ValueProvider.identity()).setHeader("Items");

        final List<String> items = IntStream.range(0, 1000)
                .mapToObj(item -> "Item " + item).collect(Collectors.toList());

        TextField filterField = new TextField("Search Item");
        filterField.setId(GRID_FILTER_ID);
        filterField.addValueChangeListener(
                event -> grid.getGenericDataView().refreshAll());

        grid.setItems(query -> {
            String searchTerm = filterField.getValue();
            return items.stream().filter(
                    item -> searchTerm.isEmpty() || item.contains(searchTerm))
                    .skip(Math.min(items.size(), query.getOffset()))
                    .limit(query.getLimit());
        });

        grid.setId(LAZY_FILTERABLE_GRID_ID);

        add(filterField, grid);
    }

    private void createCheckBoxFilterGrid() {

        Span message = new Span();
        message.setId("notification-message");

        Grid<String> grid = new Grid<>();
        grid.setDataProvider(new ListDataProvider<>(Arrays.asList("a", "b", "c",
                "d", "e", "f", "g", "h", "i", "j", "k")));
        grid.addColumn(x -> x).setHeader("Header");
        ComboBox<String> filter = new ComboBox<>();
        filter.addValueChangeListener(evt -> {
            ListDataProvider<String> dataProvider = (ListDataProvider<String>) grid
                    .getDataProvider();
            dataProvider.clearFilters();
            if (filter.getValue() != null) {
                dataProvider.addFilter(item -> StringUtils
                        .containsIgnoreCase(item, filter.getValue()));
            }
        });
        filter.setClearButtonVisible(true);
        filter.setItems(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h",
                "i", "j", "k"));
        grid.prependHeaderRow().getCells().get(0).setComponent(filter);
        grid.addItemClickListener(ev -> message
                .setText("column=" + ev.getColumn() + " item=" + ev.getItem()));
        add(grid, message);
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

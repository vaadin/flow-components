/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import static com.vaadin.flow.component.crud.tests.Helper.createPersonEditor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DebounceSettings;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.DebouncePhase;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-crud/customsearch")
public class CustomSearchView extends VerticalLayout {

    final private List<Person> data = PersonCrudDataProvider
            .generatePersonsList();

    public CustomSearchView() {
        final Grid<Person> grid = new Grid<>(Person.class);
        final Crud<Person> crud = new Crud<>(Person.class, grid,
                createPersonEditor());

        DataProvider<Person, String> dataProvider = new CallbackDataProvider<>(
                query -> findAnyMatching(query.getFilter()),
                query -> countAnyMatching(query.getFilter()));

        ConfigurableFilterDataProvider<Person, Void, String> filterableDataProvider = dataProvider
                .withConfigurableFilter();

        grid.setDataProvider(filterableDataProvider);
        crud.addNewListener(e -> data.add(e.getItem()));

        final TextField searchBar = new TextField();
        searchBar.setId("searchBar");
        searchBar.getElement().getStyle().set("flex-grow", "1");
        searchBar.setValueChangeMode(ValueChangeMode.EAGER);
        searchBar.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchBar.setPlaceholder("Search...");
        ComponentUtil.addListener(searchBar, FilterChanged.class,
                e -> filterableDataProvider.setFilter(searchBar.getValue()));

        crud.setToolbar(searchBar);

        Anchor newItemLink = new Anchor("javascript:", "New person");
        crud.setNewButton(newItemLink);

        crud.getElement().getStyle().set("flex-direction", "column-reverse");

        setHeight("100%");
        add(crud);
    }

    @DomEvent(value = "value-changed", debounce = @DebounceSettings(timeout = 300, phases = DebouncePhase.TRAILING))
    public static class FilterChanged extends ComponentEvent<TextField> {

        public FilterChanged(TextField source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    private Stream<Person> findAnyMatching(Optional<String> filter) {
        if (filter.isPresent() && filter.get() != null
                && !filter.get().isEmpty()) {
            return filter(filter);
        }
        return data.stream();
    }

    private int countAnyMatching(Optional<String> filter) {
        if (filter.isPresent() && filter.get() != null
                && !filter.get().isEmpty()) {
            return (int) filter(filter).count();
        }
        return data.size();
    }

    private Stream<Person> filter(Optional<String> filter) {
        final String f = filter.orElse("").toLowerCase();
        return data.stream()
                .filter(p -> (p.getFirstName() != null)
                        && (p.getFirstName().toLowerCase().contains(f))
                        || (p.getLastName() != null)
                                && (p.getLastName().toLowerCase().contains(f))
                        || String.valueOf(p.getId()).contains(f));
    }
}

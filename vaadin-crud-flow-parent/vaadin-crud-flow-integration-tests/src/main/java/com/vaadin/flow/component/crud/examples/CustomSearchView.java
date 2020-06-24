package com.vaadin.flow.component.crud.examples;

import static com.vaadin.flow.component.crud.examples.Helper.createPersonEditor;

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
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route
@Theme(Lumo.class)
public class CustomSearchView extends VerticalLayout {

    final private List<Person> data = PersonCrudDataProvider.generatePersonsList();

    public CustomSearchView() {
        final Grid<Person> grid = new Grid<>(Person.class);
        final Crud<Person> crud = new Crud<>(Person.class, grid, createPersonEditor());

        DataProvider<Person, String> dataProvider = new CallbackDataProvider<>(
            query -> findAnyMatching(query.getFilter()),
            query -> countAnyMatching(query.getFilter()));

        ConfigurableFilterDataProvider<Person, Void, String> filterableDataProvider
                = dataProvider.withConfigurableFilter();

        grid.setDataProvider(filterableDataProvider);
        crud.addNewListener(e -> data.add(e.getItem()));

        final TextField searchBar = new TextField();
        searchBar.getElement().getStyle().set("flex-grow", "1");
        searchBar.setValueChangeMode(ValueChangeMode.EAGER);
        searchBar.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchBar.setPlaceholder("Search...");
        ComponentUtil.addListener(searchBar, FilterChanged.class,
                e -> filterableDataProvider.setFilter(searchBar.getValue()));

        Anchor newItemLink = new Anchor("javascript:", "New person");
        newItemLink.getElement().setAttribute("new-button", true);
        crud.setToolbar(searchBar, newItemLink);

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
        if (filter.isPresent() && filter.get() != null && !filter.get().isEmpty()) {
            return filter(filter);
        }
        return data.stream();
    }

    private int countAnyMatching(Optional<String> filter) {
        if (filter.isPresent() && filter.get() != null && !filter.get().isEmpty()) {
            return (int) filter(filter).count();
        }
        return data.size();
    }

    private Stream<Person> filter(Optional<String> filter) {
        final String f = filter.get().toLowerCase();
        return data.stream().filter(p ->
            (p.getFirstName() != null) && (p.getFirstName().toLowerCase().contains(f))
                || (p.getLastName() != null) && (p.getLastName().toLowerCase().contains(f))
                || String.valueOf(p.getId()).contains(f)
        );
    }
}

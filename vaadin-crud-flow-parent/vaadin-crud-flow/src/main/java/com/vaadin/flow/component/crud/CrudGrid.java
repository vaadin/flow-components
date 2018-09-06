package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
 * %%
 * Copyright (C) 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

public class CrudGrid<E> extends Grid<E> {

    private final Class<E> beanType;
    private final boolean enableDefaultFilters;
    private final CrudFilter filter = new CrudFilter();

    public CrudGrid(Class<E> beanType, boolean enableDefaultFilters) {
        super(beanType);

        this.beanType = beanType;
        this.enableDefaultFilters = enableDefaultFilters;

        setup();
    }

    private void setup() {
        this.setSelectionMode(SelectionMode.NONE);
        setupSorting();
        if (enableDefaultFilters) {
            setupFiltering();
        }
    }

    private void setupFiltering() {
        final HeaderRow filterRow = this.appendHeaderRow();
        this.getColumns().forEach(column -> {
            final TextField field = new TextField();

            field.addValueChangeListener(event -> {
                filter.getConstraints().remove(column.getKey());

                if (!field.isEmpty()) {
                    filter.getConstraints().put(column.getKey(), event.getValue());
                }

                getConfigurableDataProvider().refreshAll();
            });

            field.setValueChangeMode(ValueChangeMode.EAGER);

            filterRow.getCell(column).setComponent(field);
            field.setSizeFull();
            field.setPlaceholder("Filter");
        });
    }

    private void setupSorting() {
        setMultiSort(true);
        this.addSortListener(event -> {
            filter.getSortOrders().clear();
            event.getSortOrder().forEach(e ->
                    filter.getSortOrders().put(e.getSorted().getKey(), e.getDirection()));
            getConfigurableDataProvider().refreshAll();
        });
    }

    private ConfigurableFilterDataProvider<E, Void, CrudFilter> getConfigurableDataProvider() {
        return (ConfigurableFilterDataProvider<E, Void, CrudFilter>) getDataProvider();
    }

    /**
     * Sets a DataProvider&lt;E, CrudFilter&gt;
     *
     * @param dataProvider a {@link DataProvider}
     * @see CrudFilter
     */
    @Override
    public void setDataProvider(DataProvider<E, ?> dataProvider) {
        // Attempt a cast to ensure that the captured ? is actually a CrudFilter
        // Unfortunately this cannot be enforced by the compiler
        try {
            ConfigurableFilterDataProvider<E, Void, CrudFilter> provider
                    = ((DataProvider<E, CrudFilter>) dataProvider).withConfigurableFilter();

            provider.setFilter(filter);

            super.setDataProvider(provider);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("DataProvider<" + beanType.getSimpleName()
                    + ", CrudFilter> expected", ex);
        }
    }
}

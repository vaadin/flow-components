package com.vaadin.flow.component.grid.it;

import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-single-selection-update-and-deselect")
public class GridSingleSelectionUpdateAndDeselectPage extends Div {

    private Bean bean = new Bean(0, "Foo");

    private Bean getBeanClone() {
        return new Bean(bean.getId(), bean.getName());
    }

    public GridSingleSelectionUpdateAndDeselectPage() {
        Grid<Bean> grid = new Grid<>(Bean.class);

        CallbackDataProvider<Bean, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    query.getLimit();
                    query.getOffset();
                    return Stream.of(getBeanClone());
                }, query -> {
                    query.getLimit();
                    query.getOffset();
                    Stream<Bean> stream = Stream.of(getBeanClone());
                    return (int) stream.count();
                });
        grid.setDataProvider(dataProvider);

        grid.select(getBeanClone());

        Button button = new Button("Update name", e -> {
            bean.setName("Bar");

            // The order of these two calls is important. Do not change.
            // See https://github.com/vaadin/flow-components/issues/3229
            grid.getDataProvider().refreshAll();
            grid.select(null);
        });
        button.setId("update-name");

        add(grid, button);
    }

    public static class Bean {
        private final int id;
        private String name;

        public Bean(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Bean) {
                Bean other = (Bean) obj;
                return id == other.id;
            }
            return false;
        }
    }
}

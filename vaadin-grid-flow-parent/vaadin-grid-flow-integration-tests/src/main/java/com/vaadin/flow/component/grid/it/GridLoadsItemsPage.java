package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

@Route("grid-loads-items")
public class GridLoadsItemsPage extends Div {
    public GridLoadsItemsPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("data-grid");

        VerticalLayout messages = new VerticalLayout();
        messages.setId("messages");

        DataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    String message = "Fetch " + offset + " - "
                            + (offset + limit);
                    messages.add(new Span(message));

                    return IntStream.range(0, 1000).skip(offset).limit(limit)
                            .mapToObj(Integer::toString);
                }, query -> 1000);

        grid.setDataProvider(dataProvider);

        grid.addColumn(item -> item).setHeader("Data");

        NativeButton clearButton = new NativeButton("Clear message",
                e -> messages.removeAll());
        clearButton.setId("clear-messages");

        add(grid, clearButton, messages);
    }
}

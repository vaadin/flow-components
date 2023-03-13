package com.vaadin.flow.component.combobox.test;

import java.util.Arrays;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

/**
 * vaadin/vaadin-combo-box-flow#296 - Filtering problem using slow DataProvider
 */
@Route("vaadin-combo-box/filter-debounce")
public class FilterDebouncePage extends VerticalLayout {

    public FilterDebouncePage() {
        ComboBox<String> combo = new ComboBox<>();
        ListDataProvider<String> dp = new ListDataProvider<>(
                Arrays.asList("aaa", "bbb", "ccc"));
        combo.setItems((query) -> {
            waitABit();
            return dp.fetch(new Query<>(query.getOffset(), query.getLimit(),
                    null, null, filter(query.getFilter().get())));
        }, query -> {
            waitABit();
            return dp.size(new Query<>(0, Integer.MAX_VALUE, null, null,
                    filter(query.getFilter().get())));
        });
        combo.setAutofocus(true);
        Input tf = new Input();
        tf.setId("external-input");
        add(combo, tf);
    }

    private SerializablePredicate<String> filter(String filter) {
        return str -> str.contains(filter.toLowerCase());
    }

    private void waitABit() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Wait more
            waitABit();
        }
    }
}

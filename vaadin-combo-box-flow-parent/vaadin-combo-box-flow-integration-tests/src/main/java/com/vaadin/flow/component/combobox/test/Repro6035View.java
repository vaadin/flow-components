package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/6035
 *
 * Based on the maintainer (vursen) reproduction in the issue. Both scenarios
 * need a lazy, server-side-filtered ComboBox with auto-open disabled, and an
 * artificial server latency so the tab-out race (scenario 1) has a window.
 */
@Route("repro-6035")
public class Repro6035View extends Div {

    // 100 items incl. "Finland" so the ComboBox must filter on the server and
    // the "finland" -> "Finland" symptom from the report is observable.
    private static final List<String> ITEMS = Stream
            .concat(Stream.of("Finland"),
                    IntStream.range(0, 99).mapToObj(i -> "Item " + i))
            .sorted().toList();

    public Repro6035View() {
        // Scenario 1: type "finland" then quickly press Tab. The value may
        // reset, or the input text stays "finland" instead of "Finland".
        ComboBox<String> combo1 = new ComboBox<>("Default");
        combo1.setId("combo1");
        combo1.setAutoOpen(false);
        combo1.setItems(this::fetchItems, this::countItems);

        Div valueLabel = new Div("Value: null");
        valueLabel.setId("value-label");
        combo1.addValueChangeListener(
                e -> valueLabel.setText("Value: " + e.getValue()));

        // Scenario 2: before opening, type "asd", delete it, then open via the
        // toggle button -> overlay stuck on a loading spinner.
        ComboBox<String> combo2 = new ComboBox<>("Auto-open disabled");
        combo2.setId("combo2");
        combo2.setAutoOpen(false);
        combo2.setItems(this::fetchItems, this::countItems);

        add(new H3("Scenario 1: type \"finland\" and quickly press Tab"),
                combo1, valueLabel,
                new H3("Scenario 2: type \"asd\", delete it, then open"), combo2);
    }

    private Stream<String> fetchItems(Query<String, String> query) {
        simulateLatency();
        String filter = query.getFilter().orElse("").toLowerCase(Locale.ENGLISH);
        return ITEMS.stream()
                .filter(item -> item.toLowerCase(Locale.ENGLISH).contains(filter))
                .skip(query.getOffset()).limit(query.getLimit());
    }

    private int countItems(Query<String, String> query) {
        String filter = query.getFilter().orElse("").toLowerCase(Locale.ENGLISH);
        return (int) ITEMS.stream()
                .filter(item -> item.toLowerCase(Locale.ENGLISH).contains(filter))
                .count();
    }

    private void simulateLatency() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

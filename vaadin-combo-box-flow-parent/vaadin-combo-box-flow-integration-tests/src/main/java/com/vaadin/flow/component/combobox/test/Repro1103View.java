package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/1103
 *
 * Calling {@code setOpened(true)} while the ComboBox is being attached (in the
 * constructor) should open the overlay. The bug: the overlay stays closed on a
 * direct page load / refresh, though it opens on client-side navigation.
 *
 * Steps: open {@code /repro-1103} directly (or refresh) and observe whether the
 * overlay is open.
 */
@Route("repro-1103")
public class Repro1103View extends HorizontalLayout {

    public Repro1103View() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setId("combo");
        combo.setItems("Option one", "Option two");
        combo.setOpened(true);
        add(combo);
    }
}

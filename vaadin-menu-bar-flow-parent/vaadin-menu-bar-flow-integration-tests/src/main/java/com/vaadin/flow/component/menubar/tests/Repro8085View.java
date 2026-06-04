package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/8085
 *
 * A checkable MenuItem with addClickShortcut(): clicking with the mouse toggles
 * the checked state and fires the listener; the shortcut is reported to fire the
 * listener WITHOUT toggling, so isChecked() never flips.
 *
 * Checkable items are only allowed inside a sub-menu (root items can not be
 * checkable), so the checkable item lives under a "Menu" parent.
 */
@Route("repro-8085")
public class Repro8085View extends Div {

    public Repro8085View() {
        Div label = new Div("checked = (initial)");
        label.setId("label");

        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu-bar");

        MenuItem root = menuBar.addItem("Menu");
        MenuItem checkable = root.getSubMenu().addItem("Toggle",
                e -> label.setText("checked = " + e.getSource().isChecked()));
        checkable.setCheckable(true);
        checkable.addClickShortcut(Key.KEY_T, KeyModifier.CONTROL,
                KeyModifier.SHIFT, KeyModifier.ALT);

        add(menuBar, label);
    }
}

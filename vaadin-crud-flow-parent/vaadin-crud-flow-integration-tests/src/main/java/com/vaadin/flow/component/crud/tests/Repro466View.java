/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/466 — the
 * Crud auto-generated filter row disappears after calling
 * crud.getGrid().setColumns(...) to reorder/select columns. Minimal pair: a
 * default Crud (filters expected) next to one reconfigured with setColumns.
 * Signal: the setColumns grid has zero filter fields (crud-role="Search").
 */
@Route("repro-466")
public class Repro466View extends Div {

    public Repro466View() {
        // Control: default Crud, filter row should be present.
        Crud<Person> defaultCrud = new Crud<>(Person.class,
                Helper.createPersonEditor());
        defaultCrud.setDataProvider(new PersonCrudDataProvider());
        Div control = new Div(new H4("Default (control)"), defaultCrud);
        control.setId("control");

        // Bug case: reorder/select columns via setColumns after construction.
        Crud<Person> setColumnsCrud = new Crud<>(Person.class,
                Helper.createPersonEditor());
        setColumnsCrud.getGrid().setColumns("firstName", "lastName");
        setColumnsCrud.setDataProvider(new PersonCrudDataProvider());
        Div withSetColumns = new Div(new H4("After setColumns(...)"),
                setColumnsCrud);
        withSetColumns.setId("with-set-columns");

        add(control, withSetColumns);
    }
}

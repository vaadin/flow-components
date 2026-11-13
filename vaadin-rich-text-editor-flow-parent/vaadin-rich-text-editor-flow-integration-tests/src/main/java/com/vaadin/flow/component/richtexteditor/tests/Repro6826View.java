/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("repro-6826")
public class Repro6826View extends Div {

    static final String DELTA = "[{\"insert\":\"Hello delta\\n\"}]";
    static final String HTML = "<p>Hello html</p>";

    record User(String name) {
    }

    private final List<RichTextEditor> editors = new ArrayList<>();

    public Repro6826View() {
        var log = new Pre();
        log.setId("log");

        // A: reporter case - Grid details, asDelta().setValue in renderer
        var gridDelta = new Grid<User>();
        gridDelta.setId("grid-delta");
        gridDelta.addColumn(User::name).setHeader("A: details asDelta");
        gridDelta.setItemDetailsRenderer(
                new ComponentRenderer<>(() -> track(new RichTextEditor()),
                        (rte, user) -> rte.asDelta().setValue(DELTA)));
        gridDelta.setItems(new User("Julia"), new User("Bretta"));
        gridDelta.setAllRowsVisible(true);

        // B: control - Grid details, setValue (HTML) in renderer
        var gridHtml = new Grid<User>();
        gridHtml.setId("grid-html");
        gridHtml.addColumn(User::name).setHeader("B: details HTML");
        gridHtml.setItemDetailsRenderer(
                new ComponentRenderer<>(() -> track(new RichTextEditor()),
                        (rte, user) -> rte.setValue(HTML)));
        gridHtml.setItems(new User("Julia"), new User("Bretta"));
        gridHtml.setAllRowsVisible(true);

        // C: no grid - asDelta().setValue before attach
        var container = new Div();
        container.setId("container");
        var attachPreset = new NativeButton("C: set delta, then attach", e -> {
            var rte = track(new RichTextEditor());
            rte.setId("rte-preset");
            rte.asDelta().setValue(DELTA);
            container.add(rte);
        });
        attachPreset.setId("attach-preset");

        // D: control - attach first, then asDelta().setValue
        var attachThenSet = new NativeButton("D: attach, then set delta", e -> {
            var rte = track(new RichTextEditor());
            rte.setId("rte-postset");
            container.add(rte);
            rte.asDelta().setValue(DELTA);
        });
        attachThenSet.setId("attach-postset");

        var read = new NativeButton("Read server values", e -> {
            var sb = new StringBuilder();
            for (int i = 0; i < editors.size(); i++) {
                var rte = editors.get(i);
                sb.append(i).append(" attached=").append(rte.isAttached())
                        .append(" html=").append(rte.getValue())
                        .append(" delta=")
                        .append(rte.getElement().getProperty("value"))
                        .append('\n');
            }
            log.setText(sb.toString());
        });
        read.setId("read-values");

        add(new Div(attachPreset, attachThenSet, read), gridDelta, gridHtml,
                container, log);
    }

    private RichTextEditor track(RichTextEditor rte) {
        editors.add(rte);
        return rte;
    }
}

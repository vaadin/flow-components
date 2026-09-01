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
import java.util.function.Consumer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1894
 *
 * Each case is an independent editor with its own log. Clicking the case button
 * sets the value from the server. The log lists every value change event as
 * {@code #<n> value=<value> fromClient=<true|false>}.
 *
 * Expectation: a server-side setValue produces exactly one event with
 * fromClient=false.
 */
@Route("repro-1894")
public class Repro1894View extends Div {

    public Repro1894View() {
        // Control: well-formed HTML, the web component normalizes to the same
        // markup.
        addCase("plain-html", "setValue(\"<p>Test</p>\")",
                rte -> rte.setValue("<p>Test</p>"));

        // Reported variant: bare text, the web component normalizes it to
        // <p>Test</p> and reports the normalized value back.
        addCase("bare-text", "setValue(\"Test\")", rte -> rte.setValue("Test"));

        // Original issue snippet, via the asHtml() wrapper.
        addCase("ashtml-plain", "asHtml().setValue(\"<p>Test</p>\")",
                rte -> rte.asHtml().setValue("<p>Test</p>"));

        // 2024 comment variant, via the asHtml() wrapper.
        addCase("ashtml-bare", "asHtml().setValue(\"Test\")",
                rte -> rte.asHtml().setValue("Test"));

        // Other markup the web component is likely to rewrite.
        addCase("div-wrapped", "setValue(\"<div>Test</div>\")",
                rte -> rte.setValue("<div>Test</div>"));

        addCase("bold-only", "setValue(\"<b>Test</b>\")",
                rte -> rte.setValue("<b>Test</b>"));

        // Same cases with the default ON_CHANGE value change mode, to check
        // whether the reporter's EAGER mode is part of the trigger.
        addCase("onchange-plain", "ON_CHANGE + setValue(\"<p>Test</p>\")",
                ValueChangeMode.ON_CHANGE, rte -> rte.setValue("<p>Test</p>"));

        addCase("onchange-bold", "ON_CHANGE + setValue(\"<b>Test</b>\")",
                ValueChangeMode.ON_CHANGE, rte -> rte.setValue("<b>Test</b>"));

        // No explicit mode change at all: the component default, untouched.
        addCase("untouched-bold", "default mode + setValue(\"<b>Test</b>\")",
                null, rte -> rte.setValue("<b>Test</b>"));

        // The other modes that sync the html-value-changed event directly.
        addCase("lazy-bold", "LAZY + setValue(\"<b>Test</b>\")",
                ValueChangeMode.LAZY, rte -> rte.setValue("<b>Test</b>"));

        addCase("timeout-bold", "TIMEOUT + setValue(\"<b>Test</b>\")",
                ValueChangeMode.TIMEOUT, rte -> rte.setValue("<b>Test</b>"));

        addCase("onblur-bold", "ON_BLUR + setValue(\"<b>Test</b>\")",
                ValueChangeMode.ON_BLUR, rte -> rte.setValue("<b>Test</b>"));
    }

    private void addCase(String id, String label,
            Consumer<RichTextEditor> action) {
        addCase(id, label, ValueChangeMode.EAGER, action);
    }

    private void addCase(String id, String label, ValueChangeMode mode,
            Consumer<RichTextEditor> action) {
        RichTextEditor editor = new RichTextEditor();
        if (mode != null) {
            editor.setValueChangeMode(mode);
        }
        editor.setId("editor-" + id);

        Span log = new Span();
        log.setId("log-" + id);

        List<String> events = new ArrayList<>();
        editor.addValueChangeListener(event -> {
            events.add(String.format("#%d value=%s fromClient=%s",
                    events.size() + 1, event.getValue(), event.isFromClient()));
            log.setText(String.join(" | ", events));
        });

        NativeButton setValue = new NativeButton(label,
                event -> action.accept(editor));
        setValue.setId("set-" + id);

        NativeButton clearLog = new NativeButton("clear log", event -> {
            events.clear();
            log.setText("");
        });
        clearLog.setId("clear-" + id);

        Div section = new Div();
        section.getStyle().set("border", "1px solid #ccc").set("margin", "8px")
                .set("padding", "8px");
        section.add(new Div(new Span(id + ": " + label)), editor,
                new Div(setValue, clearLog),
                new Div(new Span("events: "), log));
        add(section);
    }
}

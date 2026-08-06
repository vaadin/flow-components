/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.timepicker.tests;

import java.time.LocalTime;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/9842
 *
 * Value set before attach is not marked as checked in the dropdown.
 */
@Route("repro-9842")
public class Repro9842View extends Div {

    public Repro9842View() {
        // A: value set before attach -> reported as broken
        TimePicker preAttached = new TimePicker("A: value before attach");
        preAttached.setId("pre-attached");
        preAttached.setValue(LocalTime.of(10, 0));
        add(wrap(preAttached));

        // B: value set after attach, same round trip -> reported as broken too
        TimePicker sameRoundTrip = new TimePicker("B: value after add()");
        sameRoundTrip.setId("same-round-trip");
        add(wrap(sameRoundTrip));
        sameRoundTrip.setValue(LocalTime.of(10, 0));

        // C: value set in onAttach
        TimePicker inAttach = new TimePicker("C: value in attach listener");
        inAttach.setId("in-attach");
        inAttach.addAttachListener(
                event -> inAttach.setValue(LocalTime.of(10, 0)));
        add(wrap(inAttach));

        // D: value set after an extra client round trip -> reported workaround
        TimePicker afterRoundTrip = new TimePicker(
                "D: value after JS round trip");
        afterRoundTrip.setId("after-round-trip");
        add(wrap(afterRoundTrip));
        afterRoundTrip.getElement().executeJs("return")
                .then(ignored -> afterRoundTrip.setValue(LocalTime.of(10, 0)));

        // E: no value, set from a button click -> control
        TimePicker fromButton = new TimePicker("E: value from button");
        fromButton.setId("from-button");
        NativeButton setValue = new NativeButton("Set 10:00 on E",
                event -> fromButton.setValue(LocalTime.of(10, 0)));
        setValue.setId("set-value-e");
        add(wrap(fromButton), setValue);

        // F: pre-attached value, detach/attach cycle
        TimePicker detachAttach = new TimePicker("F: pre-attached + reattach");
        detachAttach.setId("detach-attach");
        detachAttach.setValue(LocalTime.of(10, 0));
        Div fContainer = wrap(detachAttach);
        NativeButton toggleF = new NativeButton("Detach/attach F", event -> {
            fContainer.remove(detachAttach);
            fContainer.add(detachAttach);
        });
        toggleF.setId("toggle-f");
        add(fContainer, toggleF);

        // G: German locale + value, both set before attach
        TimePicker localePre = new TimePicker("G: de + value before attach");
        localePre.setId("locale-pre");
        localePre.setLocale(Locale.GERMANY);
        localePre.setValue(LocalTime.of(10, 0));
        add(wrap(localePre));

        // H: same as G, value set first
        TimePicker localePreSwapped = new TimePicker(
                "H: value then de, before attach");
        localePreSwapped.setId("locale-pre-swapped");
        localePreSwapped.setValue(LocalTime.of(10, 0));
        localePreSwapped.setLocale(Locale.GERMANY);
        add(wrap(localePreSwapped));

        // I: German locale + value set after attach
        TimePicker localePost = new TimePicker("I: de + value after attach");
        localePost.setId("locale-post");
        add(wrap(localePost));
        localePost.setLocale(Locale.GERMANY);
        localePost.setValue(LocalTime.of(10, 0));

        // J: German locale, value set from a button click -> control
        TimePicker localeButton = new TimePicker("J: de, value from button");
        localeButton.setId("locale-button");
        localeButton.setLocale(Locale.GERMANY);
        NativeButton setValueJ = new NativeButton("Set 10:00 on J",
                event -> localeButton.setValue(LocalTime.of(10, 0)));
        setValueJ.setId("set-value-j");
        add(wrap(localeButton), setValueJ);

        // K: German locale before attach, value after client round trip
        TimePicker localeRoundTrip = new TimePicker(
                "K: de, value after JS round trip");
        localeRoundTrip.setId("locale-round-trip");
        localeRoundTrip.setLocale(Locale.GERMANY);
        add(wrap(localeRoundTrip));
        localeRoundTrip.getElement().executeJs("return")
                .then(ignored -> localeRoundTrip.setValue(LocalTime.of(10, 0)));
    }

    private Div wrap(TimePicker timePicker) {
        Div div = new Div(timePicker);
        div.getStyle().set("display", "inline-block").set("margin", "10px");
        return div;
    }
}

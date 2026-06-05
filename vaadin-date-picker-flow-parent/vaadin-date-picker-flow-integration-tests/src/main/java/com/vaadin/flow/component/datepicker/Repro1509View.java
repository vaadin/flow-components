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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1509 —
 * "German Date locale formatting wrong".
 *
 * The value 2015-07-06 (single-digit day and month) is shown with each locale.
 * The report says de_CH / de_DE render without leading zeros (e.g. 6.7.2015)
 * while the expected German format is zero-padded (06.07.2015); tr_TR was cited
 * as already padded (28.11.2014).
 */
@Route("repro-1509")
public class Repro1509View extends Div {

    public Repro1509View() {
        LocalDate date = LocalDate.of(2015, 7, 6); // 6 July 2015

        add(picker("de-CH", new Locale("de", "CH"), date));
        add(picker("de-DE", new Locale("de", "DE"), date));
        add(picker("tr-TR", new Locale("tr", "TR"), date));
    }

    private DatePicker picker(String id, Locale locale, LocalDate date) {
        DatePicker picker = new DatePicker();
        picker.setLabel(id);
        picker.setLocale(locale);
        picker.setValue(date);
        picker.setId(id);
        return picker;
    }
}

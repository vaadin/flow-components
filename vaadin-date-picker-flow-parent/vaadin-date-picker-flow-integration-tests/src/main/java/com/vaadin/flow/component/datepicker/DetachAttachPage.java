/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/detach-attach")
public class DetachAttachPage extends Div {

    public DetachAttachPage() {
        DatePicker datePicker = new DatePicker(
                LocalDate.of(1993, Month.JUNE, 13));

        datePicker.setLocale(Locale.FRANCE);
        datePicker.setI18n(TestI18N.FINNISH);

        NativeButton detach = new NativeButton("detach",
                e -> remove(datePicker));
        detach.setId("detach");
        NativeButton attach = new NativeButton("attach", e -> add(datePicker));
        attach.setId("attach");

        add(datePicker, detach, attach);
    }

}

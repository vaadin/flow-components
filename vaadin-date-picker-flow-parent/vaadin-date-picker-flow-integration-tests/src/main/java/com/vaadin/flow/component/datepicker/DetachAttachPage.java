/*
 * Copyright 2000-2022 Vaadin Ltd.
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

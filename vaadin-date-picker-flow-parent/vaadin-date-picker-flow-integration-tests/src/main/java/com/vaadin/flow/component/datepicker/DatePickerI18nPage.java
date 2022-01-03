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
 *
 */

package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/date-picker-i18n")
public class DatePickerI18nPage extends VerticalLayout {

    public static final String ID_INITIAL_I18N_DATE_PICKER = "initial-i18n-date-picker";
    public static final String ID_DYNAMIC_I18N_DATE_PICKER = "dynamic-i18n-date-picker";
    public static final String ID_SET_FINNISH_BUTTON = "set-finnish-button";
    public static final String ID_SET_PARTIAL_I18N_BUTTON = "set-partial-i18n-button";

    public DatePickerI18nPage() {
        DatePicker initialI18nDatePicker = new DatePicker();
        initialI18nDatePicker.setId(ID_INITIAL_I18N_DATE_PICKER);
        initialI18nDatePicker.setI18n(TestI18N.FINNISH);

        DatePicker dynamicI18nDatePicker = new DatePicker();
        dynamicI18nDatePicker.setId(ID_DYNAMIC_I18N_DATE_PICKER);

        NativeButton setFinnishButton = new NativeButton("Set Finnish",
                e -> dynamicI18nDatePicker.setI18n(TestI18N.FINNISH));
        setFinnishButton.setId(ID_SET_FINNISH_BUTTON);

        NativeButton setPartialI18nButton = new NativeButton(
                "Set partial I18N config",
                e -> dynamicI18nDatePicker.setI18n(TestI18N.FINNISH_PARTIAL));
        setPartialI18nButton.setId(ID_SET_PARTIAL_I18N_BUTTON);

        add(new H1("Initial I18N"), initialI18nDatePicker);
        add(new H1("Dynamic I18N"), dynamicI18nDatePicker, setFinnishButton,
                setPartialI18nButton);
    }
}

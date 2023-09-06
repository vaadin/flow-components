/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.data.renderer.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/local-date-renderer")
public class LocalDateRendererPage extends Div {
    public LocalDateRendererPage() {
        ValueProvider<Integer, LocalDate> localDateProvider = (
                year) -> LocalDate.of(year, Month.JANUARY, 1);

        var renderer = new LocalDateRenderer<>(localDateProvider,
                () -> DateTimeFormatter.ISO_LOCAL_DATE);

        var component = renderer.createComponent(2023);
        component.setId("local-date");

        add(component);
    }
}

/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.data.renderer.tests;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
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

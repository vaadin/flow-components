/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.data.renderer.tests;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-renderer-flow/local-date-time-renderer")
public class LocalDateTimeRendererPage extends Div {
    public LocalDateTimeRendererPage() {
        ValueProvider<Integer, LocalDateTime> localDateTimeProvider = (
                year) -> LocalDateTime.of(year, Month.JANUARY, 1, 1, 1);

        var renderer = new LocalDateTimeRenderer<>(localDateTimeProvider,
                () -> DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        var component = renderer.createComponent(2023);
        component.setId("local-date-time");

        add(component);
    }
}

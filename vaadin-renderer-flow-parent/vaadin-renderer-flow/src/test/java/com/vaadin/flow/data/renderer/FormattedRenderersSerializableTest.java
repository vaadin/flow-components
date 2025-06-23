/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.data.renderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.junit.Test;

public class FormattedRenderersSerializableTest {

    @Test
    public void numberRendererIsSerializable() throws IOException {
        NumberRenderer<?> renderer = new NumberRenderer<>(value -> 42);
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);

        renderer = new NumberRenderer<>(value -> 42,
                NumberFormat.getInstance());
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }

    @Test
    public void localDateTimeRendererIsSerializable() throws IOException {
        LocalDateTimeRenderer<?> renderer = new LocalDateTimeRenderer<>(
                value -> LocalDateTime.now());
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);

        renderer = new LocalDateTimeRenderer<>(value -> LocalDateTime.now(),
                () -> DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM));
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }

    @Test
    public void localDateRendererIsSerializable() throws IOException {
        LocalDateRenderer<?> renderer = new LocalDateRenderer<>(
                (v) -> LocalDate.now());
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);

        renderer = new LocalDateRenderer<>((v) -> LocalDate.now(),
                () -> DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM));
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }
}

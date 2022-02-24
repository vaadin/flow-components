package com.vaadin.flow.data.renderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.junit.Test;

public class FormattedRenderersSerializableTest {

    @Test
    public void numberRendererIsSerializable() throws IOException {
        final NumberRenderer renderer = new NumberRenderer(
                value -> value.toString(), NumberFormat.getInstance());
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }

    @Test
    public void localDateTimeRendererIsSerializable() throws IOException {
        final LocalDateTimeRenderer renderer = new LocalDateTimeRenderer(
                value -> value.toString(), () -> DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM));
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }

    @Test
    public void localDateRendererIsSerializable() throws IOException {
        final LocalDateRenderer renderer = new LocalDateRenderer(
                value -> value.toString(), () -> DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM));
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }
}

/*
 * Copyright 2000-2024 Vaadin Ltd.
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

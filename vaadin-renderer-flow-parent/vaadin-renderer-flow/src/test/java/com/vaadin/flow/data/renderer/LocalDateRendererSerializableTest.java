package com.vaadin.flow.data.renderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class LocalDateRendererSerializableTest extends ClassesSerializableTest {

    @Test
    public void localDateRendererIsSerializable() throws IOException {
        final LocalDateRenderer renderer = new LocalDateRenderer(
                value -> value.toString(), "");
        new ObjectOutputStream(new ByteArrayOutputStream())
                .writeObject(renderer);
    }
}

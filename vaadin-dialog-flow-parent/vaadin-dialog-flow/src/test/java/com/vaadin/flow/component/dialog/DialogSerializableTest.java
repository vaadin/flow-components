package com.vaadin.flow.component.dialog;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class DialogSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(),
                Stream.of("com\\.vaadin\\.base\\.devserver\\..*"));
    }
}

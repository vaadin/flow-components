package com.vaadin.flow.component.details;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class DetailsSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.contextmenu\\.osgi\\..*",
                "com\\.vaadin\\.base\\.devserver\\..*"));
    }
}

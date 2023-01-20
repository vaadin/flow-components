package com.vaadin.flow.component.dialog;

import com.vaadin.flow.testutil.ClassesSerializableTest;

import java.util.stream.Stream;

public class DialogSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.shared\\.InternalOverlayClassListProxy\\$IteratorProxy"));
    }
}

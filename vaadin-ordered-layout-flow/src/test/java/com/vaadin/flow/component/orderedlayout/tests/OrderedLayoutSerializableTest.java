package com.vaadin.flow.component.orderedlayout.tests;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class OrderedLayoutSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(
                super.getExcludedPatterns(),
                Stream.of("com\\.vaadin\\.flow\\.component\\.orderedlayout\\.it\\..*"));
    }
}

package com.vaadin.flow.component.grid;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class GridSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.grid\\.it\\..*",
                "com\\.vaadin\\.flow\\.component\\.contextmenu\\.osgi\\..*",
                "com\\.vaadin\\.flow\\.component\\.treegrid\\.it\\..*",
                "com\\.vaadin\\.flow\\.component\\.datepicker\\..*"
        ));
    }
}

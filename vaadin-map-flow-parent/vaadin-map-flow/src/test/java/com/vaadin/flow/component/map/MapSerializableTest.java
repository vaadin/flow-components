package com.vaadin.flow.component.map;

import com.vaadin.flow.testutil.ClassesSerializableTest;

import java.util.stream.Stream;

public class MapSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.map\\.Assets",
                "com\\.vaadin\\.flow\\.component\\.map\\.configuration\\.Coordinate\\$Converters",
                "com\\.vaadin\\.flow\\.component\\.map\\.configuration\\.Constants",
                "com\\.vaadin\\.flow\\.component\\.map\\.configuration\\..*\\$.*Options"));
    }
}

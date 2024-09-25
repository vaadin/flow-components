/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

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

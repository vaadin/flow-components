/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class ChartsSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                ".*\\Serializer(Modifier)?$",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializationDelegate",
                "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializerDelegator"));
    }
}

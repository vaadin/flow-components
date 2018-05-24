package com.vaadin.flow.component.charts.tests;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class ChartsSerializableTest extends ClassesSerializableTest {
    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(
                super.getExcludedPatterns(),
                Stream.of("com\\.vaadin\\.flow\\.component\\.charts\\.Chart"/*todo remove and fix*/,
                        "^((?!\\.charts\\.).)*$" /*todo remove when upgraded to new flow*/,
                        ".*\\Serializer(Modifier)?$",
                        "com\\.vaadin\\.flow\\.component\\.charts\\.model\\.serializers\\.BeanSerializationDelegate"
                ));
    }
}

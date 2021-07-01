package com.vaadin.flow.component.login;

import java.util.stream.Stream;

import com.vaadin.flow.testutil.ClassesSerializableTest;

public class LoginSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(),
                Stream.of("com\\.vaadin\\.base\\.devserver\\..*"));
    }
}

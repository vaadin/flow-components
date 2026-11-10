/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoordinateTest {

    @Test
    void equalsAndHashCode_sameXAndY_areEqual() {
        Coordinate coordinate1 = new Coordinate(1.123, 2.345);
        Coordinate coordinate2 = new Coordinate(1.123, 2.345);

        Assertions.assertEquals(coordinate1, coordinate2);
        Assertions.assertEquals(coordinate1.hashCode(), coordinate2.hashCode());
    }

    @Test
    void equals_differentXOrY_notEqual() {
        Coordinate coordinate = new Coordinate(1.123, 2.345);

        Assertions.assertNotEquals(coordinate, new Coordinate(1.123, 0));
        Assertions.assertNotEquals(coordinate, new Coordinate(0, 2.345));
    }

    @Test
    void equals_nullOrOtherType_notEqual() {
        Coordinate coordinate = new Coordinate(1.123, 2.345);
        Object nullObject = null;
        Object otherType = "Coordinate";

        Assertions.assertNotEquals(coordinate, nullObject);
        Assertions.assertNotEquals(coordinate, otherType);
    }
}

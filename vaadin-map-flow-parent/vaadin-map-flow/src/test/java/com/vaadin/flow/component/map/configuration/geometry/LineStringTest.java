/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;

class LineStringTest {

    private LineString lineString;

    @BeforeEach
    void setup() {
        Coordinate coord1 = new Coordinate(0, 0);
        Coordinate coord2 = new Coordinate(10, 10);
        lineString = new LineString(new Coordinate[] { coord1, coord2 });
    }

    @Test
    void getType_returnsCorrectType() {
        Assertions.assertEquals(Constants.OL_GEOMETRY_LINESTRING,
                lineString.getType());
    }

    @Test
    void initializeWithList() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        Coordinate coord3 = new Coordinate(5, 6);
        LineString line = new LineString(List.of(coord1, coord2, coord3));

        Coordinate[] coordinates = line.getCoordinates();
        Assertions.assertEquals(3, coordinates.length);
        Assertions.assertEquals(coord1, coordinates[0]);
        Assertions.assertEquals(coord2, coordinates[1]);
        Assertions.assertEquals(coord3, coordinates[2]);
    }

    @Test
    void initializeWithArray() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        LineString line = new LineString(new Coordinate[] { coord1, coord2 });

        Coordinate[] coordinates = line.getCoordinates();
        Assertions.assertEquals(2, coordinates.length);
        Assertions.assertEquals(coord1, coordinates[0]);
        Assertions.assertEquals(coord2, coordinates[1]);
    }

    @Test
    void initializeWithVarargs() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        LineString line = new LineString(coord1, coord2);

        Coordinate[] coordinates = line.getCoordinates();
        Assertions.assertEquals(2, coordinates.length);
        Assertions.assertEquals(coord1, coordinates[0]);
        Assertions.assertEquals(coord2, coordinates[1]);
    }

    @Test
    void setCoordinatesWithList() {
        Coordinate coord1 = new Coordinate(5, 5);
        Coordinate coord2 = new Coordinate(15, 15);
        Coordinate coord3 = new Coordinate(20, 20);
        lineString.setCoordinates(List.of(coord1, coord2, coord3));

        Coordinate[] coordinates = lineString.getCoordinates();
        Assertions.assertEquals(3, coordinates.length);
        Assertions.assertEquals(coord1, coordinates[0]);
        Assertions.assertEquals(coord2, coordinates[1]);
        Assertions.assertEquals(coord3, coordinates[2]);
    }

    @Test
    void setCoordinatesWithArray() {
        Coordinate coord1 = new Coordinate(5, 5);
        Coordinate coord2 = new Coordinate(15, 15);
        lineString.setCoordinates(new Coordinate[] { coord1, coord2 });

        Coordinate[] coordinates = lineString.getCoordinates();
        Assertions.assertEquals(2, coordinates.length);
        Assertions.assertEquals(coord1, coordinates[0]);
        Assertions.assertEquals(coord2, coordinates[1]);
    }

    @Test
    void setCoordinatesWithVarargs() {
        Coordinate coord1 = new Coordinate(5, 5);
        Coordinate coord2 = new Coordinate(15, 15);
        lineString.setCoordinates(new Coordinate[] { coord1, coord2 });

        Coordinate[] coordinates = lineString.getCoordinates();
        Assertions.assertEquals(2, coordinates.length);
        Assertions.assertEquals(coord1, coordinates[0]);
        Assertions.assertEquals(coord2, coordinates[1]);
    }

    @Test
    void translate() {
        lineString.translate(5, 10);

        Coordinate[] coordinates = lineString.getCoordinates();
        Assertions.assertEquals(5, coordinates[0].getX(), 0);
        Assertions.assertEquals(10, coordinates[0].getY(), 0);
        Assertions.assertEquals(15, coordinates[1].getX(), 0);
        Assertions.assertEquals(20, coordinates[1].getY(), 0);
    }

    @Test
    void constructWithNullList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString((List<Coordinate>) null));
    }

    @Test
    void constructWithNullArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString((Coordinate[]) null));
    }

    @Test
    void constructWithEmptyList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString(List.of()));
    }

    @Test
    void constructWithEmptyArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString(new Coordinate[] {}));
    }

    @Test
    void constructWithSingleCoordinateList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString(List.of(new Coordinate(0, 0))));
    }

    @Test
    void constructWithSingleCoordinateArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString(
                        new Coordinate[] { new Coordinate(0, 0) }));
    }

    @Test
    void constructWithSingleCoordinateArgument_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new LineString(new Coordinate(0, 0)));
    }

    @Test
    void setCoordinatesWithNullList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lineString.setCoordinates((List<Coordinate>) null));
    }

    @Test
    void setCoordinatesWithNullArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lineString.setCoordinates((Coordinate[]) null));
    }

    @Test
    void setCoordinatesWithEmptyList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lineString.setCoordinates(List.of()));
    }

    @Test
    void setCoordinatesWithEmptyArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lineString.setCoordinates(new Coordinate[] {}));
    }

    @Test
    void setCoordinatesWithSingleCoordinateList_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lineString.setCoordinates(List.of(new Coordinate(0, 0))));
    }

    @Test
    void setCoordinatesWithSingleCoordinateArray_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> lineString
                .setCoordinates(new Coordinate[] { new Coordinate(0, 0) }));
    }

    @Test
    void setCoordinatesWithSingleCoordinate_throwsException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lineString.setCoordinates(new Coordinate(0, 0)));
    }
}

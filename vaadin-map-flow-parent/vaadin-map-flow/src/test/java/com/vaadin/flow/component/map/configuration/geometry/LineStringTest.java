/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.geometry;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Coordinate;

public class LineStringTest {

    private LineString lineString;

    @Before
    public void setup() {
        Coordinate coord1 = new Coordinate(0, 0);
        Coordinate coord2 = new Coordinate(10, 10);
        lineString = new LineString(new Coordinate[] { coord1, coord2 });
    }

    @Test
    public void getType_returnsCorrectType() {
        Assert.assertEquals(Constants.OL_GEOMETRY_LINESTRING,
                lineString.getType());
    }

    @Test
    public void initializeWithList() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        Coordinate coord3 = new Coordinate(5, 6);
        LineString line = new LineString(List.of(coord1, coord2, coord3));

        Coordinate[] coordinates = line.getCoordinates();
        Assert.assertEquals(3, coordinates.length);
        Assert.assertEquals(coord1, coordinates[0]);
        Assert.assertEquals(coord2, coordinates[1]);
        Assert.assertEquals(coord3, coordinates[2]);
    }

    @Test
    public void initializeWithArray() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        LineString line = new LineString(new Coordinate[] { coord1, coord2 });

        Coordinate[] coordinates = line.getCoordinates();
        Assert.assertEquals(2, coordinates.length);
        Assert.assertEquals(coord1, coordinates[0]);
        Assert.assertEquals(coord2, coordinates[1]);
    }

    @Test
    public void initializeWithVarargs() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        LineString line = new LineString(coord1, coord2);

        Coordinate[] coordinates = line.getCoordinates();
        Assert.assertEquals(2, coordinates.length);
        Assert.assertEquals(coord1, coordinates[0]);
        Assert.assertEquals(coord2, coordinates[1]);
    }

    @Test
    public void setCoordinatesWithList() {
        Coordinate coord1 = new Coordinate(5, 5);
        Coordinate coord2 = new Coordinate(15, 15);
        Coordinate coord3 = new Coordinate(20, 20);
        lineString.setCoordinates(List.of(coord1, coord2, coord3));

        Coordinate[] coordinates = lineString.getCoordinates();
        Assert.assertEquals(3, coordinates.length);
        Assert.assertEquals(coord1, coordinates[0]);
        Assert.assertEquals(coord2, coordinates[1]);
        Assert.assertEquals(coord3, coordinates[2]);
    }

    @Test
    public void setCoordinatesWithArray() {
        Coordinate coord1 = new Coordinate(5, 5);
        Coordinate coord2 = new Coordinate(15, 15);
        lineString.setCoordinates(new Coordinate[] { coord1, coord2 });

        Coordinate[] coordinates = lineString.getCoordinates();
        Assert.assertEquals(2, coordinates.length);
        Assert.assertEquals(coord1, coordinates[0]);
        Assert.assertEquals(coord2, coordinates[1]);
    }

    @Test
    public void setCoordinatesWithVarargs() {
        Coordinate coord1 = new Coordinate(5, 5);
        Coordinate coord2 = new Coordinate(15, 15);
        lineString.setCoordinates(new Coordinate[] { coord1, coord2 });

        Coordinate[] coordinates = lineString.getCoordinates();
        Assert.assertEquals(2, coordinates.length);
        Assert.assertEquals(coord1, coordinates[0]);
        Assert.assertEquals(coord2, coordinates[1]);
    }

    @Test
    public void translate() {
        lineString.translate(5, 10);

        Coordinate[] coordinates = lineString.getCoordinates();
        Assert.assertEquals(5, coordinates[0].getX(), 0);
        Assert.assertEquals(10, coordinates[0].getY(), 0);
        Assert.assertEquals(15, coordinates[1].getX(), 0);
        Assert.assertEquals(20, coordinates[1].getY(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullList_throwsException() {
        new LineString((List<Coordinate>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNullArray_throwsException() {
        new LineString((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyList_throwsException() {
        new LineString(List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyArray_throwsException() {
        new LineString(new Coordinate[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateList_throwsException() {
        new LineString(List.of(new Coordinate(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateArray_throwsException() {
        new LineString(new Coordinate[] { new Coordinate(0, 0) });
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithSingleCoordinateArgument_throwsException() {
        new LineString(new Coordinate(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithNullList_throwsException() {
        lineString.setCoordinates((List<Coordinate>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithNullArray_throwsException() {
        lineString.setCoordinates((Coordinate[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithEmptyList_throwsException() {
        lineString.setCoordinates(List.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithEmptyArray_throwsException() {
        lineString.setCoordinates(new Coordinate[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinateList_throwsException() {
        lineString.setCoordinates(List.of(new Coordinate(0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinateArray_throwsException() {
        lineString.setCoordinates(new Coordinate[] { new Coordinate(0, 0) });
    }

    @Test(expected = IllegalArgumentException.class)
    public void setCoordinatesWithSingleCoordinate_throwsException() {
        lineString.setCoordinates(new Coordinate(0, 0));
    }
}

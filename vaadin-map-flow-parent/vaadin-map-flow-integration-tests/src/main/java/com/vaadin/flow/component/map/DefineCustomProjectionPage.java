/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.View;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/define-custom-projection")
public class DefineCustomProjectionPage extends Div {

    // WKS for ETRS89 / TM35FIN(E,N) -- Finland
    // Source: https://epsg.io/3067
    //
    //@formatter:off
    private static final String EPSG_3067_WKS = ""
            + "PROJCS[\"ETRS89 / TM35FIN(E,N)\",\n"
            + "    GEOGCS[\"ETRS89\",\n"
            + "        DATUM[\"European_Terrestrial_Reference_System_1989\",\n"
            + "            SPHEROID[\"GRS 1980\",6378137,298.257222101,\n"
            + "                AUTHORITY[\"EPSG\",\"7019\"]],\n"
            + "            TOWGS84[0,0,0,0,0,0,0],\n"
            + "            AUTHORITY[\"EPSG\",\"6258\"]],\n"
            + "        PRIMEM[\"Greenwich\",0,\n"
            + "            AUTHORITY[\"EPSG\",\"8901\"]],\n"
            + "        UNIT[\"degree\",0.0174532925199433,\n"
            + "            AUTHORITY[\"EPSG\",\"9122\"]],\n"
            + "        AUTHORITY[\"EPSG\",\"4258\"]],\n"
            + "    PROJECTION[\"Transverse_Mercator\"],\n"
            + "    PARAMETER[\"latitude_of_origin\",0],\n"
            + "    PARAMETER[\"central_meridian\",27],\n"
            + "    PARAMETER[\"scale_factor\",0.9996],\n"
            + "    PARAMETER[\"false_easting\",500000],\n"
            + "    PARAMETER[\"false_northing\",0],\n"
            + "    UNIT[\"metre\",1,\n"
            + "        AUTHORITY[\"EPSG\",\"9001\"]],\n"
            + "    AXIS[\"Easting\",EAST],\n"
            + "    AXIS[\"Northing\",NORTH],\n"
            + "    AUTHORITY[\"EPSG\",\"3067\"]]";
    //@formatter:on

    public DefineCustomProjectionPage() {
        // Define custom EPSG:3067 projection
        Map.defineProjection("EPSG:3067", EPSG_3067_WKS);
        // Use EPSG:3067 as user projection
        Map.setUserProjection("EPSG:3067");

        Map map = new Map();
        // Set ID for easier referencing from tests
        map.getBackgroundLayer().setId("background-layer");
        // Use EPSG:3067 as view projection
        map.setView(new View("EPSG:3067"));
        // Coordinates for Turku in EPSG:3067
        map.setCenter(new Coordinate(239895.33, 6711153.69));
        map.setZoom(10);

        Div eventData = new Div();
        eventData.setId("event-data");
        map.addViewMoveEndEventListener(event -> {
            String eventText = event.getCenter().getX() + ";"
                    + event.getCenter().getY() + ";";

            eventData.setText(eventText);
        });

        add(map, eventData);
    }
}

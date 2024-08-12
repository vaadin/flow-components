/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import org.junit.Assert;
import org.junit.Test;

public class MapVariantTest {
    @Test
    public void addThemeVariants() {
        Map map = new Map();
        map.addThemeVariants(MapVariant.BORDERLESS);

        Assert.assertEquals("borderless",
                map.getElement().getAttribute("theme"));
    }

    @Test
    public void removeThemeVariants() {
        Map map = new Map();
        map.getElement().setAttribute("theme", "borderless");
        map.removeThemeVariants(MapVariant.BORDERLESS);

        Assert.assertNull(map.getElement().getAttribute("theme"));
    }
}

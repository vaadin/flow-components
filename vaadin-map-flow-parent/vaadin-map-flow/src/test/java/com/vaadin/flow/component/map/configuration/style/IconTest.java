/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.Assets;

class IconTest {

    @Test
    void defaults() {
        Icon.Options options = new Icon.Options();
        options.setSrc("test");
        Icon icon = new Icon(options);

        Assertions.assertNotNull(icon.getAnchor());
        Assertions.assertEquals(0.5, icon.getAnchor().getX(), 0);
        Assertions.assertEquals(0.5, icon.getAnchor().getY(), 0);

        Assertions.assertEquals(Icon.AnchorOrigin.TOP_LEFT,
                icon.getAnchorOrigin());

        Assertions.assertNull(icon.getColor());
        Assertions.assertNull(icon.getCrossOrigin());
        Assertions.assertEquals("test", icon.getSrc());
        Assertions.assertNull(icon.getImg());
        Assertions.assertNull(icon.getImgHandler());
        Assertions.assertNull(icon.getImgSize());
    }

    @Test
    void withOptions() {
        String src = "assets/custom-marker.png";
        Icon.Anchor anchor = new Icon.Anchor(0.5f, 1);
        Icon.AnchorOrigin anchorOrigin = Icon.AnchorOrigin.TOP_RIGHT;
        String color = "cornflowerblue";
        String crossOrigin = "customCrossOrigin";

        Icon.Options options = new Icon.Options();
        options.setOpacity(0.8f);
        options.setScale(2f);
        options.setSrc(src);
        options.setAnchor(anchor);
        options.setAnchorOrigin(anchorOrigin);
        options.setColor(color);
        options.setCrossOrigin(crossOrigin);
        Icon icon = new Icon(options);

        Assertions.assertEquals(0.8, icon.getOpacity(), 0.001);
        Assertions.assertEquals(2, icon.getScale(), 0.001);
        Assertions.assertEquals(src, icon.getSrc());
        Assertions.assertNotNull(icon.getAnchor());
        Assertions.assertEquals(anchor.getX(), icon.getAnchor().getX(), 0);
        Assertions.assertEquals(anchor.getY(), icon.getAnchor().getY(), 0);
        Assertions.assertEquals(anchorOrigin, icon.getAnchorOrigin());
        Assertions.assertEquals(color, icon.getColor());
        Assertions.assertEquals(crossOrigin, icon.getCrossOrigin());
    }

    @Test
    void failsWithoutSourceUrlOrImage() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new Icon(new Icon.Options()));
    }

    @Test
    void failsWithBothSourceUrlAndImage() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            Icon.Options options = new Icon.Options();
            options.setSrc("test");
            options.setImg(Assets.PIN.getHandler());
            new Icon(options);
        });
    }
}

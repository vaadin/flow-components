package com.vaadin.flow.component.map.configuration.style;

import com.vaadin.flow.component.map.Assets;
import org.junit.Assert;
import org.junit.Test;

public class IconTest {

    @Test
    public void defaults() {
        Icon icon = new Icon(new Icon.Options().setSrc("test"));

        Assert.assertNotNull(icon.getAnchor());
        Assert.assertEquals(0.5, icon.getAnchor().getX(), 0);
        Assert.assertEquals(0.5, icon.getAnchor().getY(), 0);

        Assert.assertEquals(Icon.AnchorOrigin.TOP_LEFT, icon.getAnchorOrigin());

        Assert.assertNull(icon.getColor());
        Assert.assertNull(icon.getCrossOrigin());
        Assert.assertEquals("test", icon.getSrc());
        Assert.assertNull(icon.getImg());
        Assert.assertNull(icon.getImgSize());
    }

    @Test
    public void withOptions() {
        String src = "assets/custom-marker.png";
        Icon.Anchor anchor = new Icon.Anchor(0.5f, 1);
        Icon.AnchorOrigin anchorOrigin = Icon.AnchorOrigin.TOP_RIGHT;
        String color = "cornflowerblue";
        String crossOrigin = "customCrossOrigin";

        Icon icon = new Icon(new Icon.Options().setSrc(src).setAnchor(anchor)
                .setAnchorOrigin(anchorOrigin).setColor(color)
                .setCrossOrigin(crossOrigin));

        Assert.assertNotNull(icon.getAnchor());
        Assert.assertEquals(anchor.getX(), icon.getAnchor().getX(), 0);
        Assert.assertEquals(anchor.getY(), icon.getAnchor().getY(), 0);
        Assert.assertEquals(anchorOrigin, icon.getAnchorOrigin());
        Assert.assertEquals(color, icon.getColor());
        Assert.assertEquals(crossOrigin, icon.getCrossOrigin());
        Assert.assertEquals(src, icon.getSrc());
    }

    @Test
    public void failsWithoutSourceUrlOrImage() {
        Assert.assertThrows(NullPointerException.class,
                () -> new Icon(new Icon.Options()));
    }

    @Test
    public void failsWithBothSourceUrlAndImage() {
        Assert.assertThrows(IllegalStateException.class,
                () -> new Icon(new Icon.Options().setSrc("test")
                        .setImg(Assets.DEFAULT_MARKER.getResource())));
    }
}
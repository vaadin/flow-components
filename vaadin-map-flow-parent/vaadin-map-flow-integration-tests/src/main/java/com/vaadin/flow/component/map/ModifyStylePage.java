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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.router.Route;

@Route("vaadin-map/modify-style")
public class ModifyStylePage extends Div {
    public ModifyStylePage() {
        Map map = new Map();
        map.getFeatureLayer().setId("feature-layer");

        MarkerFeature marker = new MarkerFeature(new Coordinate(0, 0),
                createIcon());
        map.getFeatureLayer().addFeature(marker);

        NativeButton setImage = new NativeButton("Set image", e -> {
            Icon icon = createIcon();
            marker.getStyle().setImage(icon);
        });
        setImage.setId("set-style-image");

        NativeButton setImageScale = new NativeButton("Set image scale",
                e -> marker.getStyle().getImage().setScale(5));
        setImageScale.setId("set-image-scale");

        Span renderCount = new Span("0");
        renderCount.setId("render-count");
        // Register event listener for the rendercomplete event, and increase
        // render count
        //@formatter:off
        renderCount.getElement().executeJs(
                "const map = $0;" +
                        "map.configuration.on('rendercomplete', () => {" +
                        "  const nextValue = parseInt(this.textContent) + 1;" +
                        "  this.textContent = nextValue.toString();" +
                        "});",
                map.getElement()
        );
        //@formatter:on

        add(map);
        add(new Div(setImage, setImageScale));
        add(new Div(new Span("Number of renders: "), renderCount));
    }

    private Icon createIcon() {
        Icon.ImageSize pointImageSize = new Icon.ImageSize(
                Assets.POINT.getWidth(), Assets.POINT.getHeight());
        Icon.Options iconOptions = new Icon.Options();
        iconOptions.setSrc(MarkerFeature.POINT_ICON.getSrc());
        iconOptions.setImg(Assets.POINT.getResource());
        iconOptions.setImgSize(pointImageSize);
        iconOptions.setScale(0.25f);
        iconOptions.setAnchorOrigin(Icon.AnchorOrigin.TOP_LEFT);
        iconOptions.setAnchor(new Icon.Anchor(0.5f, 0.5f));

        return new Icon(iconOptions);
    }
}

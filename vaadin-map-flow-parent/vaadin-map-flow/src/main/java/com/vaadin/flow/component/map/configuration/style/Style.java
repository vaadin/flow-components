package com.vaadin.flow.component.map.configuration.style;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public class Style extends AbstractConfigurationObject {

    private ImageStyle image;

    @Override
    public String getType() {
        return Constants.OL_STYLE_STYLE;
    }

    public ImageStyle getImage() {
        return image;
    }

    public void setImage(ImageStyle image) {
        updateNestedPropertyObserver(this.image, image);
        this.image = image;
        notifyChange();
    }
}

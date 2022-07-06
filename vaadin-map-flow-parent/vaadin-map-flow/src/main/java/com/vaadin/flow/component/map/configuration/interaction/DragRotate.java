package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class DragRotate extends Interaction {

    public DragRotate(boolean active) {
        super(active);
    }

    @Override
    public String getType() {
        return Constants.OL_DRAGROTATE;
    }

}

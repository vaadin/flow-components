package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class DragPan extends Interaction {

    public DragPan(boolean active) {
        super(active);
    }

    @Override
    public String getType() {
        return Constants.OL_DRAGPAN;
    }

}

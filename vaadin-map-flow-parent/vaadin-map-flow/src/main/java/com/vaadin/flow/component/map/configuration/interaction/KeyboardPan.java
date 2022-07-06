package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class KeyboardPan extends Interaction {

    public KeyboardPan(boolean active) {
        super(active);
    }

    @Override
    public String getType() {
        return Constants.OL_KEYBOARDPAN;
    }

}

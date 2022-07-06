package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class KeyboardZoom extends Interaction {
	
	public KeyboardZoom(boolean active) {
		super(active);
	}

	@Override
    public String getType() {
        return Constants.OL_KEYBOARDZOOM;
    }

}

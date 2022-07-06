package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class PinchZoom extends Interaction {
	
	public PinchZoom(boolean active) {
		super(active);
	}

	@Override
    public String getType() {
        return Constants.OL_PINCHZOOM;
    }

}

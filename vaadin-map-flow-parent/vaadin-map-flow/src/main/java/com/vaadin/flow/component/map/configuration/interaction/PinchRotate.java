package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class PinchRotate extends Interaction {
	
	public PinchRotate(boolean active) {
		super(active);
	}

	@Override
    public String getType() {
        return Constants.OL_PINCHROTATE;
    }

}

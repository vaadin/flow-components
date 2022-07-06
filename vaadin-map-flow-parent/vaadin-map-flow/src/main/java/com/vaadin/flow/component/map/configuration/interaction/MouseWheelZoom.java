package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class MouseWheelZoom extends Interaction {
	
	public MouseWheelZoom(boolean active) {
		super(active);
	}

	@Override
    public String getType() {
        return Constants.OL_MOUSEWHEELZOOM;
    }

}

package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class DoubleClickZoom extends Interaction {
	
	public DoubleClickZoom(boolean active) {
		super(active);
	}

	@Override
    public String getType() {
        return Constants.OL_DOUBLECLICKZOOM;
    }

}

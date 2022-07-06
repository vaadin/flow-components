package com.vaadin.flow.component.map.configuration.interaction;

import com.vaadin.flow.component.map.configuration.Constants;

public class DragZoom extends Interaction {
	
	public DragZoom(boolean active) {
		super(active);
	}

	@Override
    public String getType() {
        return Constants.OL_DRAGZOOM;
    }

}

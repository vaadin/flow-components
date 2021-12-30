package com.vaadin.flow.component.charts.events.internal;

import java.io.Serializable;

public class ScrollbarVisibilityChanged implements Serializable {
    private boolean visibility;

    public boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public ScrollbarVisibilityChanged(boolean value) {
        this.visibility = value;
    }
}

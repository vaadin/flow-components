package com.vaadin.flow.component.map.configuration.layer;

import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.source.VectorSource;

import java.util.Objects;

public class VectorLayer extends Layer {
    private VectorSource source;

    @Override
    public String getType() {
        return Constants.OL_LAYER_VECTOR;
    }

    public VectorSource getSource() {
        return source;
    }

    public void setSource(VectorSource source) {
        Objects.requireNonNull(source);

        updateNestedPropertyObserver(this.source, source);

        this.source = source;
        notifyChange();
    }
}

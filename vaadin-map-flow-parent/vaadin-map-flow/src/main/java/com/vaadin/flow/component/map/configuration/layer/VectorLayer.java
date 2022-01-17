package com.vaadin.flow.component.map.configuration.layer;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright (C) 2022 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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

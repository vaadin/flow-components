/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VectorSource extends Source {
    private final List<Feature> features = new ArrayList<>();

    public VectorSource() {
        this(new Options());
    }

    public VectorSource(Options options) {
        super(options);
    }

    @Override
    public String getType() {
        return Constants.OL_SOURCE_VECTOR;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public void addFeature(Feature feature) {
        Objects.requireNonNull(feature);

        features.add(feature);
        addChild(feature);
    }

    public void removeFeature(Feature feature) {
        Objects.requireNonNull(feature);

        features.remove(feature);
        removeChild(feature);
    }

    public static class Options extends Source.Options {
    }
}

package com.vaadin.flow.component.map.configuration.interaction;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Feature;

public class Translate extends Interaction {

    private Feature feature;

    public Translate(Map map, Feature feature) {
        super(true);
        Objects.requireNonNull(feature);
        setFeature(feature);
    }

    @Override
    public String getType() {
        return Constants.OL_TRANSLATE;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Feature getFeature() {
        return feature;
    }

    private void setFeature(Feature feature) {
        removeChild(this.feature);
        this.feature = feature;
        addChild(feature);
    }

}

package com.vaadin.flow.component.map.configuration;

import com.vaadin.flow.component.map.configuration.layer.Layer;

import java.util.ArrayList;
import java.util.List;

public class Configuration extends AbstractConfigurationObject {
    private List<Layer> layers = new ArrayList<>();
    private View view = new View();

    @Override
    public String getType() {
        return Constants.OL_MAP;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}

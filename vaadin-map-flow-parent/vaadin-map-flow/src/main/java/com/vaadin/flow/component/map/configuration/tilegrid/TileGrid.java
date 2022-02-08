package com.vaadin.flow.component.map.configuration.tilegrid;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Extent;

public class TileGrid extends AbstractConfigurationObject {
    public Extent getExtent() {
        return extent;
    }

    public void setExtent(Extent extent) {
        this.extent = extent;
    }

    public TileSize getSize() {
        return size;
    }

    public void setSize(TileSize size) {
        this.size = size;
    }

    public double[] getResolutions() {
        return resolutions;
    }

    public void setResolutions(double[] resolutions) {
        this.resolutions = resolutions;
    }

    private Extent extent;
    private TileSize size;
    private double[] resolutions;

    public TileGrid(Extent extent, TileSize size, double[] resolutions) {
        this.extent = extent;
        this.size = size;
        this.resolutions = resolutions;
    }

    @Override
    public String getType() {
        return Constants.OL_TILEGRID_TILEGRID;
    }
}

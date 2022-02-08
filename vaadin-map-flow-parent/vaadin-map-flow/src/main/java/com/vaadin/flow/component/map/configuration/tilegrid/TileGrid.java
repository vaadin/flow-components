package com.vaadin.flow.component.map.configuration.tilegrid;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;
import com.vaadin.flow.component.map.configuration.Extent;

/**
 * Class for setting the grid pattern for sources accessing tiled-image servers
 */
public class TileGrid extends AbstractConfigurationObject {
    private Extent extent;
    private TileSize tileSize;
    private double[] resolutions;

    public TileGrid(Extent extent, TileSize size, double[] resolutions) {
        this.extent = extent;
        this.tileSize = size;
        this.resolutions = resolutions;
    }

    @Override
    public String getType() {
        return Constants.OL_TILEGRID_TILEGRID;
    }

    /**
     * Get the extent for this tile grid.
     *
     * @return extent of the tile grid.
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Get the tile size of the grid
     *
     * @return tile size of the grid
     */
    public TileSize getTileSize() {
        return tileSize;
    }

    /**
     * Get the list of resolutions for the tile grid.
     *
     * @return the list of resolutions for the tile grid.
     */
    public double[] getResolutions() {
        return resolutions;
    }
}

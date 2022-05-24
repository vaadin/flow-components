package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Pane Container to allow multiple Panes
 */
public class PaneList extends AbstractConfigurationObject {
    private final List<Pane> paneList = new ArrayList<>();

    /**
     * @return The number of panes in the list
     */
    public int getNumberOfPanes() {
        return paneList.size();
    }

    /**
     * @param index
     *            Index of the pane
     * @return The pane with the given index
     */
    public Pane getPane(int index) {
        return paneList.get(index);
    }

    /**
     * Returns the pane list. Use this only for serialization.
     *
     * @return The pane list.
     */
    public List<Pane> getPanes() {
        return paneList;
    }

    /**
     * Adds a new pane to the list
     *
     * @param pane
     *            The pane to add
     */
    public void addPane(Pane pane) {
        paneList.add(pane);
        updateIndexes();
    }

    /**
     * Removes a pane from the list
     *
     * @param pane
     *            The pane to remove
     */
    public void removePane(Pane pane) {
        paneList.remove(pane);
        updateIndexes();

    }

    private void updateIndexes() {
        for (int i = 0; i < paneList.size(); i++) {
            paneList.get(i).setPaneIndex(i);
        }
    }
}

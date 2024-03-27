/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

public class States extends AbstractConfigurationObject {

    private Hover hover;
    private Select select;
    private Inactive inactive;

    public States() {
    }

    /**
     * @see #setHover(Hover)
     */
    public Hover getHover() {
        if (hover == null) {
            hover = new Hover();
        }
        return hover;
    }

    /**
     * The appearance of the other marker or data point when hover.
     *
     * @see #setInactive(Hover)
     */
    public void setHover(Hover hover) {
        this.hover = hover;
    }

    /**
     * @see #setSelect(Select)
     */
    public Select getSelect() {
        if (select == null) {
            select = new Select();
        }
        return select;
    }

    /**
     * The appearance of the point marker when selected. In order to allow a
     * point to be selected, set the <code>series.allowPointSelect</code> option
     * to true.
     */
    public void setSelect(Select select) {
        this.select = select;
    }

    /**
     * @see #setInactive(Hover)
     */
    public Inactive getInactive() {
        if (inactive == null) {
            inactive = new Inactive();
        }
        return inactive;
    }

    /**
     * The appearance of the other data points when one is hovered.
     * <p>
     * Note: Inactive style is not applied to <code>Marker</code>.
     *
     * @see #setHover(Hover)
     */
    public void setInactive(Inactive inactive) {
        this.inactive = inactive;
    }
}

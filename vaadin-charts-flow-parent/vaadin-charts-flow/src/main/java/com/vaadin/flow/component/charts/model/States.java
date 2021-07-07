package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 * 
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
     * The appearance of the other marker when hover.
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
     * The appearance of the other point markers when one is hovered.
     *
     * @see #setHover(Hover)
     */
    public void setInactive(Inactive inactive) {
        this.inactive = inactive;
    }
}

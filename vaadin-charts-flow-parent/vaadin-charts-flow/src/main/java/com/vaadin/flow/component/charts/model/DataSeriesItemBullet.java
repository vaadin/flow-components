package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2020 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

import java.time.Instant;

/**
 * DataSeriesItem that can hold also target and targetOptions. Used in e.g.
 * bullet series.
 */
public class DataSeriesItemBullet extends DataSeriesItem {

    private Number target;
    private TargetOptions targetOptions;

    /**
     * Constructs an empty item
     */
    public DataSeriesItemBullet() {
        super();
    }

    /**
     * Constructs an item with Y and Target
     * 
     * @param y
     * @param target
     */
    public DataSeriesItemBullet(Number y, Number target) {
        super();
        setY(y);
        setTarget(target);
    }

    /**
     * Constructs an item with X, Y and Target
     * 
     * @param x
     * @param y
     * @param target
     */
    public DataSeriesItemBullet(Number x, Number y, Number target) {
        super(x, y);
        setTarget(target);
    }

    /**
     * Constructs an item with X, Y and Target
     * 
     * @param x
     * @param y
     * @param target
     */
    public DataSeriesItemBullet(Instant x, Number y, Number target) {
        super(x, y);
        setTarget(target);
    }

    /**
     * Returns the target value of the item.
     *
     * @see #setTarget(Number)
     * @return The target value of this data item.
     */
    public Number getTarget() {
        return target;
    }

    /**
     * The target value of the item.
     *
     * @param target
     *            target value of the item.
     */
    public void setTarget(Number target) {
        this.target = target;
        makeCustomized();
    }

    /**
     * @see #setPartialFill(ItemPartialFill)
     */
    public TargetOptions getTargetOptions() {
        if (targetOptions == null) {
            targetOptions = new TargetOptions();
            makeCustomized();
        }
        return targetOptions;
    }

    /**
     * Individual target options for each point.
     */
    public void setPartialFill(TargetOptions targetOptions) {
        this.targetOptions = targetOptions;
        makeCustomized();
    }

}

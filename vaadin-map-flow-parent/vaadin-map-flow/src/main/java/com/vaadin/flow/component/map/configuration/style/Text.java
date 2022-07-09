package com.vaadin.flow.component.map.configuration.style;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.Constants;

public class Text extends AbstractConfigurationObject {

    private String font = "10px sans-serif";

    private int offsetX = 0;

    private int offsetY = 10;

    private boolean overflow = false;

    private String placement = "point";

    private float scale = 1f;

    private boolean roateWithView = false;

    private float rotation = 0f;

    private String text;

    private String textAlign = "center";

    private String textBaseline = "middle";

    private Fill fill;

    private Stroke stroke;

    private Fill backgroundFill;

    private Stroke backgroundStroke;

    public Text() {
        setFill(new Fill("#000"));
        setStroke(new Stroke("#fff", 1));
    }

    public Text(String text) {
        this();
        setText(text);
    }

    @Override
    public String getType() {
        return Constants.OL_STYLE_TEXT;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        removeChild(this.fill);
        this.fill = fill;
        addChild(fill);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        removeChild(this.stroke);
        this.stroke = stroke;
        addChild(stroke);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Fill getBackgroundFill() {
        return backgroundFill;
    }

    public void setBackgroundFill(Fill backgroundFill) {
        removeChild(this.backgroundFill);
        this.backgroundFill = backgroundFill;
        addChild(backgroundFill);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Stroke getBackgroundStroke() {
        return backgroundStroke;
    }

    public void setBackgroundStroke(Stroke backgroundStroke) {
        removeChild(this.backgroundStroke);
        this.backgroundStroke = backgroundStroke;
        addChild(backgroundStroke);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        markAsDirty();
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
        markAsDirty();
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
        markAsDirty();
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
        markAsDirty();
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
        markAsDirty();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        markAsDirty();
    }

    public boolean isRoateWithView() {
        return roateWithView;
    }

    public void setRoateWithView(boolean roateWithView) {
        this.roateWithView = roateWithView;
        markAsDirty();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        markAsDirty();
    }

    public String getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
        markAsDirty();
    }

    public String getTextBaseline() {
        return textBaseline;
    }

    public void setTextBaseline(String textBaseline) {
        this.textBaseline = textBaseline;
        markAsDirty();
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
        markAsDirty();
    }

}

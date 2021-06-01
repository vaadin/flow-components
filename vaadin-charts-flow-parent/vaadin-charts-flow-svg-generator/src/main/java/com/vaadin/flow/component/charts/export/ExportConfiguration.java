package com.vaadin.flow.component.charts.export;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.style.Theme;

/**
 * <p>Configuration settings for exporting charts in the server.</p>
 *
 * @since 21.0
 */
public class ExportConfiguration extends AbstractConfigurationObject {

    @JsonUnwrapped
    private Theme theme;
    private Lang lang;
    private Number height;
    private Number width;

    public ExportConfiguration() {
        super();
    }

    //<editor-fold desc="Getters and setters">
    public Theme getTheme() {
        return theme;
    }

    public ExportConfiguration theme(Theme theme) {
        this.theme = theme;
        return this;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Lang getLang() {
        return lang;
    }

    public ExportConfiguration lang(Lang lang) {
        this.lang = lang;
        return this;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public Number getHeight() {
        return height;
    }

    public ExportConfiguration height(Number height) {
        this.height = height;
        return this;
    }

    public void setHeight(Number height) {
        this.height = height;
    }

    public Number getWidth() {
        return width;
    }

    public ExportConfiguration width(Number width) {
        this.width = width;
        return this;
    }

    public void setWidth(Number width) {
        this.width = width;
    }
    //</editor-fold>
}

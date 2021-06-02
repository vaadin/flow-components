package com.vaadin.flow.component.charts.export;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.style.Theme;

/**
 * <p>
 * Optional settings for exporting charts in the server.
 * </p>
 *
 * @since 21.0
 */
public class ExportOptions extends AbstractConfigurationObject {

    private Theme theme;
    private Lang lang;
    private Number width;
    private Number height;
    private Boolean timeline;
    private Boolean executeFunctions;

    public ExportOptions() {
        super();
    }

    // <editor-fold desc="Getters and setters">
    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public Number getWidth() {
        return width;
    }

    public void setWidth(Number width) {
        this.width = width;
    }

    public Number getHeight() {
        return height;
    }

    public void setHeight(Number height) {
        this.height = height;
    }

    public Boolean getTimeline() {
        return timeline;
    }

    public void setTimeline(Boolean timeline) {
        this.timeline = timeline;
    }

    public boolean getExecuteFunctions() {
        return executeFunctions;
    }

    public void setExecuteFunctions(boolean executeFunctions) {
        this.executeFunctions = executeFunctions;
    }
    // </editor-fold>
}

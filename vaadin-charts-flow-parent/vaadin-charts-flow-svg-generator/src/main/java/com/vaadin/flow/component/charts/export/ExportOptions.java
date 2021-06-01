package com.vaadin.flow.component.charts.export;

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.style.Theme;

/**
 * <p>Optional settings for exporting charts in the server.</p>
 *
 * @since 21.0
 */
public class ExportOptions extends AbstractConfigurationObject {

    private Theme theme;
    private Lang lang;
    private Number width;
    private Number height;
    private boolean timeline;

    public ExportOptions() {
        super();
    }

    //<editor-fold desc="Getters and setters">
    public Theme getTheme() {
        return theme;
    }

    public ExportOptions theme(Theme theme) {
        this.theme = theme;
        return this;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Lang getLang() {
        return lang;
    }

    public ExportOptions lang(Lang lang) {
        this.lang = lang;
        return this;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public Number getWidth() {
        return width;
    }

    public ExportOptions width(Number width) {
        this.width = width;
        return this;
    }

    public void setWidth(Number width) {
        this.width = width;
    }

    public Number getHeight() {
        return height;
    }

    public ExportOptions height(Number height) {
        this.height = height;
        return this;
    }

    public void setHeight(Number height) {
        this.height = height;
    }

    public boolean isTimeline() {
        return timeline;
    }

    public ExportOptions withTimeline(boolean withTimeline) {
        this.timeline = withTimeline;
        return this;
    }

    public void setTimeline(boolean timeline) {
        this.timeline = timeline;
    }
//</editor-fold>
}

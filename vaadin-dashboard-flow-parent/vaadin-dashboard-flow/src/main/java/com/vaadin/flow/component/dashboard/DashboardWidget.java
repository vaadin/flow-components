package com.vaadin.flow.component.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

@Tag("vaadin-dashboard-widget")
public class DashboardWidget extends Component {

    private Component content;

    public DashboardWidget() {
    }

    public DashboardWidget(String title, Component content) {
        setTitle(title);
        this.content = content;
    }

    public String getTitle() {
        return getElement().getProperty("title");
    }

    public void setTitle(String title) {
        getElement().setProperty("title", title);
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
        this.content = content;

        getElement().removeAllChildren();
        getElement().appendChild(content.getElement());
    }

    public int getColspan() {
        return getElement().getProperty("colspan", 1);
    }

    public void setColspan(int colspan) {
        getElement().setProperty("colspan", colspan);
    }

    public int getRowspan() {
        return getElement().getProperty("rowspan", 1);
    }

    public void setRowspan(int rowspan) {
        getElement().setProperty("rowspan", rowspan);
    }

}

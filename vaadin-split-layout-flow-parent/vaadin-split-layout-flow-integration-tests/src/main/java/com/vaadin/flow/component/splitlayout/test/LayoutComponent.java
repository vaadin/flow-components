
package com.vaadin.flow.component.splitlayout.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;

class LayoutComponent extends Div {

    private SplitLayout sidebarWrapper;
    private Div buttonContainer = new Div();
    private Div contentContainer = new Div();
    private Div content;

    public LayoutComponent() {

        content = new Div(new Span("Main"));
        content.setId("mainContentInLayoutComponent");
        content.setSizeFull();
        content.getElement().getStyle().set("border", "1px solid black");

        NativeButton toggleButton = new NativeButton("Toggle",
                event -> toggleSidebar());
        toggleButton.setId("toggleButtonInLayoutComponent");
        buttonContainer.add(toggleButton);
        add(buttonContainer, contentContainer);
        contentContainer.add(content);
    }

    private void toggleSidebar() {

        if (sidebarWrapper == null) {
            sidebarWrapper = new SplitLayout();
            sidebarWrapper.setSplitterPosition(80);
            sidebarWrapper.setSizeFull();
            sidebarWrapper.addToPrimary(content);
            this.contentContainer.removeAll();
            sidebarWrapper.addToSecondary(new Span("Sidebar"));
            this.contentContainer.add(sidebarWrapper);
        } else {
            Component primaryComponent = sidebarWrapper.getPrimaryComponent();
            contentContainer.removeAll();
            contentContainer.add(primaryComponent);
            if (primaryComponent instanceof HasSize) {
                ((HasSize) primaryComponent).setSizeFull();
            }

            sidebarWrapper = null;
        }

    }

}

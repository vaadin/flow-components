
package com.vaadin.flow.component.splitlayout.test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-split-layout/splitter-position")
public class SplitterPositionView extends Div {

    public static final double INITIAL_POSITION = 70;
    public static final double FINAL_POSITION = 30;

    public SplitterPositionView() {
        NativeButton buttonJava = new NativeButton(
                "Create layout with java API", e -> createLayoutJavaApi());
        buttonJava.setId("createLayoutJavaApi");
        NativeButton buttonElement = new NativeButton(
                "Create layout with element API",
                e -> createLayoutElementApi());
        buttonElement.setId("createLayoutElementApi");
        NativeButton buttonLayoutComponent = new NativeButton(
                "Create layout component", e -> add(new LayoutComponent()));
        buttonLayoutComponent.setId("createLayoutComponent");
        add(buttonJava, buttonElement, buttonLayoutComponent);
    }

    private void createLayoutJavaApi() {
        Span primary = new Span("Primary");
        primary.setId("primaryJavaApi");
        Span secondary = new Span("Secondary");
        secondary.setId("secondaryJavaApi");
        SplitLayout layout = new SplitLayout(primary, secondary);
        layout.setSplitterPosition(INITIAL_POSITION);
        layout.setId("splitLayoutJavaApi");
        NativeButton setSplitterPosition = new NativeButton(
                "set splitter position",
                event -> layout.setSplitterPosition(FINAL_POSITION));
        setSplitterPosition.setId("setSplitPositionJavaApi");
        add(setSplitterPosition, layout);
    }

    private void createLayoutElementApi() {
        Span primary = new Span("Primary");
        primary.setId("primaryElementApi");
        Span secondary = new Span("Secondary");
        secondary.setId("secondaryElementApi");
        SplitLayout layout = new SplitLayout();
        layout.setSplitterPosition(INITIAL_POSITION);
        layout.setId("splitLayoutElementApi");
        layout.getElement().appendChild(primary.getElement(),
                secondary.getElement());

        NativeButton setSplitterPosition = new NativeButton(
                "set splitter position",
                event -> layout.setSplitterPosition(FINAL_POSITION));
        setSplitterPosition.setId("setSplitPositionElementApi");
        add(setSplitterPosition, layout);
    }
}

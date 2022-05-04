package com.vaadin.flow.component.spreadsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.spreadsheet.SheetOverlayWrapper.OverlayChangeListener;

@SuppressWarnings("serial")
class MinimizableComponentContainer extends Div {

    private Component content;
    private final Button minimizeButton;
    private OverlayChangeListener listener;

    public MinimizableComponentContainer(Component comp) {
        this.content = comp;
        this.minimizeButton = createMinimizeButton();

        this.add(minimizeButton, content);
    }

    public MinimizableComponentContainer() {
        this.minimizeButton = createMinimizeButton();
        this.add(minimizeButton);
    }

    private Button createMinimizeButton() {
        final Button minimizeButton = new Button(new Icon(VaadinIcon.MINUS));

        // minimizeButton.setClassName(ValoTheme.BUTTON_LINK);
        minimizeButton.addClassName("minimize-button");

        minimizeButton.addClickListener(event -> {
            content.setVisible(!content.isVisible());
            if (content.isVisible()) {
                minimizeButton.setIcon(new Icon(VaadinIcon.MINUS));
            } else {
                minimizeButton.setIcon(new Icon(VaadinIcon.PLUS));
            }

            fireMinimizeEvent();
        });
        return minimizeButton;
    }

    public boolean isMinimized() {
        return !content.isVisible();
    }

    public void setMinimizeListener(OverlayChangeListener listener) {
        this.listener = listener;
    }

    public void fireMinimizeEvent() {
        listener.overlayChanged();
    }

    public void setContent(Component newContent) {
        if (content != null) {
            removeAll();
            add(newContent);
        } else
            add(newContent);

        content = newContent;
    }

    public Component getContent() {
        return content;
    }
}

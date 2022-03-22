package com.vaadin.addon.spreadsheet;

import com.vaadin.addon.spreadsheet.SheetOverlayWrapper.OverlayChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
class MinimizableComponentContainer extends CssLayout {

    private Component content;
    private final Button minimizeButton;
    private OverlayChangeListener listener;

    public MinimizableComponentContainer(Component comp) {
        this.content = comp;
        this.minimizeButton = createMinimizeButton();

        this.addComponents(minimizeButton, content);
    }

    public MinimizableComponentContainer() {
        this.minimizeButton = createMinimizeButton();
        this.addComponent(minimizeButton);
    }

    private Button createMinimizeButton() {
        final Button minimizeButton = new Button(FontAwesome.MINUS);

        minimizeButton.setStyleName(ValoTheme.BUTTON_LINK);
        minimizeButton.addStyleName("minimize-button");

        minimizeButton.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                content.setVisible(!content.isVisible());
                if (content.isVisible()) {
                    minimizeButton.setIcon(FontAwesome.MINUS);
                } else {
                    minimizeButton.setIcon(FontAwesome.PLUS);
                }

                fireMinimizeEvent();
            }
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
        if (content != null)
            replaceComponent(content, newContent);
        else
            addComponent(newContent);

        content = newContent;
    }

    public Component getContent() {
        return content;
    }
}

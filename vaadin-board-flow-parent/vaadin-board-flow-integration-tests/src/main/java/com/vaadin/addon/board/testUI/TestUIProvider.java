package com.vaadin.addon.board.testUI;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

/**
 *
 */
@SuppressWarnings("serial")
public class TestUIProvider extends UIProvider {
    private static Logger logger = Logger.getLogger(TestUIProvider.class
            .getName());

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        if(event.getRequest().getPathInfo().endsWith(".js")) {
            return null;
        }
        String name = (event.getRequest()).getPathInfo();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        try {
            String className = "com.vaadin.addon.board.testUI." + name;
            return Class.forName(className).asSubclass(UI.class);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Could not find UI " + name, e);
        }
        return null;
    }

}
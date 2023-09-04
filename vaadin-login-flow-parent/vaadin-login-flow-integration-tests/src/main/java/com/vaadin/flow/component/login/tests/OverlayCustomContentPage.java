package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.Route;

@Route("vaadin-login/custom-content")
public class OverlayCustomContentPage extends Div {
    public static String CUSTOM_FIELD_CONTENT = "__CUSTOM_FIELD_CONTENT__";
    public static String FOOTER_CONTENT = "__FOOTER_CONTENT__";

    public OverlayCustomContentPage() {
        LoginOverlay login = new LoginOverlay();

        NativeButton open = new NativeButton("Open");
        open.setId("open-overlay-btn");
        open.addClickListener(e -> login.setOpened(true));

        Span footerContent = new Span(FOOTER_CONTENT);

        NativeButton addFooter = new NativeButton("Add footer");
        addFooter.setId("add-footer-btn");
        addFooter.addClickListener(e -> login.getFooter().add(footerContent));

        NativeButton removeFooter = new NativeButton("Remove footer");
        removeFooter.setId("remove-footer-btn");
        removeFooter.addClickListener(e -> login.getFooter().removeAll());

        Span customFieldContent = new Span(CUSTOM_FIELD_CONTENT);

        NativeButton addCustomField = new NativeButton("Add custom field");
        addCustomField.setId("add-custom-field-btn");
        addCustomField.addClickListener(
                e -> login.getCustomFields().add(customFieldContent));

        NativeButton removeCustomField = new NativeButton(
                "Remove custom field");
        removeCustomField.setId("remove-custom-field-btn");
        removeCustomField
                .addClickListener(e -> login.getCustomFields().removeAll());

        add(open, addFooter, removeFooter, addCustomField, removeCustomField);
    }
}

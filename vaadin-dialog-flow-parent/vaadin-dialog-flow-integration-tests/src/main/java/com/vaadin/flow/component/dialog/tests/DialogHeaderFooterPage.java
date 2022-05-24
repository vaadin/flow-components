package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/header-footer")
public class DialogHeaderFooterPage extends Div {
    public static String HEADER_TITLE = "__HEADER_TITLE__";
    public static String HEADER_CONTENT = "__HEADER_CONTENT__";
    public static String ANOTHER_HEADER_CONTENT = "__ANOTHER_HEADER_CONTENT__";
    public static String FOOTER_CONTENT = "__FOOTER_CONTENT__";
    public static String ANOTHER_FOOTER_CONTENT = "__ANOTHER_FOOTER_CONTENT__";

    public DialogHeaderFooterPage() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.add("Dialog content");

        NativeButton openDialog = new NativeButton("open dialog",
                e -> dialog.open());
        openDialog.setId("open-dialog-button");

        NativeButton attachDialog = new NativeButton("attach dialog",
                e -> add(dialog));
        attachDialog.setId("attach-dialog-button");

        NativeButton addHeaderTitle = new NativeButton("add header title",
                e -> dialog.setHeaderTitle(HEADER_TITLE));
        addHeaderTitle.setId("add-header-title-button");
        NativeButton removeHeaderTitle = new NativeButton("remove header title",
                e -> dialog.setHeaderTitle(null));
        removeHeaderTitle.setId("remove-header-title-button");

        Span headerContent = new Span(HEADER_CONTENT);
        NativeButton addHeaderContent = new NativeButton("add header content",
                e -> dialog.getHeader().add(headerContent));
        addHeaderContent.setId("add-header-content-button");

        NativeButton removeHeaderContent = new NativeButton(
                "remove header content",
                e -> dialog.getHeader().remove(headerContent));
        removeHeaderContent.setId("remove-header-content-button");

        NativeButton addSecondHeaderContent = new NativeButton(
                "add second header content",
                e -> dialog.getHeader().add(new Span(ANOTHER_HEADER_CONTENT)));
        addSecondHeaderContent.setId("add-second-header-content-button");

        Span footerContent = new Span(FOOTER_CONTENT);
        NativeButton addFooterContent = new NativeButton("add footer content",
                e -> dialog.getFooter().add(footerContent));
        addFooterContent.setId("add-footer-content-button");

        NativeButton removeFooterContent = new NativeButton(
                "remove footer content",
                e -> dialog.getFooter().remove(footerContent));
        removeFooterContent.setId("remove-footer-content-button");

        NativeButton addSecondFooterContent = new NativeButton(
                "add second footer content",
                e -> dialog.getFooter().add(new Span(ANOTHER_FOOTER_CONTENT)));
        addSecondFooterContent.setId("add-second-footer-content-button");

        Div buttonsContainer = new Div(openDialog, attachDialog, addHeaderTitle,
                removeHeaderTitle, addHeaderContent, removeHeaderContent,
                addSecondHeaderContent, addFooterContent, removeFooterContent,
                addSecondFooterContent);

        NativeButton moveButtons = new NativeButton("move buttons",
                e -> dialog.add(buttonsContainer));
        moveButtons.setId("move-buttons-button");
        dialog.add(moveButtons);

        NativeButton closeDialog = new NativeButton("close dialog",
                e -> dialog.setOpened(false));
        closeDialog.setId("close-dialog-button");
        dialog.add(closeDialog);

        add(buttonsContainer);
    }
}

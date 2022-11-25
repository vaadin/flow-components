package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import net.jcip.annotations.NotThreadSafe;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@NotThreadSafe
public class UploadSlotsTest {

    @Before
    public void setup() {
        UI ui = new UI();
        UI.setCurrent(ui);
    }

    @Test
    public void getUploadButton_defaultButtonExists() {
        Upload upload = new Upload();
        Component button = upload.getUploadButton();
        assertEquals("add-button", button.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(button));
    }

    @Test
    public void setUploadButton_buttonIsAdded() {
        Upload upload = new Upload();
        NativeButton button = new NativeButton("Add files");
        upload.setUploadButton(button);
        assertEquals(button, upload.getUploadButton());
        assertEquals("add-button", button.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(button));
    }

    @Test
    public void setUploadButtonNull_defaultButtonIsRestored() {
        Upload upload = new Upload();
        Component defaultButton = upload.getUploadButton();

        NativeButton button = new NativeButton("Add files");
        upload.setUploadButton(button);

        upload.setUploadButton(null);

        assertEquals(defaultButton, upload.getUploadButton());
        assertEquals(upload, getParent(defaultButton));
    }

    @Test
    public void getDropLabel_defaultLabelExists() {
        Upload upload = new Upload();
        Component label = upload.getDropLabel();
        assertEquals("drop-label", label.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(label));
    }

    @Test
    public void setDropLabel_labelIsAdded() {
        Upload upload = new Upload();
        Span label = new Span("Drop files here");
        upload.setDropLabel(label);
        assertEquals(label, upload.getDropLabel());
        assertEquals("drop-label", label.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(label));
    }

    @Test
    public void setDropLabelNull_defaultLabelIsRestored() {
        Upload upload = new Upload();
        Component defaultLabel = upload.getDropLabel();

        Span label = new Span("Drop files here");
        upload.setDropLabel(label);

        upload.setDropLabel(null);

        assertEquals(defaultLabel, upload.getDropLabel());
        assertEquals(upload, getParent(defaultLabel));
    }

    @Test
    public void getDropLabelIcon_defaultIconExists() {
        Upload upload = new Upload();
        Component icon = upload.getDropLabelIcon();
        assertEquals("drop-label-icon", icon.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(icon));
    }

    @Test
    public void setDropLabelIcon_iconIsAdded() {
        Upload upload = new Upload();
        Span icon = new Span("->");
        upload.setDropLabelIcon(icon);
        assertEquals(icon, upload.getDropLabelIcon());
        assertEquals("drop-label-icon", icon.getElement().getAttribute("slot"));
        assertEquals(upload, getParent(icon));
    }

    @Test
    public void setDropLabelIconNull_defaultIconIsRestored() {
        Upload upload = new Upload();
        Component defaultIcon = upload.getDropLabelIcon();

        Span icon = new Span("->");
        upload.setDropLabelIcon(icon);

        upload.setDropLabelIcon(null);

        assertEquals(defaultIcon, upload.getDropLabelIcon());
        assertEquals(upload, getParent(defaultIcon));
    }

    private static Component getParent(Component component) {
        return component.getParent().orElse(null);
    }
}

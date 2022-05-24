package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Test;

@NotThreadSafe
public class UploadDropLabelTest {
    // Regression test for:
    // https://github.com/vaadin/flow-components/issues/3053
    @Test
    public void setLabelAndIcon_updateLabel_doesNotThrow() {
        UI ui = new UI();
        UI.setCurrent(ui);
        Upload upload = new Upload();
        upload.setDropLabel(new Span("Label"));
        upload.setDropLabelIcon(new Span("Icon"));
        upload.setDropLabel(new Span("Updated Label"));
    }
}

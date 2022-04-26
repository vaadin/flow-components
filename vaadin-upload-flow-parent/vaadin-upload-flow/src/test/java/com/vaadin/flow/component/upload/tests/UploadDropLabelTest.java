package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.VaadinSession;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

@NotThreadSafe
public class UploadDropLabelTest {
    @Before
    public void setup() {
        UI ui = new UI();
        UI.setCurrent(ui);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(session);
    }

    // Regression test for:
    // https://github.com/vaadin/flow-components/issues/3053
    @Test
    public void setLabelAndIcon_updateLabel_doesNotThrow() {
        Upload upload = new Upload();
        upload.setDropLabel(new Span("Label"));
        upload.setDropLabelIcon(new Span("Icon"));
        upload.setDropLabel(new Span("Updated Label"));
    }
}

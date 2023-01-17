package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor/as-html-binder")
public class RichTextEditorAsHtmlBinderPage extends Div {
    private TestBean testBean = new TestBean();

    public RichTextEditorAsHtmlBinderPage() {
        RichTextEditor editor = new RichTextEditor();

        Span binderError = new Span();
        binderError.setId("binder-error");

        Span beanValue = new Span();
        beanValue.setId("bean-value");

        Binder<TestBean> binder = new Binder<>();
        binder.forField(editor.asHtml())
                .withValidator(value -> value != null && !value.isEmpty(),
                        "Value should not be empty")
                .bind(TestBean::getValue, TestBean::setValue);

        NativeButton writeBean = new NativeButton("Write bean", e -> {
            try {
                binder.writeBean(testBean);
                binderError.setText("false");
                beanValue.setText(testBean.getValue());
            } catch (ValidationException ex) {
                binderError.setText("true");
                beanValue.setText(testBean.getValue());
            }
        });
        writeBean.setId("write-bean");

        NativeButton readBean = new NativeButton("Read bean", e -> {
            testBean.setValue("<p>foo</p>");
            binder.readBean(testBean);
        });
        readBean.setId("read-bean");

        NativeButton reset = new NativeButton("Reset", e -> {
            binder.readBean(null);
        });
        reset.setId("reset");

        add(editor);
        add(new Div(new Span("Binder error: "), binderError));
        add(new Div(new Span("Bean value: "), beanValue));
        add(new Div(writeBean, readBean, reset));
    }

    private static class TestBean {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

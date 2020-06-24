package com.vaadin.flow.component.select.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;

/*
 * https://github.com/vaadin/vaadin-select-flow/issues/43
 *
 * Test that setting a renderer after detach won't cause
 * an exception on the client side.
 *
 */
@Route(value = "reattach-test")
public class TestReattachView extends Div {

    public TestReattachView() {
        final Select<String> select = new Select<>();
        final SerializableFunction<String, Span> spanProvider = Span::new;
        select.setRenderer(new ComponentRenderer<>(spanProvider));
        select.setItems("a", "b", "c");
        select.setValue("a");
        add(select);

        NativeButton remove = new NativeButton("Remove", event -> {
            remove(select);
            NativeButton add = new NativeButton("Add", ev -> {
                add(select);
                select.setRenderer(new ComponentRenderer<>(spanProvider));
            });
            add(add);
            remove(event.getSource());
        });
        add(remove);
    }
}

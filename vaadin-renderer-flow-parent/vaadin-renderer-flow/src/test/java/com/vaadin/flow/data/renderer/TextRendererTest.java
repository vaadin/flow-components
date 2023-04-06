
package com.vaadin.flow.data.renderer;

import org.junit.Test;

public class TextRendererTest {

    @Test(expected = IllegalStateException.class)
    public void dontAllowNullInLabelGenerator() {
        TextRenderer<Object> renderer = new TextRenderer<>(obj -> null);
        renderer.createComponent(new Object());
    }

}

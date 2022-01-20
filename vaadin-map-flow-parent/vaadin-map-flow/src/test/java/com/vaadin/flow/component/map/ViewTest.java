package com.vaadin.flow.component.map;

import com.vaadin.flow.component.map.configuration.View;
import org.junit.Assert;
import org.junit.Test;

public class ViewTest {

    @Test
    public void  setCenter_doesNotAllowNullValue() {
        View view = new View();

        Assert.assertThrows(NullPointerException.class, () ->  view.setCenter(null));
    }
}

package com.vaadin.flow.component.crud;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CrudTest {

    private Crud systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Crud<>(DummyBean.class, new DummyCrudEditor());
    }

    @Test
    public void onAttach_init() {
        Assert.assertTrue(true);
    }

    public static class DummyCrudEditor extends CrudEditor<DummyBean> {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public Element getView() {
            return new Div().getElement();
        }
    }

    public static class DummyBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
package com.vaadin.flow.component.customfield;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class CustomFieldTest {

    private final Object value = new Object();

    private CustomField<Object> systemUnderTest;

    private Consumer<Object> consumer;

    @Before
    public void setUp() {
        consumer = Mockito.mock(Consumer.class);
        systemUnderTest = new CustomField<Object>() {
            @Override
            protected Object generateModelValue() {
                return value;
            }

            @Override
            protected void setPresentationValue(Object newPresentationValue) {
                consumer.accept(newPresentationValue);
            }
        };
    }

    @Test
    public void valuesAreUpdated() {
        Assert.assertNull(systemUnderTest.getValue());
        systemUnderTest.setValue(value);
        Assert.assertSame(value,systemUnderTest.getValue());
        Mockito.verify(consumer).accept(value);
        Consumer<Object> listener = Mockito.mock(Consumer.class);
        systemUnderTest.addValueChangeListener(e -> listener.accept(e.getValue()));
    }

}

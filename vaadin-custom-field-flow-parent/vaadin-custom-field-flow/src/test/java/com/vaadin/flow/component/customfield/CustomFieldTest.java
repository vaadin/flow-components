package com.vaadin.flow.component.customfield;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

public class CustomFieldTest {

    private final Object value = new Object();

    private CustomField<Object> systemUnderTest;

    private Consumer<Object> consumer;

    @Before
    @SuppressWarnings("unchecked")
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
        systemUnderTest.updateValue();
        Assert.assertSame(value,systemUnderTest.getValue());
        Mockito.verify(consumer).accept(value);
    }
}

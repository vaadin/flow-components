package com.vaadin.flow.component.crud;

import com.vaadin.flow.component.crud.annotation.Hidden;
import com.vaadin.flow.component.crud.annotation.Order;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

public class UtilTest {

    @Test
    public void capitalize() {
        Assert.assertEquals("Name", Util.capitalize("name"));
        Assert.assertEquals("SomeBean", Util.capitalize("someBean"));
        Assert.assertEquals("SOMEBEAN", Util.capitalize("SOMEBEAN"));
        Assert.assertEquals("You and i", Util.capitalize("you and i"));
    }

    @Test
    public void visiblePropertiesIn() {
        final String[] orderedPropertiesGuaranteedFirst = new String[]{"id", "name", "registrationTime"};
        final String[] unorderedPropertiesGuaranteedLast = new String[]{"age", "blacklisted"};
        final String[] hiddenProperties = new String[]{"ssn", "staticProp"};

        String[] fields = Arrays.stream(Util.visiblePropertiesIn(Person.class))
                .map(Field::getName)
                .toArray(String[]::new);

        Assert.assertArrayEquals(orderedPropertiesGuaranteedFirst, Arrays.copyOfRange(fields, 0, 3));

        String[] lastFields = Arrays.copyOfRange(fields, 3, fields.length);
        Arrays.sort(lastFields);

        Assert.assertEquals(unorderedPropertiesGuaranteedLast, lastFields);

        Arrays.stream(hiddenProperties).forEach(e ->
                Assert.assertFalse(Arrays.asList(fields).contains(e)));
    }

    @Test
    public void getterFor() throws NoSuchFieldException, NoSuchMethodException {
        Assert.assertEquals(Person.class.getDeclaredMethod("getName"),
                Util.getterFor(Person.class.getDeclaredField("name"), Person.class));

        Assert.assertEquals(Person.class.getDeclaredMethod("isBlacklisted"),
                Util.getterFor(Person.class.getDeclaredField("blacklisted"), Person.class));
    }

    @Test
    public void orderFor() throws NoSuchFieldException {
        Assert.assertEquals(0,
                Util.orderFor(Person.class.getDeclaredField("id"), Person.class));

        Assert.assertEquals(1,
                Util.orderFor(Person.class.getDeclaredField("name"), Person.class));

        Assert.assertEquals(Integer.MAX_VALUE,
                Util.orderFor(Person.class.getDeclaredField("blacklisted"), Person.class));
    }

    private static class Person {

        @Order
        private long id;

        @Order(1)
        private String name;

        private int age;

        @Order(2)
        private Date registrationTime;

        private boolean blacklisted;

        private static String staticProp = "This should not show";

        @Hidden
        private String ssn;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Date getRegistrationTime() {
            return registrationTime;
        }

        public void setRegistrationTime(Date registrationTime) {
            this.registrationTime = registrationTime;
        }

        public boolean isBlacklisted() {
            return blacklisted;
        }

        public void setBlacklisted(boolean blacklisted) {
            this.blacklisted = blacklisted;
        }

        public String getSsn() {
            return ssn;
        }

        public void setSsn(String ssn) {
            this.ssn = ssn;
        }

        public static String getStaticProp() {
            return staticProp;
        }

        public static void setStaticProp(String staticProp) {
            Person.staticProp = staticProp;
        }
    }
}

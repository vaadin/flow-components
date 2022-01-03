/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class BeanGridSortingTest {

    public static class SortableBean {
        private String string;
        private int integer;
        private boolean bool;
        private double number;
        private Object notComparable;

        private SortableBean innerBean;

        public SortableBean(String string, int integer, boolean bool,
                double number, SortableBean innerBean, Object notComparable) {
            this.string = string;
            this.integer = integer;
            this.bool = bool;
            this.number = number;
            this.innerBean = innerBean;
            this.notComparable = notComparable;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public int getInteger() {
            return integer;
        }

        public void setInteger(int integer) {
            this.integer = integer;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public double getNumber() {
            return number;
        }

        public void setNumber(double number) {
            this.number = number;
        }

        public SortableBean getInnerBean() {
            return innerBean;
        }

        public void setInnerBean(SortableBean innerBean) {
            this.innerBean = innerBean;
        }

        public void setNotComparable(Object notComparable) {
            this.notComparable = notComparable;
        }

        public Object getNotComparable() {
            return notComparable;
        }
    }

    private Grid<SortableBean> grid;

    @Before
    public void init() {
        grid = new Grid<>(SortableBean.class);
        grid.setItems(createBeans());
    }

    @Test
    public void setSortableColumns_onlyTheReferencedColumnsAreSortable() {
        grid.setColumns("string", "integer", "bool", "number");

        Assert.assertTrue(grid.getColumnByKey("string").isSortable());
        Assert.assertTrue(grid.getColumnByKey("integer").isSortable());
        Assert.assertTrue(grid.getColumnByKey("bool").isSortable());
        Assert.assertTrue(grid.getColumnByKey("number").isSortable());

        grid.setSortableColumns("string");

        Assert.assertTrue(grid.getColumnByKey("string").isSortable());
        Assert.assertFalse(grid.getColumnByKey("integer").isSortable());
        Assert.assertFalse(grid.getColumnByKey("bool").isSortable());
        Assert.assertFalse(grid.getColumnByKey("number").isSortable());

        grid.setSortableColumns("bool", "number");

        Assert.assertFalse(grid.getColumnByKey("string").isSortable());
        Assert.assertFalse(grid.getColumnByKey("integer").isSortable());
        Assert.assertTrue(grid.getColumnByKey("bool").isSortable());
        Assert.assertTrue(grid.getColumnByKey("number").isSortable());
    }

    @Test
    public void setSortableColumns_onlyComparablePropertiesAreSortable() {
        Assert.assertTrue(grid.getColumnByKey("string").isSortable());
        Assert.assertFalse(grid.getColumnByKey("notComparable").isSortable());

        grid.setColumns("string", "notComparable", "innerBean",
                "innerBean.string");

        Assert.assertTrue(grid.getColumnByKey("string").isSortable());
        Assert.assertFalse(grid.getColumnByKey("notComparable").isSortable());
        Assert.assertFalse(grid.getColumnByKey("innerBean").isSortable());
        Assert.assertTrue(grid.getColumnByKey("innerBean.string").isSortable());

        grid.addColumn("bool");
        grid.addColumn("innerBean.notComparable");

        Assert.assertTrue(grid.getColumnByKey("bool").isSortable());
        Assert.assertFalse(
                grid.getColumnByKey("innerBean.notComparable").isSortable());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void setSortableColumnsForNonBeanGrid_throws() {
        Grid<SortableBean> nonBeanGrid = new Grid<>();
        nonBeanGrid.setSortableColumns("string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSortableColumnsForNonExistingProperty_throws() {
        grid.setSortableColumns("nonExisting");
    }

    @Test
    public void basicPropertiesAreSortedAsComparables() {
        grid.setColumns("string", "integer", "bool", "number");
        grid.setSortableColumns("string", "integer", "bool", "number");

        callSortersChanged("string", "asc");
        assertInMemorySorting(
                (b1, b2) -> b1.getString().compareTo(b2.getString()));
        callSortersChanged("string", "desc");
        assertInMemorySorting(
                (b1, b2) -> b2.getString().compareTo(b1.getString()));

        callSortersChanged("integer", "asc");
        assertInMemorySorting(
                (b1, b2) -> Integer.compare(b1.getInteger(), b2.getInteger()));
        callSortersChanged("integer", "desc");
        assertInMemorySorting(
                (b1, b2) -> Integer.compare(b2.getInteger(), b1.getInteger()));

        callSortersChanged("bool", "asc");
        assertInMemorySorting(
                (b1, b2) -> Boolean.compare(b1.isBool(), b2.isBool()));
        callSortersChanged("bool", "desc");
        assertInMemorySorting(
                (b1, b2) -> Boolean.compare(b2.isBool(), b1.isBool()));

        callSortersChanged("number", "asc");
        assertInMemorySorting(
                (b1, b2) -> Double.compare(b1.getNumber(), b2.getNumber()));
        callSortersChanged("number", "desc");
        assertInMemorySorting(
                (b1, b2) -> Double.compare(b2.getNumber(), b1.getNumber()));
    }

    @Test
    public void innerPropertiesAreSortedAsComparables() {
        grid.setColumns("innerBean.string", "innerBean.integer",
                "innerBean.bool", "innerBean.number");
        grid.setSortableColumns("innerBean.string", "innerBean.integer",
                "innerBean.bool", "innerBean.number");

        callSortersChanged("innerBean.string", "asc");
        assertInMemorySorting((b1, b2) -> b1.getInnerBean().getString()
                .compareTo(b2.getInnerBean().getString()));
        callSortersChanged("innerBean.string", "desc");
        assertInMemorySorting((b1, b2) -> b2.getInnerBean().getString()
                .compareTo(b1.getInnerBean().getString()));

        callSortersChanged("innerBean.integer", "asc");
        assertInMemorySorting(
                (b1, b2) -> Integer.compare(b1.getInnerBean().getInteger(),
                        b2.getInnerBean().getInteger()));
        callSortersChanged("innerBean.integer", "desc");
        assertInMemorySorting(
                (b1, b2) -> Integer.compare(b2.getInnerBean().getInteger(),
                        b1.getInnerBean().getInteger()));

        callSortersChanged("innerBean.bool", "asc");
        assertInMemorySorting((b1, b2) -> Boolean.compare(
                b1.getInnerBean().isBool(), b2.getInnerBean().isBool()));
        callSortersChanged("innerBean.bool", "desc");
        assertInMemorySorting((b1, b2) -> Boolean.compare(
                b2.getInnerBean().isBool(), b1.getInnerBean().isBool()));

        callSortersChanged("innerBean.number", "asc");
        assertInMemorySorting((b1, b2) -> Double.compare(
                b1.getInnerBean().getNumber(), b2.getInnerBean().getNumber()));
        callSortersChanged("innerBean.number", "desc");
        assertInMemorySorting((b1, b2) -> Double.compare(
                b2.getInnerBean().getNumber(), b1.getInnerBean().getNumber()));
    }

    private List<SortableBean> createBeans() {
        return Arrays.asList(
                new SortableBean("Bean A", 9, false, 9.5,
                        new SortableBean("Sub A", 111, true, 111.5, null,
                                "Not comparable A"),
                        "Not comparable 1"),
                new SortableBean("Bean B", 111, true, 111.5,
                        new SortableBean("Sub B", 1, false, 1.5, null,
                                "Not comparable B"),
                        "Not comparable 2"),
                new SortableBean(
                        "Bean C", 1, false, 1.5, new SortableBean("Sub C", 9,
                                false, 9.5, null, "Not comparable C"),
                        "Not comparable 3"));
    }

    private void assertInMemorySorting(Comparator<SortableBean> comparator) {
        List<SortableBean> expectedOrder = createBeans();
        List<SortableBean> actualOrder = new ArrayList<>(expectedOrder);

        expectedOrder.sort(comparator);
        actualOrder.sort(grid.getDataCommunicator().getInMemorySorting());

        Assert.assertEquals(expectedOrder, actualOrder);
    }

    private void callSortersChanged(String columnId, String direction) {
        try {
            JsonObject json = Json.createObject();
            json.put("path", grid.getColumnByKey(columnId).getInternalId());
            json.put("direction", direction);

            JsonArray array = Json.createArray();
            array.set(0, json);

            Method method = Grid.class.getDeclaredMethod("sortersChanged",
                    JsonArray.class);
            method.setAccessible(true);
            method.invoke(grid, array);
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            Assert.fail("Could not call Grid.sortersChanged: " + e);
        }
    }

}

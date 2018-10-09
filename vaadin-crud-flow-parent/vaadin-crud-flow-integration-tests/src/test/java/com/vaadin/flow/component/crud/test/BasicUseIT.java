package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL()  + "/BasicUse");
    }

    @Test
    public void crudIsPresent() {
        Assert.assertTrue($("vaadin-crud").exists());
        Assert.assertTrue($(GridElement.class).exists());
    }

    @Test
    public void dataPresentInGrid() {
        Assert.assertEquals(3, $(GridElement.class).first().getRowCount());
    }

    @Test
    public void filterEnabledInGrid() {
        $(TextFieldElement.class)
                .attribute("crud-role", "Search").waitForFirst();

        Assert.assertEquals(3, $(TextFieldElement.class)
                .attribute("crud-role", "Search").all().size());
    }

    @Test
    public void filterCanBeDisabled() {
        getDriver().get(getBaseURL() + "/NoFilter");
        Assert.assertFalse($(TextFieldElement.class).attribute("crud-role", "Search").exists());
    }

    @Test
    public void filterValueCorrect() {
        List<TextFieldElement> fields = $(TextFieldElement.class)
                .attribute("crud-role", "Search").all();
        fields.get(0).setValue("Me");
        fields.get(2).setValue("You");

        ButtonElement showFilterButton = getButton("showFilter");
        showFilterButton.click();

        Assert.assertEquals("{firstName=Me, lastName=You}{}", getLastEvent());
    }

    @Test
    @Ignore("Unable to access the sorting controls in Grid")
    public void sortEnabledInGrid() {
        GridElement grid = $(GridElement.class).first();
        Assert.assertTrue(grid.getHeaderCell(1).$("vaadin-grid-sorter").exists());
    }

    @Test
    @Ignore("Unable to access the sorting controls in Grid")
    public void sortOrdersCorrect() {
        GridElement grid = $(GridElement.class).first();
        List<TestBenchElement> sorters = grid.getHeaderCell(1).$("vaadin-grid-sorter").all();

        sorters.get(0).click(); // First name ascending
        sorters.get(2).click(); sorters.get(2).click(); // Last name descending

        ButtonElement showFilterButton = getButton("showFilter");
        showFilterButton.click();

        Assert.assertEquals("{}{lastName=ASCENDING, firstName=ASCENDING}", getLastEvent());
    }

    @Test
    public void i18n() {
        Assert.assertEquals("New item", getNewButton().getText());
        getButton("updateI18n").click();
        Assert.assertEquals("Eeyan titun", getNewButton().getText());
    }

    @Test
    public void footer() {
        Assert.assertEquals("3 items available",
                $("span").onPage().first().getText());
    }
}

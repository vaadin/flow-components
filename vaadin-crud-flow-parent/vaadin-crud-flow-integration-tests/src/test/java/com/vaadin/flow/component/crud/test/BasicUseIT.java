package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
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
        getDriver().get(getBaseURL());
    }

    @Test
    public void crudIsPresent() {
        Assert.assertTrue($(CrudElement.class).exists());
        Assert.assertNotNull($(CrudElement.class).first().getGrid());
    }

    @Test
    public void dataPresentInGrid() {
        Assert.assertEquals(3, $(CrudElement.class).waitForFirst().getGrid().getRowCount());
    }

    @Test
    public void filterEnabledInGrid() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        waitUntil(e -> !crud.getFilterFields().isEmpty());

        Assert.assertEquals(3, crud.getFilterFields().size());
    }

    @Test
    public void filterCanBeDisabled() {
        getDriver().get(getBaseURL() + "/nofilter");
        Assert.assertTrue($(CrudElement.class).waitForFirst().getFilterFields().isEmpty());
    }

    @Test
    public void filterValueCorrect() {
        List<TextFieldElement> fields = $(CrudElement.class).waitForFirst().getFilterFields();
        fields.get(0).setValue("Me");
        fields.get(2).setValue("You");

        ButtonElement showFilterButton = getTestButton("showFilter");
        showFilterButton.click();

        Assert.assertEquals("{firstName=Me, lastName=You}{}", getLastEvent());
    }

    @Test
    @Ignore("Unable to access the sorting controls in Grid")
    public void sortEnabledInGrid() {
        GridElement grid = $(GridElement.class).waitForFirst();
        Assert.assertTrue(grid.getHeaderCell(1).$("vaadin-grid-sorter").exists());
    }

    @Test
    @Ignore("Unable to access the sorting controls in Grid")
    public void sortOrdersCorrect() {
        GridElement grid = $(GridElement.class).waitForFirst();
        List<TestBenchElement> sorters = grid.getHeaderCell(1).$("vaadin-grid-sorter").all();

        sorters.get(0).click(); // First name ascending
        sorters.get(2).click(); sorters.get(2).click(); // Last name descending

        ButtonElement showFilterButton = getTestButton("showFilter");
        showFilterButton.click();

        Assert.assertEquals("{}{lastName=DESCENDING, firstName=ASCENDING}", getLastEvent());
    }

    @Test
    public void i18n() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertEquals("New item", crud.getNewItemButton().getText());
        getTestButton("updateI18n").click();
        Assert.assertEquals("Eeyan titun", crud.getNewItemButton().getText());
        Assert.assertEquals("I18n updated", getLastEvent());
    }

    @Test
    public void footer() {
        Assert.assertEquals("3 items available",
                $("span").onPage().first().getText());
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }
}

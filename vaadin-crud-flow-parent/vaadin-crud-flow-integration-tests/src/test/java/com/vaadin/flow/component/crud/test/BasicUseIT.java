package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class BasicUseIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud");
        getDriver().get(url);
    }

    @Test
    public void crudIsPresent() {
        Assert.assertTrue($(CrudElement.class).exists());
        Assert.assertNotNull($(CrudElement.class).first().getGrid());
    }

    @Test
    public void crudReplacesGrid() {
        getTestButton("addGrid").click();
        List<GridElement> grids = $(GridElement.class).all();
        Assert.assertEquals(1, grids.size());
    }

    @Test
    public void dataPresentInGrid() {
        Assert.assertEquals(3,
                $(CrudElement.class).waitForFirst().getGrid().getRowCount());
    }

    @Test
    public void filterEnabledInGrid() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        waitUntil(e -> !crud.getFilterFields().isEmpty());

        Assert.assertEquals(3, crud.getFilterFields().size());
    }

    @Test
    public void filterCanBeDisabled() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/nofilter";
        getDriver().get(url);
        Assert.assertTrue($(CrudElement.class).waitForFirst().getFilterFields()
                .isEmpty());
    }

    @Test
    public void filterValueCorrect() {
        List<TextFieldElement> fields = $(CrudElement.class).waitForFirst()
                .getFilterFields();
        fields.get(0).setValue("Me");
        fields.get(2).setValue("You");

        ButtonElement showFilterButton = getTestButton("showFilter");
        showFilterButton.click();

        Assert.assertEquals("{firstName=Me, lastName=You}{}", getLastEvent());
    }

    @Test
    public void sortEnabledInGrid() {
        GridElement grid = $(GridElement.class).waitForFirst();
        Assert.assertTrue(grid.getHeaderCellContent(0, 0)
                .$("vaadin-grid-sorter").exists());
    }

    @Test
    public void sorterHasAriaLabel() {
        GridElement grid = $(GridElement.class).waitForFirst();
        Assert.assertEquals("Sort by First Name",
                grid.getHeaderCellContent(0, 0).$("vaadin-grid-sorter").first()
                        .getAttribute("aria-label"));
    }

    @Test
    public void filterHasAriaLabel() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertEquals("Filter by First Name",
                crud.getFilterFields().get(0).getAttribute("aria-label"));
    }

    @Test
    public void sortOrdersCorrect() {
        GridElement grid = $(GridElement.class).waitForFirst();

        TestBenchElement firstNameSorter = grid.getHeaderCellContent(0, 0)
                .$("vaadin-grid-sorter").get(0);
        TestBenchElement lasttNameSorter = grid.getHeaderCellContent(0, 2)
                .$("vaadin-grid-sorter").get(0);

        firstNameSorter.click(); // First name ascending
        lasttNameSorter.click();
        lasttNameSorter.click(); // Last name descending

        ButtonElement showFilterButton = getTestButton("showFilter");
        showFilterButton.click();

        Assert.assertEquals("{}{lastName=DESCENDING, firstName=ASCENDING}",
                getLastEvent());
    }

    @Test
    public void i18n() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertEquals("New item",
                crud.getNewItemButton().get().getText().trim());
        getTestButton("updateI18n").click();
        Assert.assertEquals("Eeyan titun",
                crud.getNewItemButton().get().getText().trim());
        Assert.assertEquals("I18n updated", getLastEvent());
    }

    @Test
    public void footer() {
        Assert.assertEquals("3 items available",
                $("span").onPage().first().getText());
    }

    @Test
    public void crudAndGeneratedGridReactToThemeVariantChanges() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        GridElement grid = $(GridElement.class).first();

        Assert.assertNotEquals("no-border", crud.getAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getAttribute("theme"));

        getTestButton("toggleBorders").click();
        Assert.assertEquals("no-border", crud.getAttribute("theme"));
        Assert.assertEquals("no-border", grid.getAttribute("theme"));

        getTestButton("toggleBorders").click();
        Assert.assertNotEquals("no-border", crud.getAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getAttribute("theme"));
    }

    @Test
    public void toolbarVisibleByDefault() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertNull(crud.getAttribute("no-toolbar"));
    }

    @Test
    public void hideToolbar() {
        CrudElement crud = $(CrudElement.class).waitForFirst();

        ButtonElement hideToolbarButton = getTestButton("hideToolbarButton");
        hideToolbarButton.click();

        TestBenchElement toolbar = crud.$(TestBenchElement.class).id("toolbar");

        Assert.assertEquals("none", toolbar.getCssValue("display"));
    }

    @Test
    public void showToolbar() {
        CrudElement crud = $(CrudElement.class).waitForFirst();

        ButtonElement hideToolbarButton = getTestButton("hideToolbarButton");
        ButtonElement showToolbarButton = getTestButton("showToolbarButton");

        hideToolbarButton.click();
        showToolbarButton.click();

        TestBenchElement toolbar = crud.$(TestBenchElement.class).id("toolbar");
        Assert.assertEquals("flex", toolbar.getCssValue("display"));
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }
}

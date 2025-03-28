/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud")
public class BasicUseIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
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
        String url = getRootURL() + getTestPath() + "/nofilter";
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
                        .getDomAttribute("aria-label"));
    }

    @Test
    public void filterHasAriaLabel() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertEquals("Filter by First Name",
                crud.getFilterFields().get(0).getDomAttribute("aria-label"));
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
    public void defaultButtonTexts() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertEquals("New item",
                crud.getNewItemButton().get().getText().trim());

        crud.openRowForEditing(0);
        Assert.assertEquals("Save",
                crud.getEditorSaveButton().getText().trim());
        Assert.assertEquals("Cancel",
                crud.getEditorCancelButton().getText().trim());
        Assert.assertEquals("Delete...",
                crud.getEditorDeleteButton().getText().trim());
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

        Assert.assertNotEquals("no-border", crud.getDomAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getDomAttribute("theme"));

        getTestButton("toggleBorders").click();
        Assert.assertEquals("no-border", crud.getDomAttribute("theme"));
        Assert.assertEquals("no-border", grid.getDomAttribute("theme"));

        getTestButton("toggleBorders").click();
        Assert.assertNotEquals("no-border", crud.getDomAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getDomAttribute("theme"));
    }

    @Test
    public void toolbarVisibleByDefault() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.hasAttribute("no-toolbar"));
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

    private String getLastEvent() {
        return $(VerticalLayoutElement.class).last().$("span").last().getText();
    }
}

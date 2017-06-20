package com.vaadin.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import com.vaadin.annotations.HtmlImport;
import com.vaadin.board.client.RowState;
import com.vaadin.shared.Connector;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.declarative.DesignContext;

/**
 * Row class to define rows used in a {@link Board} instance.
 * <p>
 * Each Row consists of four columns, and can contain up to four components
 * taking one column each, or fewer components with multiple columns each as
 * long as sum of columns stays less than or equal to four.
 * 
 * <p>
 * One row might also contain a nested row as shown in the following example:
 * 
 * <pre>
 * Board board = new Board();
 * Label lbl1 = createLabel("Label 1");
 * Label lbl2 = createLabel("Label 2");
 * Label lbl3 = createLabel("Label 3");
 * 
 * Label inner1 = createLabel("Inner 1");
 * Label inner3 = createLabel("Inner 3");
 * Label inner4 = createLabel("Inner 4");
 * Label inner2 = createLabel("Inner 2");
 * Row innerRow = new Row(inner1, inner2, inner3, inner4);
 * Row outerRow = board.addRow(lbl1, lbl2, lbl3, innerRow);
 * </pre>
 */
@HtmlImport("frontend://vaadin-board/vaadin-board-row.html")
public class Row extends AbstractComponentContainer {

    protected List<Component> components = new ArrayList<>();

    /**
     * Creates an empty row.
     * <p>
     * Use {@link #addComponent(Component)},
     * {@link #addComponent(Component, int)} or
     * {@link #addComponent(Component, int)} to add content to the row.
     */
    public Row() {
        super();
    }

    /**
     * Creates an new row with the given components.
     * 
     * @param components
     *            initial content of the row
     */
    public Row(Component... components) {
        super();
        addComponents(components);
    }

    private void checkNewColValue(Component component, int cols) {
        Map<Connector, Integer> map = getState().cols;
        int colValueForComponent = map.getOrDefault(component, 0);
        int sum = getState().usedColAmount();
        if ((sum - colValueForComponent + cols) > 4) {
            throw new IllegalStateException("new total amount of cols would be bigger than 4");
        }
    }

    private void checkIfContained(Component component, int cols) {
        Map<Connector, Integer> map = getState().cols;
        if (!map.containsKey(component)) {
            throw new IllegalStateException("try to modify a component that is not in row " + component);
        }
    }

    private void checkIfNotNegative(Component component, int cols) {
        if (cols < 1) {
            throw new IllegalStateException("please , don´t try to add negative values or zero for cols");
        }
    }

    private void checkIfValueSmallerOrEqualFour(Component component, int cols) {
        if (cols > 4) {
            throw new IllegalStateException("max col value you can set is 4");
        }
    }

    /**
     * Adds the given component(s) to the row.
     * <p>
     * All added components are set to use 1 column. Use
     * {@link #setCols(Component, int)} to make a component span multiple
     * columns.
     *
     * @param components
     *            the components to add
     * @throws IllegalStateException
     *             if adding the components would cause the row to have more
     *             than 4 child components
     **/
    @Override
    public void addComponents(Component... components) {
        // Overridden only for javadoc
        super.addComponents(components);
    }

    /**
     * Adds the given component to the row.
     * <p>
     * All added components are set to use 1 column. Use
     * {@link #setCols(Component, int)} to make a component span multiple
     * columns.
     *
     * @param component
     *            the component to add
     * @throws IllegalStateException
     *             if adding the component would cause the row to have more than
     *             4 child components
     **/
    @Override
    public void addComponent(Component component) {
        addComponent(component, 1);
    }

    @Override
    public void removeComponent(Component component) {
        super.removeComponent(component);
        if (components.contains(component)) {
            components.remove(component);
            getState(true).cols.remove(component);
        }
    }

    /**
     * Adds the given component to the row using the given number of columns.
     *
     * @param component
     *            the component to add
     * @param cols
     *            the number of columns the component should use
     * @throws IllegalStateException
     *             if adding the component would cause the row to have more than
     *             4 child components
     **/
    public void addComponent(Component component, int cols) {
        checkIfValueSmallerOrEqualFour(component, cols);
        checkNewColValue(component, cols);
        checkIfNotNegative(component, cols);

        super.addComponent(component);
        components.add(component);
        getState(true).cols.put(component, 1);
        setCols(component, cols);
    }

    /**
     * Gets the number of columns the given component spans.
     *
     * @param component
     *            the child component to get columns for
     * @return the number of columns the component spans, by default 1.
     **/
    public int getCols(Component component) {
        if (getState().cols.containsKey(component)) {
            return getState().cols.get(component);
        } else {
            if (component.getParent() != this) {
                throw new IllegalArgumentException(
                    "The given component is not a child of this row");
            } else {
                return 1;
            }
        }
    }

    ;

    @Override
    protected RowState getState() {
        return (RowState) super.getState();
    }

    @Override
    protected RowState getState(boolean markAsDirty) {
        return (RowState) super.getState(markAsDirty);
    }

    /**
     * Sets the number of columns the given component spans.
     *
     * @param component
     *            the child component to set columns for
     * @param cols
     *            the number of columns the component spans
     * @throws IllegalArgumentException
     *             if the component is not a child component or if the number of
     *             columns is less than 1
     **/
    public void setCols(Component component, int cols) {
        checkIfValueSmallerOrEqualFour(component, cols);
        checkNewColValue(component, cols);
        checkIfNotNegative(component, cols);

        getState(true).cols.put(component, cols);
    }

    @Override
    public void replaceComponent(Component oldComponent,
                                 Component newComponent) {
        throw new UnsupportedOperationException(
            "replaceComponent is not currently supported");
    }

    @Override
    public int getComponentCount() {
        return components.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return Collections.unmodifiableCollection(components).iterator();
    }

    @Override
    public void readDesign(Element design, DesignContext designContext) {
        super.readDesign(design, designContext);
        // handle children
        for (Element childComponent : design.children()) {
            Component child = designContext.readDesign(childComponent);
            int cols = 1;
            for (Attribute attr : childComponent.attributes()) {
                if (attr.getKey().equals(":cols")) {
                    cols = Integer.parseInt(attr.getValue());
                }
            }
            this.addComponent(child, cols);
        }
    }

    /*
     * (non-Javadoc)
     *
     * Setting full height for the row is not supported. Set height for the child components.
     */
    @Override
    public void setSizeFull() {
        super.setSizeFull();
    }

    /*
    * (non-Javadoc)
    *
    * Setting height for the row is not supported. Set height for the child components.
    */
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
    }

    /*
     * (non-Javadoc)
     *
     * Setting height for the row is not supported. Set height for the child components.
     */
    @Override
    public void setHeight(float height, Unit unit) {
        throw new UnsupportedOperationException(
            "Setting height for the row is not supported. Set height for the child components.");
    }

    @Override
    public void writeDesign(Element design, DesignContext designContext) {
        super.writeDesign(design, designContext);
        Iterator<Component> it = iterator();
        while (it.hasNext()) {
            Component comp = it.next();
            Element childElement = designContext.createElement(comp);
            int boardCols = getCols(comp);
            if (boardCols > 1) {
                childElement.attr(":cols", "" + boardCols);
            }
            design.appendChild(childElement);
        }
    }
}

package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.WidgetUtil;

public class SheetTabSheet extends Widget {

    private static final String HIDDEN = "hidden";

    public interface SheetTabSheetHandler {
        public void onSheetTabSelected(int tabIndex);

        public void onSheetRename(int selectedTabIndex, String value);

        public void onNewSheetCreated();

        public void onSheetRenameCancel();

        public void onFirstTabIndexChange(int tabScrollIndex);

        public void onSheetTabSheetFocus();
    }

    private static final String SELECTED_TAB_CLASSNAME = "selected-tab";

    private DivElement root = Document.get().createDivElement();
    // div containing the tabs
    private DivElement container = Document.get().createDivElement();

    private DivElement options = Document.get().createDivElement();

    private DivElement scrollBeginning = Document.get().createDivElement();

    private DivElement scrollEnd = Document.get().createDivElement();

    private DivElement scrollLeft = Document.get().createDivElement();

    private DivElement scrollRight = Document.get().createDivElement();

    private DivElement addNewSheet = Document.get().createDivElement();

    private InputElement input = Document.get().createTextInputElement();

    private DivElement tempElement = Document.get().createDivElement();

    private JsArray<JavaScriptObject> tabs = JsArray.createArray().cast();

    private final SheetTabSheetHandler handler;

    private int selectedTabIndex = -1;

    private int tabScrollIndex;

    private double tabScrollMargin;

    private boolean readOnly;

    private boolean editing;

    private String cachedSheetName = "";

    private DivElement infoLabel = Document.get().createDivElement();

    public SheetTabSheet(SheetTabSheetHandler handler) {
        this.handler = handler;

        initDOM();
        initListeners();

        input.setMaxLength(31);
    }

    private void initDOM() {
        scrollBeginning.setClassName("scroll-tabs-beginning");
        scrollEnd.setClassName("scroll-tabs-end");
        scrollLeft.setClassName("scroll-tabs-left");
        scrollRight.setClassName("scroll-tabs-right");
        addNewSheet.setClassName("add-new-tab");

        options.setClassName("sheet-tabsheet-options");
        options.appendChild(scrollBeginning);
        options.appendChild(scrollLeft);
        options.appendChild(scrollRight);
        options.appendChild(scrollEnd);
        options.appendChild(addNewSheet);

        container.setClassName("sheet-tabsheet-container");

        tempElement.setClassName("sheet-tabsheet-temp");
        root.appendChild(tempElement);

        root.setClassName("sheet-tabsheet");
        root.appendChild(options);
        root.appendChild(container);

        infoLabel.setClassName("sheet-tabsheet-infolabel");
        root.appendChild(infoLabel);

        setElement(root);
    }

    private void initListeners() {
        Event.sinkEvents(root, Event.ONCLICK | Event.ONDBLCLICK);
        Event.setEventListener(root, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                final Element target = event.getEventTarget().cast();
                final int type = event.getTypeInt();
                if (target.equals(input)) {
                    return;
                }
                event.stopPropagation();
                if (type == Event.ONCLICK) {
                    if (editing && !readOnly) {
                        commitSheetName();
                    }
                    handler.onSheetTabSheetFocus();
                    if (options.isOrHasChild(target)
                            && !target.hasClassName(HIDDEN)) {
                        if (target.equals(scrollBeginning)) {
                            tabScrollMargin = 0;
                            tabScrollIndex = 0;
                            container.getStyle().setMarginLeft(tabScrollMargin,
                                    Unit.PX);
                            showHideScrollIcons();
                            handler.onFirstTabIndexChange(tabScrollIndex);
                        } else if (target.equals(scrollLeft)) {
                            if (tabScrollIndex > 0) {
                                tabScrollIndex--;
                                if (tabScrollIndex == 0) {
                                    tabScrollMargin = 0;
                                } else {
                                    tabScrollMargin += getTabWidth(
                                            tabScrollIndex);
                                }
                                container.getStyle().setMarginLeft(
                                        tabScrollMargin, Unit.PX);
                            }
                            showHideScrollIcons();
                            handler.onFirstTabIndexChange(tabScrollIndex);
                        } else if (target.equals(scrollRight)) {
                            if (tabScrollIndex < (tabs.length() - 1)) {
                                tabScrollMargin -= getTabWidth(tabScrollIndex);
                                container.getStyle().setMarginLeft(
                                        tabScrollMargin, Unit.PX);
                                tabScrollIndex++;
                                showHideScrollIcons();
                                handler.onFirstTabIndexChange(tabScrollIndex);
                            }
                        } else if (target.equals(scrollEnd)) {
                            int tempIndex = getLastTabVisibleWithScrollIndex();
                            setFirstVisibleTab(tempIndex);
                            handler.onFirstTabIndexChange(tabScrollIndex);
                        } else if (target.equals(addNewSheet)) {
                            if (!readOnly) {
                                handler.onNewSheetCreated();
                            }
                        }
                    } else if (container.isOrHasChild(target)) {
                        for (int i = 0; i < tabs.length(); i++) {
                            if (tabs.get(i).equals(target)) {
                                if (i != selectedTabIndex) {
                                    handler.onSheetTabSelected(i);
                                }
                            }
                        }
                    }
                } else if (type == Event.ONDBLCLICK) {
                    if (!readOnly) {
                        for (int i = 0; i < tabs.length(); i++) {
                            if (tabs.get(i).equals(target)) {
                                if (i != selectedTabIndex) {
                                    handler.onSheetTabSelected(i);
                                } else {
                                    editing = true;
                                    Element e = tabs.get(i).cast();
                                    cachedSheetName = e.getInnerText();
                                    input.setValue(cachedSheetName);
                                    e.setInnerText("");
                                    e.appendChild(input);
                                    input.focus();
                                    updateInputSize();
                                }
                            }
                        }
                    }
                }
            }

        });
        Event.sinkEvents(input, Event.ONKEYDOWN | Event.ONKEYUP | Event.ONBLUR);
        Event.setEventListener(input, new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                final int type = event.getTypeInt();
                if (editing) {
                    if (type == Event.ONBLUR) {
                        commitSheetName();
                    } else {
                        switch (event.getKeyCode()) {
                        case KeyCodes.KEY_ENTER:
                        case KeyCodes.KEY_TAB:
                            commitSheetName();
                            break;
                        case KeyCodes.KEY_ESCAPE:
                            editing = false;
                            input.removeFromParent();
                            Element element = (Element) tabs
                                    .get(selectedTabIndex).cast();
                            element.getStyle().clearWidth();
                            setTabName(element, cachedSheetName);
                            handler.onSheetRenameCancel();
                            break;
                        default:
                            doDeferredInputSizeUpdate();
                            break;
                        }
                    }
                }
                event.stopPropagation();
            }
        });
    }

    /**
     * Sets the content of the info label.
     *
     * @param value
     *            the new content. Can not be HTML.
     */
    public void setInfoLabelValue(String value) {
        if (value == null) {
            infoLabel.getStyle().setDisplay(Display.NONE);
            container.getStyle().setMarginRight(0, Unit.PX);
        } else {
            container.getStyle().setMarginRight(206, Unit.PX);
            infoLabel.getStyle().setDisplay(Display.INLINE);
            infoLabel.setInnerText(value);
        }
    }

    /**
     * @return current content of the info label.
     */
    public String getInfoLabelValue() {
        return infoLabel.getInnerText();
    }

    private double getTabWidth(int index) {
        Element tab = ((Element) tabs.get(index).cast());
        double result = WidgetUtil
                .getRequiredWidthBoundingClientRectDouble(tab);
        ComputedStyle cs = new ComputedStyle(tab);
        result += cs.getMargin()[1];
        result += cs.getMargin()[3];
        return result;
    }

    private void doDeferredInputSizeUpdate() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                updateInputSize();
            }
        });
    }

    private int getLastTabVisibleWithScrollIndex() {
        return getTabVisibleWithScrollIndex(tabs.length() - 1);
    }

    private int getTabVisibleWithScrollIndex(int tabIndex) {
        int tempWidth = root.getOffsetWidth()
                - ((Element) options.cast()).getOffsetWidth();
        if (!infoLabel.getStyle().getDisplay().equals("none")) {
            tempWidth -= ((Element) infoLabel.cast()).getOffsetWidth();
        }
        tempWidth -= getTabWidth(tabIndex);

        while (tabIndex > 0 && tempWidth - getTabWidth(tabIndex - 1) > 0) {
            tabIndex--;
            tempWidth -= getTabWidth(tabIndex);
        }
        return tabIndex;
    }

    private void updateInputSize() {
        String text = input.getValue();
        if (text.length() > 31) {
            text = text.substring(0, 31);
            input.setValue(text);
        }
        tempElement.setInnerText(text);
        int textWidth = tempElement.getOffsetWidth();
        if (textWidth < 50) {
            textWidth = 50;
        }
        // check that the edited tab doesn't overflow the tab sheet
        Element selectedTab = (Element) tabs.get(selectedTabIndex).cast();
        int rootAbsoluteRight = root.getAbsoluteRight();
        int selectedTabAbsoluteRight = selectedTab.getAbsoluteRight() + 10;
        while (selectedTabAbsoluteRight > rootAbsoluteRight
                && tabScrollIndex < (tabs.length() - 1)) {
            double width = getTabWidth(tabScrollIndex);
            selectedTabAbsoluteRight -= width;
            tabScrollMargin -= width;
            tabScrollIndex++;
        }
        container.getStyle().setMarginLeft(tabScrollMargin, Unit.PX);
        input.getStyle().setWidth(textWidth + 5d, Unit.PX);
        selectedTab.getStyle().setWidth(textWidth, Unit.PX);
    }

    private void commitSheetName() {
        editing = false;
        input.removeFromParent();
        Element selectedTab = tabs.get(selectedTabIndex).cast();
        selectedTab.getStyle().clearWidth();
        String value = input.getValue();
        if (validateSheetName(value) && !cachedSheetName.equals(value)) {
            for (int i = 0; i < tabs.length(); i++) {
                // value cannot be the same as with another sheet
                if (value.equals(
                        ((Element) tabs.get(i).cast()).getInnerText())) {
                    setTabName(selectedTab, cachedSheetName);
                    return;
                }
            }
            handler.onSheetRename(selectedTabIndex, value);
            setTabName(selectedTab, value);
            showHideScrollIcons();
        } else {
            // TODO show error ?
            setTabName(selectedTab, cachedSheetName);
        }
    }

    private boolean validateSheetName(String sheetName) {
        if (sheetName == null) {
            return false;
        }
        int len = sheetName.length();
        if (len < 1 || len > 31) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            char ch = sheetName.charAt(i);
            switch (ch) {
            case '/':
            case '\\':
            case '?':
            case '*':
            case ']':
            case '[':
            case ':':
                return false;
            default:
                // all other chars OK
                continue;
            }
        }
        if (sheetName.charAt(0) == '\'' || sheetName.charAt(len - 1) == '\'') {
            return false;
        }
        return true;
    }

    private Element createTabElement(String tabName) {
        final Element e = Document.get().createDivElement();
        setTabName(e, tabName);
        e.setClassName("sheet-tabsheet-tab");
        return e;
    }

    public void addTabs(String[] tabNames) {
        for (String tabName : tabNames) {
            Element e = createTabElement(tabName);
            container.appendChild(e);
            tabs.push(e);
        }
        showHideScrollIcons();
    }

    public void setTabs(String[] tabNames, boolean clearScrollPosition) {
        // remove unnecessary tabs
        if (clearScrollPosition) {
            container.getStyle().clearMarginLeft();
            tabScrollIndex = 0;
            tabScrollMargin = 0;
        }
        for (int i = tabNames.length; i < tabs.length(); i++) {
            ((Element) tabs.get(i).cast()).removeFromParent();
        }
        tabs.setLength(tabNames.length);
        for (int i = 0; i < tabNames.length; i++) {
            JavaScriptObject jso = tabs.get(i);
            if (jso != null) {
                Element tabElement = (Element) jso.cast();
                setTabName(tabElement, tabNames[i]);
            } else {
                Element newTab = createTabElement(tabNames[i]);
                container.appendChild(newTab);
                tabs.set(i, newTab);
            }
        }
        if (selectedTabIndex >= (tabs.length())) {
            selectedTabIndex = -1;
        }

        showHideScrollIcons();
    }

    public void removeAllTabs() {
        for (int i = 0; i < tabs.length(); i++) {
            Element e = tabs.get(i).cast();
            e.removeFromParent();
        }
        container.getStyle().clearMarginLeft();
        tabs.setLength(0);
        selectedTabIndex = -1;
        tabScrollIndex = 0;
    }

    /**
     *
     * @param sheetIndex
     *            1-based
     */
    public void setSelectedTab(int sheetIndex) {
        if (selectedTabIndex != -1) {
            ((Element) tabs.get(selectedTabIndex).cast())
                    .removeClassName(SELECTED_TAB_CLASSNAME);
        }
        selectedTabIndex = sheetIndex - 1;
        Element selectedTab = ((Element) tabs.get(selectedTabIndex).cast());
        selectedTab.addClassName(SELECTED_TAB_CLASSNAME);
        if (tabScrollIndex > selectedTabIndex) {
            setFirstVisibleTab(selectedTabIndex);
        } else if (root.getAbsoluteRight() < selectedTab.getAbsoluteRight()
                && !editing) {
            int tempIndex = getTabVisibleWithScrollIndex(selectedTabIndex);
            setFirstVisibleTab(tempIndex);
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        addNewSheet.getStyle()
                .setDisplay(readOnly ? Display.NONE : Display.INLINE_BLOCK);
    }

    public void setFirstVisibleTab(int firstVisibleTab) {
        if (tabScrollIndex < firstVisibleTab) {
            do {
                tabScrollMargin -= getTabWidth(tabScrollIndex);
                tabScrollIndex++;
            } while (tabScrollIndex < firstVisibleTab);
            container.getStyle().setMarginLeft(tabScrollMargin, Unit.PX);
        } else if (tabScrollIndex > firstVisibleTab) {
            do {
                tabScrollIndex--;
                tabScrollMargin += getTabWidth(tabScrollIndex);
            } while (tabScrollIndex > firstVisibleTab);
            container.getStyle().setMarginLeft(tabScrollMargin, Unit.PX);
        }
        showHideScrollIcons();
    }

    private void showHideScrollIcons() {
        if (tabScrollIndex == 0) {
            scrollLeft.addClassName(HIDDEN);
            scrollBeginning.addClassName(HIDDEN);
        } else {
            scrollLeft.removeClassName(HIDDEN);
            scrollBeginning.removeClassName(HIDDEN);
        }
        int lastTabVisibleWithScrollIndex = getLastTabVisibleWithScrollIndex();
        if (tabScrollIndex < lastTabVisibleWithScrollIndex) {
            scrollRight.removeClassName(HIDDEN);
            scrollEnd.removeClassName(HIDDEN);
        } else {
            scrollRight.addClassName(HIDDEN);
            scrollEnd.addClassName(HIDDEN);
        }
    }

    /**
     * Set the tab inner text and title to the given name value
     *
     * @param tab
     * @param name
     *            to use
     */
    private void setTabName(Element tab, String name) {
        if (tab == null)
            return;
        tab.setInnerText(name);
        tab.setTitle(name);
    }

    public void onWidgetResize() {
        // check if we need to display scroll buttons
        showHideScrollIcons();
    }
}

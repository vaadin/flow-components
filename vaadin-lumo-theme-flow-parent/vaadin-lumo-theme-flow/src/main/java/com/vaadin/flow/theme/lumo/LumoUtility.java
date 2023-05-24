/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.theme.lumo;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Contains the definition for all the CSS utility classes provided by Lumo.
 * Importing this class somewhere will import all Lumo CSS utility classes and
 * define them in the global namespace.
 * <p>
 * Note: It is possible that Lumo CSS utilitity classes are conflicting with
 * user-defined CSS classes
 */
@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = "24.1.0-beta2")
@JsModule("@vaadin/vaadin-lumo-styles/utility-global.js")
public final class LumoUtility {

    // The values in the inner classes are wrapped with this method in order to
    // have implicit back reference to the outer class from them. This will
    // ensure that @JsModule and @NpmPackage annotations are picked by the byte
    // code scanner when doing a production mode build.
    private static String notConstant(String value) {
        return value;
    }

    private LumoUtility() {
    }

    /**
     * Accessibility related classes.
     */
    public static final class Accessibility {

        /**
         * Hides content visually while keeping it available to screen readers.
         */
        public static final String SCREEN_READER_ONLY = notConstant("sr-only");

        private Accessibility() {
        }

    }

    /**
     * Classes for distributing space around and between items along a flexbox’s
     * cross axis or a grid’s block axis. Applies to flexbox and grid layouts.
     */
    public static final class AlignContent {

        public static final String AROUND = notConstant("content-around");
        public static final String BETWEEN = notConstant("content-between");
        public static final String CENTER = notConstant("content-center");
        public static final String END = notConstant("content-end");
        public static final String EVENLY = notConstant("content-evenly");
        public static final String START = notConstant("content-start");
        public static final String STRETCH = notConstant("content-stretch");

        private AlignContent() {
        }

    }

    /**
     * Classes for aligning items along a flexbox’s cross axis or a grid’s block
     * axis. Applies to flexbox and grid layouts.
     */
    public static final class AlignItems {

        public static final String BASELINE = notConstant("items-baseline");
        public static final String CENTER = notConstant("items-center");
        public static final String END = notConstant("items-end");
        public static final String START = notConstant("items-start");
        public static final String STRETCH = notConstant("items-stretch");

        private AlignItems() {
        }

    }

    /**
     * Classes for overriding individual items' align-item property. Applies to
     * flexbox and grid items.
     */
    public static final class AlignSelf {

        public static final String AUTO = notConstant("self-auto");
        public static final String BASELINE = notConstant("self-baseline");
        public static final String CENTER = notConstant("self-center");
        public static final String END = notConstant("self-end");
        public static final String START = notConstant("self-start");
        public static final String STRETCH = notConstant("self-stretch");

        private AlignSelf() {
        }

    }

    /**
     * Classes for applying a background color.
     */
    public static final class Background {

        public static final String BASE = notConstant("bg-base");
        public static final String TRANSPARENT = notConstant("bg-transparent");

        public static final String CONTRAST = notConstant("bg-contrast");
        public static final String CONTRAST_90 = notConstant("bg-contrast-90");
        public static final String CONTRAST_80 = notConstant("bg-contrast-80");
        public static final String CONTRAST_70 = notConstant("bg-contrast-70");
        public static final String CONTRAST_60 = notConstant("bg-contrast-60");
        public static final String CONTRAST_50 = notConstant("bg-contrast-50");
        public static final String CONTRAST_40 = notConstant("bg-contrast-40");
        public static final String CONTRAST_30 = notConstant("bg-contrast-30");
        public static final String CONTRAST_20 = notConstant("bg-contrast-20");
        public static final String CONTRAST_10 = notConstant("bg-contrast-10");
        public static final String CONTRAST_5 = notConstant("bg-contrast-5");

        public static final String PRIMARY = notConstant("bg-primary");
        public static final String PRIMARY_50 = notConstant("bg-primary-50");
        public static final String PRIMARY_10 = notConstant("bg-primary-10");

        public static final String ERROR = notConstant("bg-error");
        public static final String ERROR_50 = notConstant("bg-error-50");
        public static final String ERROR_10 = notConstant("bg-error-10");

        public static final String SUCCESS = notConstant("bg-success");
        public static final String SUCCESS_50 = notConstant("bg-success-50");
        public static final String SUCCESS_10 = notConstant("bg-success-10");

        private Background() {
        }

    }

    /**
     * Border-related classes.
     */
    public static final class Border {

        public static final String NONE = notConstant("border-0");

        public static final String ALL = notConstant("border");
        public static final String BOTTOM = notConstant("border-b");
        public static final String LEFT = notConstant("border-l");
        public static final String RIGHT = notConstant("border-r");
        public static final String TOP = notConstant("border-t");

        private Border() {
        }

    }

    /**
     * Classes for setting the border color of an element.
     */
    public static final class BorderColor {

        public static final String CONTRAST = notConstant("border-contrast");
        public static final String CONTRAST_90 = notConstant(
                "border-contrast-90");
        public static final String CONTRAST_80 = notConstant(
                "border-contrast-80");
        public static final String CONTRAST_70 = notConstant(
                "border-contrast-70");
        public static final String CONTRAST_60 = notConstant(
                "border-contrast-60");
        public static final String CONTRAST_50 = notConstant(
                "border-contrast-50");
        public static final String CONTRAST_40 = notConstant(
                "border-contrast-40");
        public static final String CONTRAST_30 = notConstant(
                "border-contrast-30");
        public static final String CONTRAST_20 = notConstant(
                "border-contrast-20");
        public static final String CONTRAST_10 = notConstant(
                "border-contrast-10");
        public static final String CONTRAST_5 = notConstant(
                "border-contrast-5");

        public static final String PRIMARY = notConstant("border-primary");
        public static final String PRIMARY_50 = notConstant(
                "border-primary-50");
        public static final String PRIMARY_10 = notConstant(
                "border-primary-10");

        public static final String ERROR = notConstant("border-error");
        public static final String ERROR_50 = notConstant("border-error-50");
        public static final String ERROR_10 = notConstant("border-error-10");

        public static final String SUCCESS = notConstant("border-success");
        public static final String SUCCESS_50 = notConstant(
                "border-success-50");
        public static final String SUCCESS_10 = notConstant(
                "border-success-10");

        private BorderColor() {
        }

    }

    /**
     * Classes for setting the border radius of an element.
     */
    public static final class BorderRadius {

        public static final String NONE = notConstant("rounded-none");
        public static final String SMALL = notConstant("rounded-s");
        public static final String MEDIUM = notConstant("rounded-m");
        public static final String LARGE = notConstant("rounded-l");

        private BorderRadius() {
        }

    }

    /**
     * Classes for applying a box shadow.
     */
    public static final class BoxShadow {

        public static final String XSMALL = notConstant("shadow-xs");
        public static final String SMALL = notConstant("shadow-s");
        public static final String MEDIUM = notConstant("shadow-m");
        public static final String LARGE = notConstant("shadow-l");
        public static final String XLARGE = notConstant("shadow-xl");

        private BoxShadow() {
        }

    }

    /**
     * Classes for setting the box sizing property of an element. Box sizing
     * determines whether an element’s border and padding is considered a part
     * of its size.
     */
    public static final class BoxSizing {

        public static final String BORDER = notConstant("box-border");
        public static final String CONTENT = notConstant("box-content");

        private BoxSizing() {
        }

    }

    /**
     * Classes for setting the display property of an element. Determines
     * whether the element is a block or inline element and how its items are
     * laid out.
     */
    public static final class Display {

        public static final String BLOCK = notConstant("block");
        public static final String FLEX = notConstant("flex");
        public static final String GRID = notConstant("grid");
        public static final String HIDDEN = notConstant("hidden");
        public static final String INLINE = notConstant("inline");
        public static final String INLINE_BLOCK = notConstant("inline-block");
        public static final String INLINE_FLEX = notConstant("inline-flex");
        public static final String INLINE_GRID = notConstant("inline-grid");

        private Display() {
        }

        /**
         * Set of classes with styles to be used for certain viewport sizes
         */
        public static final class Breakpoint {

            private Breakpoint() {
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 640px
             */
            public static final class Small {

                public static final String FLEX = notConstant("sm:flex");
                public static final String HIDDEN = notConstant("sm:hidden");

                private Small() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 768px
             */
            public static final class Medium {

                public static final String FLEX = notConstant("md:flex");
                public static final String HIDDEN = notConstant("md:hidden");

                private Medium() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 1024px
             */
            public static final class Large {

                public static final String FLEX = notConstant("lg:flex");
                public static final String HIDDEN = notConstant("lg:hidden");

                private Large() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 1280px
             */
            public static final class XLarge {

                public static final String FLEX = notConstant("xl:flex");
                public static final String HIDDEN = notConstant("xl:hidden");

                private XLarge() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 1536px
             */
            public static final class XXLarge {

                public static final String FLEX = notConstant("2xl:flex");
                public static final String HIDDEN = notConstant("2xl:hidden");

                private XXLarge() {
                }
            }
        }
    }

    /**
     * Classes for setting how items grow and shrink in a flexbox layout.
     * Applies to flexbox items.
     */
    public static final class Flex {

        public static final String AUTO = notConstant("flex-auto");
        public static final String NONE = notConstant("flex-none");

        public static final String GROW = notConstant("flex-grow");
        public static final String GROW_NONE = notConstant("flex-grow-0");

        public static final String SHRINK = notConstant("flex-shrink");
        public static final String SHRINK_NONE = notConstant("flex-shrink-0");

        private Flex() {
        }

    }

    /**
     * Classes for setting the flex direction of a flexbox layout.
     */
    public static final class FlexDirection {

        public static final String COLUMN = notConstant("flex-col");
        public static final String COLUMN_REVERSE = notConstant(
                "flex-col-reverse");
        public static final String ROW = notConstant("flex-row");
        public static final String ROW_REVERSE = notConstant(
                "flex-row-reverse");

        private FlexDirection() {
        }

        /**
         * Set of classes defining the flex direction of an element that will be
         * applied only for certain viewport sizes.
         */
        public static final class Breakpoint {

            private Breakpoint() {
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 640px.
             */
            public static final class Small {

                public static final String COLUMN = notConstant("sm:flex-col");
                public static final String ROW = notConstant("sm:flex-row");

                private Small() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 768px.
             */
            public static final class Medium {

                public static final String COLUMN = notConstant("md:flex-col");
                public static final String ROW = notConstant("md:flex-row");

                private Medium() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1024px.
             */
            public static final class Large {

                public static final String COLUMN = notConstant("lg:flex-col");
                public static final String ROW = notConstant("lg:flex-row");

                private Large() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1280px.
             */
            public static final class XLarge {

                public static final String COLUMN = notConstant("xl:flex-col");
                public static final String ROW = notConstant("xl:flex-row");

                private XLarge() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1536px.
             */
            public static final class XXLarge {

                public static final String COLUMN = notConstant("2xl:flex-col");
                public static final String ROW = notConstant("2xl:flex-row");

                private XXLarge() {
                }
            }

        }

    }

    /**
     * Classes for setting how items wrap in a flexbox layout. Applies to
     * flexbox layouts.
     */
    public static final class FlexWrap {

        public static final String NOWRAP = notConstant("flex-nowrap");
        public static final String WRAP = notConstant("flex-wrap");
        public static final String WRAP_REVERSE = notConstant(
                "flex-wrap-reverse");

        private FlexWrap() {
        }

    }

    /**
     * Classes for setting the font size of an element.
     */
    public static final class FontSize {

        public static final String XXSMALL = notConstant("text-2xs");
        public static final String XSMALL = notConstant("text-xs");
        public static final String SMALL = notConstant("text-s");
        public static final String MEDIUM = notConstant("text-m");
        public static final String LARGE = notConstant("text-l");
        public static final String XLARGE = notConstant("text-xl");
        public static final String XXLARGE = notConstant("text-2xl");
        public static final String XXXLARGE = notConstant("text-3xl");

        private FontSize() {
        }

        /**
         * Set of classes defining the font size of an element that will be
         * applied only for certain viewport sizes.
         */
        public static final class Breakpoint {

            private Breakpoint() {
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 640px.
             */
            public static final class Small {

                public static final String XXSMALL = notConstant("sm:text-2xs");
                public static final String XSMALL = notConstant("sm:text-xs");
                public static final String SMALL = notConstant("sm:text-s");
                public static final String MEDIUM = notConstant("sm:text-m");
                public static final String LARGE = notConstant("sm:text-l");
                public static final String XLARGE = notConstant("sm:text-xl");
                public static final String XXLARGE = notConstant("sm:text-2xl");
                public static final String XXXLARGE = notConstant(
                        "sm:text-3xl");

                private Small() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 768px.
             */
            public static final class Medium {

                public static final String XXSMALL = notConstant("md:text-2xs");
                public static final String XSMALL = notConstant("md:text-xs");
                public static final String SMALL = notConstant("md:text-s");
                public static final String MEDIUM = notConstant("md:text-m");
                public static final String LARGE = notConstant("md:text-l");
                public static final String XLARGE = notConstant("md:text-xl");
                public static final String XXLARGE = notConstant("md:text-2xl");
                public static final String XXXLARGE = notConstant(
                        "md:text-3xl");

                private Medium() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1024px.
             */
            public static final class Large {

                public static final String XXSMALL = notConstant("lg:text-2xs");
                public static final String XSMALL = notConstant("lg:text-xs");
                public static final String SMALL = notConstant("lg:text-s");
                public static final String MEDIUM = notConstant("lg:text-m");
                public static final String LARGE = notConstant("lg:text-l");
                public static final String XLARGE = notConstant("lg:text-xl");
                public static final String XXLARGE = notConstant("lg:text-2xl");
                public static final String XXXLARGE = notConstant(
                        "lg:text-3xl");

                private Large() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1280px.
             */
            public static final class XLarge {

                public static final String XXSMALL = notConstant("xl:text-2xs");
                public static final String XSMALL = notConstant("xl:text-xs");
                public static final String SMALL = notConstant("xl:text-s");
                public static final String MEDIUM = notConstant("xl:text-m");
                public static final String LARGE = notConstant("xl:text-l");
                public static final String XLARGE = notConstant("xl:text-xl");
                public static final String XXLARGE = notConstant("xl:text-2xl");
                public static final String XXXLARGE = notConstant(
                        "xl:text-3xl");

                private XLarge() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1536px.
             */
            public static final class XXLarge {

                public static final String XXSMALL = notConstant(
                        "2xl:text-2xs");
                public static final String XSMALL = notConstant("2xl:text-xs");
                public static final String SMALL = notConstant("2xl:text-s");
                public static final String MEDIUM = notConstant("2xl:text-m");
                public static final String LARGE = notConstant("2xl:text-l");
                public static final String XLARGE = notConstant("2xl:text-xl");
                public static final String XXLARGE = notConstant(
                        "2xl:text-2xl");
                public static final String XXXLARGE = notConstant(
                        "2xl:text-3xl");

                private XXLarge() {
                }
            }
        }
    }

    /**
     * Classes for setting the font weight of an element.
     */
    public static final class FontWeight {

        public static final String THIN = notConstant("font-thin");
        public static final String EXTRALIGHT = notConstant("font-extralight");
        public static final String LIGHT = notConstant("font-light");
        public static final String NORMAL = notConstant("font-normal");
        public static final String MEDIUM = notConstant("font-medium");
        public static final String SEMIBOLD = notConstant("font-semibold");
        public static final String BOLD = notConstant("font-bold");
        public static final String EXTRABOLD = notConstant("font-extrabold");
        public static final String BLACK = notConstant("font-black");

        private FontWeight() {
        }
    }

    /**
     * Classes for defining the space between items in a flexbox or grid layout.
     * Applies to flexbox and grid layouts.
     */
    public static final class Gap {

        public static final String XSMALL = notConstant("gap-xs");
        public static final String SMALL = notConstant("gap-s");
        public static final String MEDIUM = notConstant("gap-m");
        public static final String LARGE = notConstant("gap-l");
        public static final String XLARGE = notConstant("gap-xl");

        private Gap() {
        }

        /**
         * Classes for defining the horizontal space between items in a flexbox
         * or grid layout. Applies to flexbox and grid layouts.
         */
        public static final class Column {

            public static final String XSMALL = notConstant("gap-x-xs");
            public static final String SMALL = notConstant("gap-x-s");
            public static final String MEDIUM = notConstant("gap-x-m");
            public static final String LARGE = notConstant("gap-x-l");
            public static final String XLARGE = notConstant("gap-x-xl");

            private Column() {
            }
        }

        /**
         * Classes for defining the vertical space between items in a flexbox or
         * grid layout. Applies to flexbox and grid layouts.
         */
        public static final class Row {

            public static final String XSMALL = notConstant("gap-y-xs");
            public static final String SMALL = notConstant("gap-y-s");
            public static final String MEDIUM = notConstant("gap-y-m");
            public static final String LARGE = notConstant("gap-y-l");
            public static final String XLARGE = notConstant("gap-y-xl");

            private Row() {
            }
        }
    }

    /**
     * Set of classes to define the content flow on a grid layout.
     */
    public static final class Grid {

        /**
         * Items are placed by filling each column in turn, adding new columns
         * as necessary.
         */
        public static final String FLOW_COLUMN = notConstant("grid-flow-col");
        /**
         * Items are placed by filling each row in turn, adding new rows as
         * necessary.
         */
        public static final String FLOW_ROW = notConstant("grid-flow-row");

        private Grid() {
        }

        /**
         * Classes for setting the number of columns in a grid layout.
         */
        public static final class Column {

            public static final String COLUMNS_1 = notConstant("grid-cols-1");
            public static final String COLUMNS_2 = notConstant("grid-cols-2");
            public static final String COLUMNS_3 = notConstant("grid-cols-3");
            public static final String COLUMNS_4 = notConstant("grid-cols-4");
            public static final String COLUMNS_5 = notConstant("grid-cols-5");
            public static final String COLUMNS_6 = notConstant("grid-cols-6");
            public static final String COLUMNS_7 = notConstant("grid-cols-7");
            public static final String COLUMNS_8 = notConstant("grid-cols-8");
            public static final String COLUMNS_9 = notConstant("grid-cols-9");
            public static final String COLUMNS_10 = notConstant("grid-cols-10");
            public static final String COLUMNS_11 = notConstant("grid-cols-11");
            public static final String COLUMNS_12 = notConstant("grid-cols-12");

            public static final String COLUMN_SPAN_1 = notConstant(
                    "col-span-1");
            public static final String COLUMN_SPAN_2 = notConstant(
                    "col-span-2");
            public static final String COLUMN_SPAN_3 = notConstant(
                    "col-span-3");
            public static final String COLUMN_SPAN_4 = notConstant(
                    "col-span-4");
            public static final String COLUMN_SPAN_5 = notConstant(
                    "col-span-5");
            public static final String COLUMN_SPAN_6 = notConstant(
                    "col-span-6");
            public static final String COLUMN_SPAN_7 = notConstant(
                    "col-span-7");
            public static final String COLUMN_SPAN_8 = notConstant(
                    "col-span-8");
            public static final String COLUMN_SPAN_9 = notConstant(
                    "col-span-9");
            public static final String COLUMN_SPAN_10 = notConstant(
                    "col-span-10");
            public static final String COLUMN_SPAN_11 = notConstant(
                    "col-span-11");
            public static final String COLUMN_SPAN_12 = notConstant(
                    "col-span-12");

            private Column() {
            }

        }

        /**
         * Classes for setting the number of rows in a grid layout.
         */
        public static final class Row {

            public static final String ROWS_1 = notConstant("grid-rows-1");
            public static final String ROWS_2 = notConstant("grid-rows-2");
            public static final String ROWS_3 = notConstant("grid-rows-3");
            public static final String ROWS_4 = notConstant("grid-rows-4");
            public static final String ROWS_5 = notConstant("grid-rows-5");
            public static final String ROWS_6 = notConstant("grid-rows-6");

            public static final String ROW_SPAN_1 = notConstant("row-span-1");
            public static final String ROW_SPAN_2 = notConstant("row-span-2");
            public static final String ROW_SPAN_3 = notConstant("row-span-3");
            public static final String ROW_SPAN_4 = notConstant("row-span-4");
            public static final String ROW_SPAN_5 = notConstant("row-span-5");
            public static final String ROW_SPAN_6 = notConstant("row-span-6");

            private Row() {
            }

        }

        /**
         * Set of classes defining the number of columns in a grid layout that
         * will be applied only for certain viewport sizes.
         */
        public static final class Breakpoint {

            private Breakpoint() {
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 640px.
             */
            public static final class Small {

                public static final String COLUMNS_1 = notConstant(
                        "sm:grid-cols-1");
                public static final String COLUMNS_2 = notConstant(
                        "sm:grid-cols-2");
                public static final String COLUMNS_3 = notConstant(
                        "sm:grid-cols-3");
                public static final String COLUMNS_4 = notConstant(
                        "sm:grid-cols-4");
                public static final String COLUMNS_5 = notConstant(
                        "sm:grid-cols-5");
                public static final String COLUMNS_6 = notConstant(
                        "sm:grid-cols-6");
                public static final String COLUMNS_7 = notConstant(
                        "sm:grid-cols-7");
                public static final String COLUMNS_8 = notConstant(
                        "sm:grid-cols-8");
                public static final String COLUMNS_9 = notConstant(
                        "sm:grid-cols-9");
                public static final String COLUMNS_10 = notConstant(
                        "sm:grid-cols-10");
                public static final String COLUMNS_11 = notConstant(
                        "sm:grid-cols-11");
                public static final String COLUMNS_12 = notConstant(
                        "sm:grid-cols-12");

                private Small() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 768px.
             */
            public static final class Medium {

                public static final String COLUMNS_1 = notConstant(
                        "md:grid-cols-1");
                public static final String COLUMNS_2 = notConstant(
                        "md:grid-cols-2");
                public static final String COLUMNS_3 = notConstant(
                        "md:grid-cols-3");
                public static final String COLUMNS_4 = notConstant(
                        "md:grid-cols-4");
                public static final String COLUMNS_5 = notConstant(
                        "md:grid-cols-5");
                public static final String COLUMNS_6 = notConstant(
                        "md:grid-cols-6");
                public static final String COLUMNS_7 = notConstant(
                        "md:grid-cols-7");
                public static final String COLUMNS_8 = notConstant(
                        "md:grid-cols-8");
                public static final String COLUMNS_9 = notConstant(
                        "md:grid-cols-9");
                public static final String COLUMNS_10 = notConstant(
                        "md:grid-cols-10");
                public static final String COLUMNS_11 = notConstant(
                        "md:grid-cols-11");
                public static final String COLUMNS_12 = notConstant(
                        "md:grid-cols-12");

                private Medium() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1024px.
             */
            public static final class Large {

                public static final String COLUMNS_1 = notConstant(
                        "lg:grid-cols-1");
                public static final String COLUMNS_2 = notConstant(
                        "lg:grid-cols-2");
                public static final String COLUMNS_3 = notConstant(
                        "lg:grid-cols-3");
                public static final String COLUMNS_4 = notConstant(
                        "lg:grid-cols-4");
                public static final String COLUMNS_5 = notConstant(
                        "lg:grid-cols-5");
                public static final String COLUMNS_6 = notConstant(
                        "lg:grid-cols-6");
                public static final String COLUMNS_7 = notConstant(
                        "lg:grid-cols-7");
                public static final String COLUMNS_8 = notConstant(
                        "lg:grid-cols-8");
                public static final String COLUMNS_9 = notConstant(
                        "lg:grid-cols-9");
                public static final String COLUMNS_10 = notConstant(
                        "lg:grid-cols-10");
                public static final String COLUMNS_11 = notConstant(
                        "lg:grid-cols-11");
                public static final String COLUMNS_12 = notConstant(
                        "lg:grid-cols-12");

                private Large() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1280px.
             */
            public static final class XLarge {

                public static final String COLUMNS_1 = notConstant(
                        "xl:grid-cols-1");
                public static final String COLUMNS_2 = notConstant(
                        "xl:grid-cols-2");
                public static final String COLUMNS_3 = notConstant(
                        "xl:grid-cols-3");
                public static final String COLUMNS_4 = notConstant(
                        "xl:grid-cols-4");
                public static final String COLUMNS_5 = notConstant(
                        "xl:grid-cols-5");
                public static final String COLUMNS_6 = notConstant(
                        "xl:grid-cols-6");
                public static final String COLUMNS_7 = notConstant(
                        "xl:grid-cols-7");
                public static final String COLUMNS_8 = notConstant(
                        "xl:grid-cols-8");
                public static final String COLUMNS_9 = notConstant(
                        "xl:grid-cols-9");
                public static final String COLUMNS_10 = notConstant(
                        "xl:grid-cols-10");
                public static final String COLUMNS_11 = notConstant(
                        "xl:grid-cols-11");
                public static final String COLUMNS_12 = notConstant(
                        "xl:grid-cols-12");

                private XLarge() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1536px.
             */
            public static final class XXLarge {

                public static final String COLUMNS_1 = notConstant(
                        "2xl:grid-cols-1");
                public static final String COLUMNS_2 = notConstant(
                        "2xl:grid-cols-2");
                public static final String COLUMNS_3 = notConstant(
                        "2xl:grid-cols-3");
                public static final String COLUMNS_4 = notConstant(
                        "2xl:grid-cols-4");
                public static final String COLUMNS_5 = notConstant(
                        "2xl:grid-cols-5");
                public static final String COLUMNS_6 = notConstant(
                        "2xl:grid-cols-6");
                public static final String COLUMNS_7 = notConstant(
                        "2xl:grid-cols-7");
                public static final String COLUMNS_8 = notConstant(
                        "2xl:grid-cols-8");
                public static final String COLUMNS_9 = notConstant(
                        "2xl:grid-cols-9");
                public static final String COLUMNS_10 = notConstant(
                        "2xl:grid-cols-10");
                public static final String COLUMNS_11 = notConstant(
                        "2xl:grid-cols-11");
                public static final String COLUMNS_12 = notConstant(
                        "2xl:grid-cols-12");

                private XXLarge() {
                }
            }

        }

    }

    /**
     * Classes for defining the height of an element.
     */
    public static final class Height {

        public static final String AUTO = notConstant("h-auto");
        public static final String FULL = notConstant("h-full");
        public static final String NONE = notConstant("h-0");
        public static final String SCREEN = notConstant("h-screen");

        public static final String XSMALL = notConstant("h-xs");
        public static final String SMALL = notConstant("h-s");
        public static final String MEDIUM = notConstant("h-m");
        public static final String LARGE = notConstant("h-l");
        public static final String XLARGE = notConstant("h-xl");

        private Height() {
        }

    }

    /**
     * Classes for defining the size of elements used as icons.
     */
    public static final class IconSize {

        public static final String SMALL = notConstant("icon-s");
        public static final String MEDIUM = notConstant("icon-m");
        public static final String LARGE = notConstant("icon-l");

        private IconSize() {
        }

    }

    /**
     * Classes for aligning items along a flexbox’s main axis or a grid’s inline
     * axis. Applies to flexbox and grid layouts.
     */
    public static final class JustifyContent {

        public static final String AROUND = notConstant("justify-around");
        public static final String BETWEEN = notConstant("justify-between");
        public static final String CENTER = notConstant("justify-center");
        public static final String END = notConstant("justify-end");
        public static final String EVENLY = notConstant("justify-evenly");
        public static final String START = notConstant("justify-start");

        private JustifyContent() {
        }

    }

    /**
     * Classes for setting the line height of an element.
     */
    public static final class LineHeight {

        public static final String NONE = notConstant("leading-none");
        public static final String XSMALL = notConstant("leading-xs");
        public static final String SMALL = notConstant("leading-s");
        public static final String MEDIUM = notConstant("leading-m");

        private LineHeight() {
        }

    }

    /**
     * Class for removing the default look of a list.
     */
    public static final class ListStyleType {

        public static final String NONE = notConstant("list-none");

        private ListStyleType() {
        }

    }

    /**
     * Classes for setting the margin of an element.
     */
    public static final class Margin {

        public static final String AUTO = notConstant("m-auto");
        public static final String NONE = notConstant("m-0");
        public static final String XSMALL = notConstant("m-xs");
        public static final String SMALL = notConstant("m-s");
        public static final String MEDIUM = notConstant("m-m");
        public static final String LARGE = notConstant("m-l");
        public static final String XLARGE = notConstant("m-xl");

        private Margin() {
        }

        /**
         * Classes for setting the bottom margin of an element.
         */
        public static final class Bottom {

            public static final String AUTO = notConstant("mb-auto");
            public static final String NONE = notConstant("mb-0");
            public static final String XSMALL = notConstant("mb-xs");
            public static final String SMALL = notConstant("mb-s");
            public static final String MEDIUM = notConstant("mb-m");
            public static final String LARGE = notConstant("mb-l");
            public static final String XLARGE = notConstant("mb-xl");

            private Bottom() {
            }
        }

        /**
         * Classes for setting the logical inline end margin of an element. The
         * actual physical edge where the styles are applied depends on the text
         * flow of the element.
         */
        public static final class End {

            public static final String AUTO = notConstant("me-auto");
            public static final String NONE = notConstant("me-0");
            public static final String XSMALL = notConstant("me-xs");
            public static final String SMALL = notConstant("me-s");
            public static final String MEDIUM = notConstant("me-m");
            public static final String LARGE = notConstant("me-l");
            public static final String XLARGE = notConstant("me-xl");

            private End() {
            }
        }

        /**
         * Classes for setting both the left and the right margins an element.
         */
        public static final class Horizontal {

            public static final String AUTO = notConstant("mx-auto");
            public static final String NONE = notConstant("mx-0");
            public static final String XSMALL = notConstant("mx-xs");
            public static final String SMALL = notConstant("mx-s");
            public static final String MEDIUM = notConstant("mx-m");
            public static final String LARGE = notConstant("mx-l");
            public static final String XLARGE = notConstant("mx-xl");

            private Horizontal() {
            }
        }

        /**
         * Classes for setting the left margin of an element.
         */
        public static final class Left {

            public static final String AUTO = notConstant("ml-auto");
            public static final String NONE = notConstant("ml-0");
            public static final String XSMALL = notConstant("ml-xs");
            public static final String SMALL = notConstant("ml-s");
            public static final String MEDIUM = notConstant("ml-m");
            public static final String LARGE = notConstant("ml-l");
            public static final String XLARGE = notConstant("ml-xl");

            private Left() {
            }
        }

        /**
         * Classes for setting the right margin of an element.
         */
        public static final class Right {

            public static final String AUTO = notConstant("mr-auto");
            public static final String NONE = notConstant("mr-0");
            public static final String XSMALL = notConstant("mr-xs");
            public static final String SMALL = notConstant("mr-s");
            public static final String MEDIUM = notConstant("mr-m");
            public static final String LARGE = notConstant("mr-l");
            public static final String XLARGE = notConstant("mr-xl");

            private Right() {
            }
        }

        /**
         * Classes for setting the logical inline start margin of an element.
         * The actual physical edge where the styles are applied depends on the
         * text flow of the element.
         */
        public static final class Start {

            public static final String AUTO = notConstant("ms-auto");
            public static final String NONE = notConstant("ms-0");
            public static final String XSMALL = notConstant("ms-xs");
            public static final String SMALL = notConstant("ms-s");
            public static final String MEDIUM = notConstant("ms-m");
            public static final String LARGE = notConstant("ms-l");
            public static final String XLARGE = notConstant("ms-xl");

            private Start() {
            }
        }

        /**
         * Classes for setting the top margin of an element.
         */
        public static final class Top {

            public static final String AUTO = notConstant("mt-auto");
            public static final String NONE = notConstant("mt-0");
            public static final String XSMALL = notConstant("mt-xs");
            public static final String SMALL = notConstant("mt-s");
            public static final String MEDIUM = notConstant("mt-m");
            public static final String LARGE = notConstant("mt-l");
            public static final String XLARGE = notConstant("mt-xl");

            private Top() {
            }
        }

        /**
         * Classes for setting both the top and the bottom margins of an
         * element.
         */
        public static final class Vertical {

            public static final String AUTO = notConstant("my-auto");
            public static final String NONE = notConstant("my-0");
            public static final String XSMALL = notConstant("my-xs");
            public static final String SMALL = notConstant("my-s");
            public static final String MEDIUM = notConstant("my-m");
            public static final String LARGE = notConstant("my-l");
            public static final String XLARGE = notConstant("my-xl");

            private Vertical() {
            }
        }

    }

    /**
     * Classes for defining the maximum height of an element.
     */
    public static final class MaxHeight {

        public static final String FULL = notConstant("max-h-full");
        public static final String SCREEN = notConstant("max-h-screen");

        private MaxHeight() {
        }

    }

    /**
     * Classes for defining the maximum width of an element.
     */
    public static final class MaxWidth {

        public static final String FULL = notConstant("max-w-full");
        public static final String SCREEN_SMALL = notConstant(
                "max-w-screen-sm");
        public static final String SCREEN_MEDIUM = notConstant(
                "max-w-screen-md");
        public static final String SCREEN_LARGE = notConstant(
                "max-w-screen-lg");
        public static final String SCREEN_XLARGE = notConstant(
                "max-w-screen-xl");
        public static final String SCREEN_XXLARGE = notConstant(
                "max-w-screen-2xl");

        private MaxWidth() {
        }

    }

    /**
     * Classes for defining the minimum height of an element.
     */
    public static final class MinHeight {

        public static final String FULL = notConstant("min-h-full");
        public static final String NONE = notConstant("min-h-0");
        public static final String SCREEN = notConstant("min-h-screen");

        private MinHeight() {
        }

    }

    /**
     * Classes for defining the minimum width of an element.
     */
    public static final class MinWidth {

        public static final String FULL = notConstant("min-w-full");
        public static final String NONE = notConstant("min-w-0");

        private MinWidth() {
        }

    }

    /**
     * Classes for setting the overflow behavior of an element.
     */
    public static final class Overflow {

        public static final String AUTO = notConstant("overflow-auto");
        public static final String HIDDEN = notConstant("overflow-hidden");
        public static final String SCROLL = notConstant("overflow-scroll");

        private Overflow() {
        }

    }

    /**
     * Classes for setting the padding of an element.
     */
    public static final class Padding {

        public static final String NONE = notConstant("p-0");
        public static final String XSMALL = notConstant("p-xs");
        public static final String SMALL = notConstant("p-s");
        public static final String MEDIUM = notConstant("p-m");
        public static final String LARGE = notConstant("p-l");
        public static final String XLARGE = notConstant("p-xl");

        private Padding() {
        }

        /**
         * Classes for setting the bottom padding of an element.
         */
        public static final class Bottom {

            public static final String NONE = notConstant("pb-0");
            public static final String XSMALL = notConstant("pb-xs");
            public static final String SMALL = notConstant("pb-s");
            public static final String MEDIUM = notConstant("pb-m");
            public static final String LARGE = notConstant("pb-l");
            public static final String XLARGE = notConstant("pb-xl");

            private Bottom() {
            }
        }

        /**
         * Classes for setting the logical inline end padding of an element. The
         * actual physical edge where the styles are applied depends on the text
         * flow of the element.
         */
        public static final class End {

            public static final String NONE = notConstant("pe-0");
            public static final String XSMALL = notConstant("pe-xs");
            public static final String SMALL = notConstant("pe-s");
            public static final String MEDIUM = notConstant("pe-m");
            public static final String LARGE = notConstant("pe-l");
            public static final String XLARGE = notConstant("pe-xl");

            private End() {
            }
        }

        /**
         * Classes for setting both the right and left paddings of an element.
         */
        public static final class Horizontal {

            public static final String NONE = notConstant("px-0");
            public static final String XSMALL = notConstant("px-xs");
            public static final String SMALL = notConstant("px-s");
            public static final String MEDIUM = notConstant("px-m");
            public static final String LARGE = notConstant("px-l");
            public static final String XLARGE = notConstant("px-xl");

            private Horizontal() {
            }
        }

        /**
         * Classes for setting the left padding of an element.
         */
        public static final class Left {

            public static final String NONE = notConstant("pl-0");
            public static final String XSMALL = notConstant("pl-xs");
            public static final String SMALL = notConstant("pl-s");
            public static final String MEDIUM = notConstant("pl-m");
            public static final String LARGE = notConstant("pl-l");
            public static final String XLARGE = notConstant("pl-xl");

            private Left() {
            }
        }

        /**
         * Classes for setting the right padding of an element.
         */
        public static final class Right {

            public static final String NONE = notConstant("pr-0");
            public static final String XSMALL = notConstant("pr-xs");
            public static final String SMALL = notConstant("pr-s");
            public static final String MEDIUM = notConstant("pr-m");
            public static final String LARGE = notConstant("pr-l");
            public static final String XLARGE = notConstant("pr-xl");

            private Right() {
            }
        }

        /**
         * Classes for setting the logical inline start padding of an element.
         * The actual physical edge where the styles are applied depends on the
         * text flow of the element.
         */
        public static final class Start {

            public static final String NONE = notConstant("ps-0");
            public static final String XSMALL = notConstant("ps-xs");
            public static final String SMALL = notConstant("ps-s");
            public static final String MEDIUM = notConstant("ps-m");
            public static final String LARGE = notConstant("ps-l");
            public static final String XLARGE = notConstant("ps-xl");

            private Start() {
            }
        }

        /**
         * Classes for defining the top padding of an element.
         */
        public static final class Top {

            public static final String NONE = notConstant("pt-0");
            public static final String XSMALL = notConstant("pt-xs");
            public static final String SMALL = notConstant("pt-s");
            public static final String MEDIUM = notConstant("pt-m");
            public static final String LARGE = notConstant("pt-l");
            public static final String XLARGE = notConstant("pt-xl");

            private Top() {
            }
        }

        /**
         * Classes for defining both the vertical and horizontal paddings of an
         * element.
         */
        public static final class Vertical {

            public static final String NONE = notConstant("py-0");
            public static final String XSMALL = notConstant("py-xs");
            public static final String SMALL = notConstant("py-s");
            public static final String MEDIUM = notConstant("py-m");
            public static final String LARGE = notConstant("py-l");
            public static final String XLARGE = notConstant("py-xl");

            private Vertical() {
            }
        }

    }

    /**
     * Classes for setting the position of an element.
     */
    public static final class Position {

        public static final String ABSOLUTE = notConstant("absolute");
        public static final String FIXED = notConstant("fixed");
        public static final String RELATIVE = notConstant("relative");
        public static final String STATIC = notConstant("static");
        public static final String STICKY = notConstant("sticky");

        private Position() {
        }

    }

    /**
     * Classes for setting an element’s text alignment.
     */
    public static final class TextAlignment {

        public static final String LEFT = notConstant("text-left");
        public static final String CENTER = notConstant("text-center");
        public static final String RIGHT = notConstant("text-right");
        public static final String JUSTIFY = notConstant("text-justify");

        private TextAlignment() {
        }

    }

    /**
     * Classes for setting an element’s text color.
     */
    public static final class TextColor {

        public static final String HEADER = notConstant("text-header");
        public static final String BODY = notConstant("text-body");
        public static final String SECONDARY = notConstant("text-secondary");
        public static final String TERTIARY = notConstant("text-tertiary");
        public static final String DISABLED = notConstant("text-disabled");

        public static final String PRIMARY = notConstant("text-primary");
        public static final String PRIMARY_CONTRAST = notConstant(
                "text-primary-contrast");

        public static final String ERROR = notConstant("text-error");
        public static final String ERROR_CONTRAST = notConstant(
                "text-error-contrast");

        public static final String SUCCESS = notConstant("text-success");
        public static final String SUCCESS_CONTRAST = notConstant(
                "text-success-contrast");

        private TextColor() {
        }

    }

    /**
     * Classes for setting the text overflow.
     */
    public static final class TextOverflow {

        public static final String CLIP = notConstant("overflow-clip");
        public static final String ELLIPSIS = notConstant("overflow-ellipsis");

        private TextOverflow() {
        }

    }

    /**
     * Classes for transforming the text.
     */
    public static final class TextTransform {

        public static final String CAPITALIZE = notConstant("capitalize");
        public static final String LOWERCASE = notConstant("lowercase");
        public static final String UPPERCASE = notConstant("uppercase");

        private TextTransform() {
        }

    }

    /**
     * Classes for setting how the white space inside an element is handled.
     */
    public static final class Whitespace {

        public static final String NORMAL = notConstant("whitespace-normal");
        public static final String NOWRAP = notConstant("whitespace-nowrap");
        public static final String PRE = notConstant("whitespace-pre");
        public static final String PRE_LINE = notConstant(
                "whitespace-pre-line");
        public static final String PRE_WRAP = notConstant(
                "whitespace-pre-wrap");

        private Whitespace() {
        }

    }

    /**
     * Classes for setting the width of an element.
     */
    public static final class Width {

        public static final String AUTO = notConstant("w-auto");
        public static final String FULL = notConstant("w-full");

        public static final String XSMALL = notConstant("w-xs");
        public static final String SMALL = notConstant("w-s");
        public static final String MEDIUM = notConstant("w-m");
        public static final String LARGE = notConstant("w-l");
        public static final String XLARGE = notConstant("w-xl");

        private Width() {
        }

    }
}

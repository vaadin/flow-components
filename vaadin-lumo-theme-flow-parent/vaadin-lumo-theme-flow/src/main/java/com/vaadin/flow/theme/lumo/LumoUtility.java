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
package com.vaadin.flow.theme.lumo;

/**
 * Contains the definition for all the CSS utility classes provided by Lumo.
 */
public final class LumoUtility {

    private LumoUtility() {
    }

    /**
     * Accessibility related classes.
     */
    public static final class Accessibility {

        /**
         * Hides content visually while keeping it available to screen readers.
         */
        public static final String SCREEN_READER_ONLY = "sr-only";

        private Accessibility() {
        }

    }

    /**
     * Classes for distributing space around and between items along a flexbox’s
     * cross axis or a grid’s block axis. Applies to flexbox and grid layouts.
     */
    public static final class AlignContent {

        public static final String AROUND = "content-around";
        public static final String BETWEEN = "content-between";
        public static final String CENTER = "content-center";
        public static final String END = "content-end";
        public static final String EVENLY = "content-evenly";
        public static final String START = "content-start";
        public static final String STRETCH = "content-stretch";

        private AlignContent() {
        }

    }

    /**
     * Classes for aligning items along a flexbox’s cross axis or a grid’s block
     * axis. Applies to flexbox and grid layouts.
     */
    public static final class AlignItems {

        public static final String BASELINE = "items-baseline";
        public static final String CENTER = "items-center";
        public static final String END = "items-end";
        public static final String START = "items-start";
        public static final String STRETCH = "items-stretch";

        private AlignItems() {
        }

    }

    /**
     * Classes for overriding individual items' align-item property. Applies to
     * flexbox and grid items.
     */
    public static final class AlignSelf {

        public static final String AUTO = "self-auto";
        public static final String BASELINE = "self-baseline";
        public static final String CENTER = "self-center";
        public static final String END = "self-end";
        public static final String START = "self-start";
        public static final String STRETCH = "self-stretch";

        private AlignSelf() {
        }

    }

    /**
     * Classes for applying a background color.
     */
    public static final class Background {

        public static final String BASE = "bg-base";
        public static final String TRANSPARENT = "bg-transparent";

        public static final String CONTRAST = "bg-contrast";
        public static final String CONTRAST_90 = "bg-contrast-90";
        public static final String CONTRAST_80 = "bg-contrast-80";
        public static final String CONTRAST_70 = "bg-contrast-70";
        public static final String CONTRAST_60 = "bg-contrast-60";
        public static final String CONTRAST_50 = "bg-contrast-50";
        public static final String CONTRAST_40 = "bg-contrast-40";
        public static final String CONTRAST_30 = "bg-contrast-30";
        public static final String CONTRAST_20 = "bg-contrast-20";
        public static final String CONTRAST_10 = "bg-contrast-10";
        public static final String CONTRAST_5 = "bg-contrast-5";

        public static final String PRIMARY = "bg-primary";
        public static final String PRIMARY_50 = "bg-primary-50";
        public static final String PRIMARY_10 = "bg-primary-10";

        public static final String ERROR = "bg-error";
        public static final String ERROR_50 = "bg-error-50";
        public static final String ERROR_10 = "bg-error-10";

        public static final String SUCCESS = "bg-success";
        public static final String SUCCESS_50 = "bg-success-50";
        public static final String SUCCESS_10 = "bg-success-10";

        private Background() {
        }

    }

    /**
     * Border-related classes.
     */
    public static final class Border {

        public static final String NONE = "border-0";

        public static final String ALL = "border";
        public static final String BOTTOM = "border-b";
        public static final String LEFT = "border-l";
        public static final String RIGHT = "border-r";
        public static final String TOP = "border-t";

        private Border() {
        }

    }

    /**
     * Classes for setting the border color of an element.
     */
    public static final class BorderColor {

        public static final String CONTRAST = "border-contrast";
        public static final String CONTRAST_90 = "border-contrast-90";
        public static final String CONTRAST_80 = "border-contrast-80";
        public static final String CONTRAST_70 = "border-contrast-70";
        public static final String CONTRAST_60 = "border-contrast-60";
        public static final String CONTRAST_50 = "border-contrast-50";
        public static final String CONTRAST_40 = "border-contrast-40";
        public static final String CONTRAST_30 = "border-contrast-30";
        public static final String CONTRAST_20 = "border-contrast-20";
        public static final String CONTRAST_10 = "border-contrast-10";
        public static final String CONTRAST_5 = "border-contrast-5";

        public static final String PRIMARY = "border-primary";
        public static final String PRIMARY_50 = "border-primary-50";
        public static final String PRIMARY_10 = "border-primary-10";

        public static final String ERROR = "border-error";
        public static final String ERROR_50 = "border-error-50";
        public static final String ERROR_10 = "border-error-10";

        public static final String SUCCESS = "border-success";
        public static final String SUCCESS_50 = "border-success-50";
        public static final String SUCCESS_10 = "border-success-10";

        private BorderColor() {
        }

    }

    /**
     * Classes for setting the border radius of an element.
     */
    public static final class BorderRadius {

        public static final String NONE = "rounded-none";
        public static final String SMALL = "rounded-s";
        public static final String MEDIUM = "rounded-m";
        public static final String LARGE = "rounded-l";

        private BorderRadius() {
        }

    }

    /**
     * Classes for applying a box shadow.
     */
    public static final class BoxShadow {

        public static final String XSMALL = "shadow-xs";
        public static final String SMALL = "shadow-s";
        public static final String MEDIUM = "shadow-m";
        public static final String LARGE = "shadow-l";
        public static final String XLARGE = "shadow-xl";

        private BoxShadow() {
        }

    }

    /**
     * Classes for setting the box sizing property of an element. Box sizing
     * determines whether an element’s border and padding is considered a part
     * of its size.
     */
    public static final class BoxSizing {

        public static final String BORDER = "box-border";
        public static final String CONTENT = "box-content";

        private BoxSizing() {
        }

    }

    /**
     * Classes for setting the display property of an element. Determines
     * whether the element is a block or inline element and how its items are
     * laid out.
     */
    public static final class Display {

        public static final String BLOCK = "block";
        public static final String FLEX = "flex";
        public static final String GRID = "grid";
        public static final String HIDDEN = "hidden";
        public static final String INLINE = "inline";
        public static final String INLINE_BLOCK = "inline-block";
        public static final String INLINE_FLEX = "inline-flex";
        public static final String INLINE_GRID = "inline-grid";

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

                public static final String FLEX = "sm:flex";
                public static final String HIDDEN = "sm:hidden";

                private Small() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 768px
             */
            public static final class Medium {

                public static final String FLEX = "md:flex";
                public static final String HIDDEN = "md:hidden";

                private Medium() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 1024px
             */
            public static final class Large {

                public static final String FLEX = "lg:flex";
                public static final String HIDDEN = "lg:hidden";

                private Large() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 1280px
             */
            public static final class XLarge {

                public static final String FLEX = "xl:flex";
                public static final String HIDDEN = "xl:hidden";

                private XLarge() {
                }
            }

            /**
             * Classes for defining the display property of an element that will
             * be applied when the viewport has a minimum width of 1536px
             */
            public static final class XXLarge {

                public static final String FLEX = "2xl:flex";
                public static final String HIDDEN = "2xl:hidden";

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

        public static final String AUTO = "flex-auto";
        public static final String NONE = "flex-none";

        public static final String GROW = "flex-grow";
        public static final String GROW_NONE = "flex-grow-0";

        public static final String SHRINK = "flex-shrink";
        public static final String SHRINK_NONE = "flex-shrink-0";

        private Flex() {
        }

    }

    /**
     * Classes for setting the flex direction of a flexbox layout.
     */
    public static final class FlexDirection {

        public static final String COLUMN = "flex-col";
        public static final String COLUMN_REVERSE = "flex-col-reverse";
        public static final String ROW = "flex-row";
        public static final String ROW_REVERSE = "flex-row-reverse";

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

                public static final String COLUMN = "sm:flex-col";
                public static final String ROW = "sm:flex-row";

                private Small() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 768px.
             */
            public static final class Medium {

                public static final String COLUMN = "md:flex-col";
                public static final String ROW = "md:flex-row";

                private Medium() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1024px.
             */
            public static final class Large {

                public static final String COLUMN = "lg:flex-col";
                public static final String ROW = "lg:flex-row";

                private Large() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1280px.
             */
            public static final class XLarge {

                public static final String COLUMN = "xl:flex-col";
                public static final String ROW = "xl:flex-row";

                private XLarge() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1536px.
             */
            public static final class XXLarge {

                public static final String COLUMN = "2xl:flex-col";
                public static final String ROW = "2xl:flex-row";

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

        public static final String NOWRAP = "flex-nowrap";
        public static final String WRAP = "flex-wrap";
        public static final String WRAP_REVERSE = "flex-wrap-reverse";

        private FlexWrap() {
        }

    }

    /**
     * Classes for setting the font size of an element.
     */
    public static final class FontSize {

        public static final String XXSMALL = "text-2xs";
        public static final String XSMALL = "text-xs";
        public static final String SMALL = "text-s";
        public static final String MEDIUM = "text-m";
        public static final String LARGE = "text-l";
        public static final String XLARGE = "text-xl";
        public static final String XXLARGE = "text-2xl";
        public static final String XXXLARGE = "text-3xl";

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

                public static final String XXSMALL = "sm:text-2xs";
                public static final String XSMALL = "sm:text-xs";
                public static final String SMALL = "sm:text-s";
                public static final String MEDIUM = "sm:text-m";
                public static final String LARGE = "sm:text-l";
                public static final String XLARGE = "sm:text-xl";
                public static final String XXLARGE = "sm:text-2xl";
                public static final String XXXLARGE = "sm:text-3xl";

                private Small() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 768px.
             */
            public static final class Medium {

                public static final String XXSMALL = "md:text-2xs";
                public static final String XSMALL = "md:text-xs";
                public static final String SMALL = "md:text-s";
                public static final String MEDIUM = "md:text-m";
                public static final String LARGE = "md:text-l";
                public static final String XLARGE = "md:text-xl";
                public static final String XXLARGE = "md:text-2xl";
                public static final String XXXLARGE = "md:text-3xl";

                private Medium() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1024px.
             */
            public static final class Large {

                public static final String XXSMALL = "lg:text-2xs";
                public static final String XSMALL = "lg:text-xs";
                public static final String SMALL = "lg:text-s";
                public static final String MEDIUM = "lg:text-m";
                public static final String LARGE = "lg:text-l";
                public static final String XLARGE = "lg:text-xl";
                public static final String XXLARGE = "lg:text-2xl";
                public static final String XXXLARGE = "lg:text-3xl";

                private Large() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1280px.
             */
            public static final class XLarge {

                public static final String XXSMALL = "xl:text-2xs";
                public static final String XSMALL = "xl:text-xs";
                public static final String SMALL = "xl:text-s";
                public static final String MEDIUM = "xl:text-m";
                public static final String LARGE = "xl:text-l";
                public static final String XLARGE = "xl:text-xl";
                public static final String XXLARGE = "xl:text-2xl";
                public static final String XXXLARGE = "xl:text-3xl";

                private XLarge() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1536px.
             */
            public static final class XXLarge {

                public static final String XXSMALL = "2xl:text-2xs";
                public static final String XSMALL = "2xl:text-xs";
                public static final String SMALL = "2xl:text-s";
                public static final String MEDIUM = "2xl:text-m";
                public static final String LARGE = "2xl:text-l";
                public static final String XLARGE = "2xl:text-xl";
                public static final String XXLARGE = "2xl:text-2xl";
                public static final String XXXLARGE = "2xl:text-3xl";

                private XXLarge() {
                }
            }
        }
    }

    /**
     * Classes for setting the font weight of an element.
     */
    public static final class FontWeight {

        public static final String THIN = "font-thin";
        public static final String EXTRALIGHT = "font-extralight";
        public static final String LIGHT = "font-light";
        public static final String NORMAL = "font-normal";
        public static final String MEDIUM = "font-medium";
        public static final String SEMIBOLD = "font-semibold";
        public static final String BOLD = "font-bold";
        public static final String EXTRABOLD = "font-extrabold";
        public static final String BLACK = "font-black";

        private FontWeight() {
        }
    }

    /**
     * Classes for defining the space between items in a flexbox or grid layout.
     * Applies to flexbox and grid layouts.
     */
    public static final class Gap {

        public static final String XSMALL = "gap-xs";
        public static final String SMALL = "gap-s";
        public static final String MEDIUM = "gap-m";
        public static final String LARGE = "gap-l";
        public static final String XLARGE = "gap-xl";

        private Gap() {
        }

        /**
         * Classes for defining the horizontal space between items in a flexbox
         * or grid layout. Applies to flexbox and grid layouts.
         */
        public static final class Column {

            public static final String XSMALL = "gap-x-xs";
            public static final String SMALL = "gap-x-s";
            public static final String MEDIUM = "gap-x-m";
            public static final String LARGE = "gap-x-l";
            public static final String XLARGE = "gap-x-xl";

            private Column() {
            }
        }

        /**
         * Classes for defining the vertical space between items in a flexbox or
         * grid layout. Applies to flexbox and grid layouts.
         */
        public static final class Row {

            public static final String XSMALL = "gap-y-xs";
            public static final String SMALL = "gap-y-s";
            public static final String MEDIUM = "gap-y-m";
            public static final String LARGE = "gap-y-l";
            public static final String XLARGE = "gap-y-xl";

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
        public static final String FLOW_COLUMN = "grid-flow-col";
        /**
         * Items are placed by filling each row in turn, adding new rows as
         * necessary.
         */
        public static final String FLOW_ROW = "grid-flow-row";

        private Grid() {
        }

        /**
         * Classes for setting the number of columns in a grid layout.
         */
        public static final class Column {

            public static final String COLUMNS_1 = "grid-cols-1";
            public static final String COLUMNS_2 = "grid-cols-2";
            public static final String COLUMNS_3 = "grid-cols-3";
            public static final String COLUMNS_4 = "grid-cols-4";
            public static final String COLUMNS_5 = "grid-cols-5";
            public static final String COLUMNS_6 = "grid-cols-6";
            public static final String COLUMNS_7 = "grid-cols-7";
            public static final String COLUMNS_8 = "grid-cols-8";
            public static final String COLUMNS_9 = "grid-cols-9";
            public static final String COLUMNS_10 = "grid-cols-10";
            public static final String COLUMNS_11 = "grid-cols-11";
            public static final String COLUMNS_12 = "grid-cols-12";

            public static final String COLUMN_SPAN_1 = "col-span-1";
            public static final String COLUMN_SPAN_2 = "col-span-2";
            public static final String COLUMN_SPAN_3 = "col-span-3";
            public static final String COLUMN_SPAN_4 = "col-span-4";
            public static final String COLUMN_SPAN_5 = "col-span-5";
            public static final String COLUMN_SPAN_6 = "col-span-6";
            public static final String COLUMN_SPAN_7 = "col-span-7";
            public static final String COLUMN_SPAN_8 = "col-span-8";
            public static final String COLUMN_SPAN_9 = "col-span-9";
            public static final String COLUMN_SPAN_10 = "col-span-10";
            public static final String COLUMN_SPAN_11 = "col-span-11";
            public static final String COLUMN_SPAN_12 = "col-span-12";

            private Column() {
            }

        }

        /**
         * Classes for setting the number of rows in a grid layout.
         */
        public static final class Row {

            public static final String ROWS_1 = "grid-rows-1";
            public static final String ROWS_2 = "grid-rows-2";
            public static final String ROWS_3 = "grid-rows-3";
            public static final String ROWS_4 = "grid-rows-4";
            public static final String ROWS_5 = "grid-rows-5";
            public static final String ROWS_6 = "grid-rows-6";

            public static final String ROW_SPAN_1 = "row-span-1";
            public static final String ROW_SPAN_2 = "row-span-2";
            public static final String ROW_SPAN_3 = "row-span-3";
            public static final String ROW_SPAN_4 = "row-span-4";
            public static final String ROW_SPAN_5 = "row-span-5";
            public static final String ROW_SPAN_6 = "row-span-6";

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

                public static final String COLUMNS_1 = "sm:grid-cols-1";
                public static final String COLUMNS_2 = "sm:grid-cols-2";
                public static final String COLUMNS_3 = "sm:grid-cols-3";
                public static final String COLUMNS_4 = "sm:grid-cols-4";
                public static final String COLUMNS_5 = "sm:grid-cols-5";
                public static final String COLUMNS_6 = "sm:grid-cols-6";
                public static final String COLUMNS_7 = "sm:grid-cols-7";
                public static final String COLUMNS_8 = "sm:grid-cols-8";
                public static final String COLUMNS_9 = "sm:grid-cols-9";
                public static final String COLUMNS_10 = "sm:grid-cols-10";
                public static final String COLUMNS_11 = "sm:grid-cols-11";
                public static final String COLUMNS_12 = "sm:grid-cols-12";

                private Small() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 768px.
             */
            public static final class Medium {

                public static final String COLUMNS_1 = "md:grid-cols-1";
                public static final String COLUMNS_2 = "md:grid-cols-2";
                public static final String COLUMNS_3 = "md:grid-cols-3";
                public static final String COLUMNS_4 = "md:grid-cols-4";
                public static final String COLUMNS_5 = "md:grid-cols-5";
                public static final String COLUMNS_6 = "md:grid-cols-6";
                public static final String COLUMNS_7 = "md:grid-cols-7";
                public static final String COLUMNS_8 = "md:grid-cols-8";
                public static final String COLUMNS_9 = "md:grid-cols-9";
                public static final String COLUMNS_10 = "md:grid-cols-10";
                public static final String COLUMNS_11 = "md:grid-cols-11";
                public static final String COLUMNS_12 = "md:grid-cols-12";

                private Medium() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1024px.
             */
            public static final class Large {

                public static final String COLUMNS_1 = "lg:grid-cols-1";
                public static final String COLUMNS_2 = "lg:grid-cols-2";
                public static final String COLUMNS_3 = "lg:grid-cols-3";
                public static final String COLUMNS_4 = "lg:grid-cols-4";
                public static final String COLUMNS_5 = "lg:grid-cols-5";
                public static final String COLUMNS_6 = "lg:grid-cols-6";
                public static final String COLUMNS_7 = "lg:grid-cols-7";
                public static final String COLUMNS_8 = "lg:grid-cols-8";
                public static final String COLUMNS_9 = "lg:grid-cols-9";
                public static final String COLUMNS_10 = "lg:grid-cols-10";
                public static final String COLUMNS_11 = "lg:grid-cols-11";
                public static final String COLUMNS_12 = "lg:grid-cols-12";

                private Large() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1280px.
             */
            public static final class XLarge {

                public static final String COLUMNS_1 = "xl:grid-cols-1";
                public static final String COLUMNS_2 = "xl:grid-cols-2";
                public static final String COLUMNS_3 = "xl:grid-cols-3";
                public static final String COLUMNS_4 = "xl:grid-cols-4";
                public static final String COLUMNS_5 = "xl:grid-cols-5";
                public static final String COLUMNS_6 = "xl:grid-cols-6";
                public static final String COLUMNS_7 = "xl:grid-cols-7";
                public static final String COLUMNS_8 = "xl:grid-cols-8";
                public static final String COLUMNS_9 = "xl:grid-cols-9";
                public static final String COLUMNS_10 = "xl:grid-cols-10";
                public static final String COLUMNS_11 = "xl:grid-cols-11";
                public static final String COLUMNS_12 = "xl:grid-cols-12";

                private XLarge() {
                }
            }

            /**
             * Classes that will be applied when the viewport has a minimum
             * width of 1536px.
             */
            public static final class XXLarge {

                public static final String COLUMNS_1 = "2xl:grid-cols-1";
                public static final String COLUMNS_2 = "2xl:grid-cols-2";
                public static final String COLUMNS_3 = "2xl:grid-cols-3";
                public static final String COLUMNS_4 = "2xl:grid-cols-4";
                public static final String COLUMNS_5 = "2xl:grid-cols-5";
                public static final String COLUMNS_6 = "2xl:grid-cols-6";
                public static final String COLUMNS_7 = "2xl:grid-cols-7";
                public static final String COLUMNS_8 = "2xl:grid-cols-8";
                public static final String COLUMNS_9 = "2xl:grid-cols-9";
                public static final String COLUMNS_10 = "2xl:grid-cols-10";
                public static final String COLUMNS_11 = "2xl:grid-cols-11";
                public static final String COLUMNS_12 = "2xl:grid-cols-12";

                private XXLarge() {
                }
            }

        }

    }

    /**
     * Classes for defining the height of an element.
     */
    public static final class Height {

        public static final String AUTO = "h-auto";
        public static final String FULL = "h-full";
        public static final String NONE = "h-0";
        public static final String SCREEN = "h-screen";

        public static final String XSMALL = "h-xs";
        public static final String SMALL = "h-s";
        public static final String MEDIUM = "h-m";
        public static final String LARGE = "h-l";
        public static final String XLARGE = "h-xl";

        private Height() {
        }

    }

    /**
     * Classes for defining the size of elements used as icons.
     */
    public static final class IconSize {

        public static final String SMALL = "icon-s";
        public static final String MEDIUM = "icon-m";
        public static final String LARGE = "icon-l";

        private IconSize() {
        }

    }

    /**
     * Classes for aligning items along a flexbox’s main axis or a grid’s inline
     * axis. Applies to flexbox and grid layouts.
     */
    public static final class JustifyContent {

        public static final String AROUND = "justify-around";
        public static final String BETWEEN = "justify-between";
        public static final String CENTER = "justify-center";
        public static final String END = "justify-end";
        public static final String EVENLY = "justify-evenly";
        public static final String START = "justify-start";

        private JustifyContent() {
        }

    }

    /**
     * Classes for setting the line height of an element.
     */
    public static final class LineHeight {

        public static final String NONE = "leading-none";
        public static final String XSMALL = "leading-xs";
        public static final String SMALL = "leading-s";
        public static final String MEDIUM = "leading-m";

        private LineHeight() {
        }

    }

    /**
     * Class for removing the default look of a list.
     */
    public static final class ListStyleType {

        public static final String NONE = "list-none";

        private ListStyleType() {
        }

    }

    /**
     * Classes for setting the margin of an element.
     */
    public static final class Margin {

        public static final String AUTO = "m-auto";
        public static final String NONE = "m-0";
        public static final String XSMALL = "m-xs";
        public static final String SMALL = "m-s";
        public static final String MEDIUM = "m-m";
        public static final String LARGE = "m-l";
        public static final String XLARGE = "m-xl";

        private Margin() {
        }

        /**
         * Classes for setting the bottom margin of an element.
         */
        public static final class Bottom {

            public static final String AUTO = "mb-auto";
            public static final String NONE = "mb-0";
            public static final String XSMALL = "mb-xs";
            public static final String SMALL = "mb-s";
            public static final String MEDIUM = "mb-m";
            public static final String LARGE = "mb-l";
            public static final String XLARGE = "mb-xl";

            private Bottom() {
            }
        }

        /**
         * Classes for setting the logical inline end margin of an element. The
         * actual physical edge where the styles are applied depends on the text
         * flow of the element.
         */
        public static final class End {

            public static final String AUTO = "me-auto";
            public static final String NONE = "me-0";
            public static final String XSMALL = "me-xs";
            public static final String SMALL = "me-s";
            public static final String MEDIUM = "me-m";
            public static final String LARGE = "me-l";
            public static final String XLARGE = "me-xl";

            private End() {
            }
        }

        /**
         * Classes for setting both the left and the right margins an element.
         */
        public static final class Horizontal {

            public static final String AUTO = "mx-auto";
            public static final String NONE = "mx-0";
            public static final String XSMALL = "mx-xs";
            public static final String SMALL = "mx-s";
            public static final String MEDIUM = "mx-m";
            public static final String LARGE = "mx-l";
            public static final String XLARGE = "mx-xl";

            private Horizontal() {
            }
        }

        /**
         * Classes for setting the left margin of an element.
         */
        public static final class Left {

            public static final String AUTO = "ml-auto";
            public static final String NONE = "ml-0";
            public static final String XSMALL = "ml-xs";
            public static final String SMALL = "ml-s";
            public static final String MEDIUM = "ml-m";
            public static final String LARGE = "ml-l";
            public static final String XLARGE = "ml-xl";

            private Left() {
            }
        }

        /**
         * Classes for setting the right margin of an element.
         */
        public static final class Right {

            public static final String AUTO = "mr-auto";
            public static final String NONE = "mr-0";
            public static final String XSMALL = "mr-xs";
            public static final String SMALL = "mr-s";
            public static final String MEDIUM = "mr-m";
            public static final String LARGE = "mr-l";
            public static final String XLARGE = "mr-xl";

            private Right() {
            }
        }

        /**
         * Classes for setting the logical inline start margin of an element.
         * The actual physical edge where the styles are applied depends on the
         * text flow of the element.
         */
        public static final class Start {

            public static final String AUTO = "ms-auto";
            public static final String NONE = "ms-0";
            public static final String XSMALL = "ms-xs";
            public static final String SMALL = "ms-s";
            public static final String MEDIUM = "ms-m";
            public static final String LARGE = "ms-l";
            public static final String XLARGE = "ms-xl";

            private Start() {
            }
        }

        /**
         * Classes for setting the top margin of an element.
         */
        public static final class Top {

            public static final String AUTO = "mt-auto";
            public static final String NONE = "mt-0";
            public static final String XSMALL = "mt-xs";
            public static final String SMALL = "mt-s";
            public static final String MEDIUM = "mt-m";
            public static final String LARGE = "mt-l";
            public static final String XLARGE = "mt-xl";

            private Top() {
            }
        }

        /**
         * Classes for setting both the top and the bottom margins of an
         * element.
         */
        public static final class Vertical {

            public static final String AUTO = "my-auto";
            public static final String NONE = "my-0";
            public static final String XSMALL = "my-xs";
            public static final String SMALL = "my-s";
            public static final String MEDIUM = "my-m";
            public static final String LARGE = "my-l";
            public static final String XLARGE = "my-xl";

            private Vertical() {
            }
        }

    }

    /**
     * Classes for defining the maximum height of an element.
     */
    public static final class MaxHeight {

        public static final String FULL = "max-h-full";
        public static final String SCREEN = "max-h-screen";

        private MaxHeight() {
        }

    }

    /**
     * Classes for defining the maximum width of an element.
     */
    public static final class MaxWidth {

        public static final String FULL = "max-w-full";
        public static final String SCREEN_SMALL = "max-w-screen-sm";
        public static final String SCREEN_MEDIUM = "max-w-screen-md";
        public static final String SCREEN_LARGE = "max-w-screen-lg";
        public static final String SCREEN_XLARGE = "max-w-screen-xl";
        public static final String SCREEN_XXLARGE = "max-w-screen-2xl";

        private MaxWidth() {
        }

    }

    /**
     * Classes for defining the minimum height of an element.
     */
    public static final class MinHeight {

        public static final String FULL = "min-h-full";
        public static final String NONE = "min-h-0";
        public static final String SCREEN = "min-h-screen";

        private MinHeight() {
        }

    }

    /**
     * Classes for defining the minimum width of an element.
     */
    public static final class MinWidth {

        public static final String FULL = "min-w-full";
        public static final String NONE = "min-w-0";

        private MinWidth() {
        }

    }

    /**
     * Classes for setting the overflow behavior of an element.
     */
    public static final class Overflow {

        public static final String AUTO = "overflow-auto";
        public static final String HIDDEN = "overflow-hidden";
        public static final String SCROLL = "overflow-scroll";

        private Overflow() {
        }

    }

    /**
     * Classes for setting the padding of an element.
     */
    public static final class Padding {

        public static final String NONE = "p-0";
        public static final String XSMALL = "p-xs";
        public static final String SMALL = "p-s";
        public static final String MEDIUM = "p-m";
        public static final String LARGE = "p-l";
        public static final String XLARGE = "p-xl";

        private Padding() {
        }

        /**
         * Classes for setting the bottom padding of an element.
         */
        public static final class Bottom {

            public static final String NONE = "pb-0";
            public static final String XSMALL = "pb-xs";
            public static final String SMALL = "pb-s";
            public static final String MEDIUM = "pb-m";
            public static final String LARGE = "pb-l";
            public static final String XLARGE = "pb-xl";

            private Bottom() {
            }
        }

        /**
         * Classes for setting the logical inline end padding of an element. The
         * actual physical edge where the styles are applied depends on the text
         * flow of the element.
         */
        public static final class End {

            public static final String NONE = "pe-0";
            public static final String XSMALL = "pe-xs";
            public static final String SMALL = "pe-s";
            public static final String MEDIUM = "pe-m";
            public static final String LARGE = "pe-l";
            public static final String XLARGE = "pe-xl";

            private End() {
            }
        }

        /**
         * Classes for setting both the right and left paddings of an element.
         */
        public static final class Horizontal {

            public static final String NONE = "px-0";
            public static final String XSMALL = "px-xs";
            public static final String SMALL = "px-s";
            public static final String MEDIUM = "px-m";
            public static final String LARGE = "px-l";
            public static final String XLARGE = "px-xl";

            private Horizontal() {
            }
        }

        /**
         * Classes for setting the left padding of an element.
         */
        public static final class Left {

            public static final String NONE = "pl-0";
            public static final String XSMALL = "pl-xs";
            public static final String SMALL = "pl-s";
            public static final String MEDIUM = "pl-m";
            public static final String LARGE = "pl-l";
            public static final String XLARGE = "pl-xl";

            private Left() {
            }
        }

        /**
         * Classes for setting the right padding of an element.
         */
        public static final class Right {

            public static final String NONE = "pr-0";
            public static final String XSMALL = "pr-xs";
            public static final String SMALL = "pr-s";
            public static final String MEDIUM = "pr-m";
            public static final String LARGE = "pr-l";
            public static final String XLARGE = "pr-xl";

            private Right() {
            }
        }

        /**
         * Classes for setting the logical inline start padding of an element.
         * The actual physical edge where the styles are applied depends on the
         * text flow of the element.
         */
        public static final class Start {

            public static final String NONE = "ps-0";
            public static final String XSMALL = "ps-xs";
            public static final String SMALL = "ps-s";
            public static final String MEDIUM = "ps-m";
            public static final String LARGE = "ps-l";
            public static final String XLARGE = "ps-xl";

            private Start() {
            }
        }

        /**
         * Classes for defining the top padding of an element.
         */
        public static final class Top {

            public static final String NONE = "pt-0";
            public static final String XSMALL = "pt-xs";
            public static final String SMALL = "pt-s";
            public static final String MEDIUM = "pt-m";
            public static final String LARGE = "pt-l";
            public static final String XLARGE = "pt-xl";

            private Top() {
            }
        }

        /**
         * Classes for defining both the vertical and horizontal paddings of an
         * element.
         */
        public static final class Vertical {

            public static final String NONE = "py-0";
            public static final String XSMALL = "py-xs";
            public static final String SMALL = "py-s";
            public static final String MEDIUM = "py-m";
            public static final String LARGE = "py-l";
            public static final String XLARGE = "py-xl";

            private Vertical() {
            }
        }

    }

    /**
     * Classes for setting the position of an element.
     */
    public static final class Position {

        public static final String ABSOLUTE = "absolute";
        public static final String FIXED = "fixed";
        public static final String RELATIVE = "relative";
        public static final String STATIC = "static";
        public static final String STICKY = "sticky";

        private Position() {
        }

    }

    /**
     * Classes for setting an element’s text alignment.
     */
    public static final class TextAlignment {

        public static final String LEFT = "text-left";
        public static final String CENTER = "text-center";
        public static final String RIGHT = "text-right";
        public static final String JUSTIFY = "text-justify";

        private TextAlignment() {
        }

    }

    /**
     * Classes for setting an element’s text color.
     */
    public static final class TextColor {

        public static final String HEADER = "text-header";
        public static final String BODY = "text-body";
        public static final String SECONDARY = "text-secondary";
        public static final String TERTIARY = "text-tertiary";
        public static final String DISABLED = "text-disabled";

        public static final String PRIMARY = "text-primary";
        public static final String PRIMARY_CONTRAST = "text-primary-contrast";

        public static final String ERROR = "text-error";
        public static final String ERROR_CONTRAST = "text-error-contrast";

        public static final String SUCCESS = "text-success";
        public static final String SUCCESS_CONTRAST = "text-success-contrast";

        private TextColor() {
        }

    }

    /**
     * Classes for setting the text overflow.
     */
    public static final class TextOverflow {

        public static final String CLIP = "overflow-clip";
        public static final String ELLIPSIS = "overflow-ellipsis";

        private TextOverflow() {
        }

    }

    /**
     * Classes for transforming the text.
     */
    public static final class TextTransform {

        public static final String CAPITALIZE = "capitalize";
        public static final String LOWERCASE = "lowercase";
        public static final String UPPERCASE = "uppercase";

        private TextTransform() {
        }

    }

    /**
     * Classes for setting how the white space inside an element is handled.
     */
    public static final class Whitespace {

        public static final String NORMAL = "whitespace-normal";
        public static final String NOWRAP = "whitespace-nowrap";
        public static final String PRE = "whitespace-pre";
        public static final String PRE_LINE = "whitespace-pre-line";
        public static final String PRE_WRAP = "whitespace-pre-wrap";

        private Whitespace() {
        }

    }

    /**
     * Classes for setting the width of an element.
     */
    public static final class Width {

        public static final String AUTO = "w-auto";
        public static final String FULL = "w-full";

        public static final String XSMALL = "w-xs";
        public static final String SMALL = "w-s";
        public static final String MEDIUM = "w-m";
        public static final String LARGE = "w-l";
        public static final String XLARGE = "w-xl";

        private Width() {
        }

    }
}

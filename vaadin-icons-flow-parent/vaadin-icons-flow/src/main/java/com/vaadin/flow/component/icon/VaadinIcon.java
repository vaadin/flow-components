/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.icon;

/**
 * Enumeration of all icons in the <a href="https://vaadin.com/icons">Vaadin
 * Icons</a> collection.
 * <p>
 * These instances can be used to create {@link Icon} components either by using
 * their {@link #create()} method or by passing them to Icon's constructor.
 *
 * @author Vaadin Ltd
 *
 * @since 1.0
 */
public enum VaadinIcon implements IconFactory {

    /**
     * @deprecated Use {@link #CALC} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ABACUS, @Deprecated(since = "25.3", forRemoval = true)
    ABSOLUTE_POSITION,
    ACADEMY_CAP,
    ACCESSIBILITY,
    @Deprecated(since = "25.3", forRemoval = true)
    ACCORDION_MENU,
    /**
     * @deprecated Use {@link #DOCK} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ADD_DOCK,
    DOCK,
    /**
     * @deprecated Use {@link #CONTRAST} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ADJUST,
    CONTRAST,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ADOBE_FLASH,
    AIRPLANE,
    ALARM,
    ALIGN_CENTER,
    ALIGN_JUSTIFY,
    ALIGN_LEFT,
    ALIGN_RIGHT,
    /**
     * @deprecated Use {@link #ALT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ALT_A,
    ALT,
    AMBULANCE,
    ANCHOR,
    ANGLE_DOUBLE_DOWN,
    ANGLE_DOUBLE_LEFT,
    ANGLE_DOUBLE_RIGHT,
    ANGLE_DOUBLE_UP,
    ANGLE_DOWN,
    ANGLE_LEFT,
    ANGLE_RIGHT,
    ANGLE_UP,
    ARCHIVE,
    ARCHIVES,
    @Deprecated(since = "25.3", forRemoval = true)
    AREA_SELECT,
    ARROW_BACKWARD,
    /**
     * @deprecated Use {@link #ARROW_CIRCLE_DOWN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ARROW_CIRCLE_DOWN_O,
    ARROW_CIRCLE_DOWN,
    /**
     * @deprecated Use {@link #ARROW_CIRCLE_LEFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ARROW_CIRCLE_LEFT_O,
    ARROW_CIRCLE_LEFT,
    /**
     * @deprecated Use {@link #ARROW_CIRCLE_RIGHT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ARROW_CIRCLE_RIGHT_O,
    ARROW_CIRCLE_RIGHT,
    /**
     * @deprecated Use {@link #ARROW_CIRCLE_UP} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ARROW_CIRCLE_UP_O,
    ARROW_CIRCLE_UP,
    ARROW_DOWN,
    ARROW_FORWARD,
    ARROW_LEFT,
    ARROW_LONG_DOWN,
    ARROW_LONG_LEFT,
    ARROW_RIGHT,
    ARROW_UP,
    ARROWS_CROSS,
    ARROWS_LONG_H,
    ARROWS_LONG_RIGHT,
    ARROWS_LONG_UP,
    ARROWS_LONG_V,
    ARROWS,
    ASTERISK,
    AT,
    AUTOMATION,
    /**
     * @deprecated Use {@link #BACKSPACE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    BACKSPACE_A,
    BACKSPACE,
    BACKWARDS,
    BAN,
    BAR_CHART_H,
    BAR_CHART_V,
    BAR_CHART,
    BARCODE,
    BED,
    /**
     * @deprecated Use {@link #BELL} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    BELL_O,
    /**
     * @deprecated Use {@link #BELL_SLASH} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    BELL_SLASH_O,
    BELL_SLASH,
    BELL,
    BOAT,
    BOLD,
    BOLT,
    BOMB,
    BOOK_DOLLAR,
    BOOK_PERCENT,
    BOOK,
    /**
     * @deprecated Use {@link #BOOKMARK} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    BOOKMARK_O,
    BOOKMARK,
    BRANCH,
    BRIEFCASE,
    BROWSER,
    /**
     * @deprecated Use {@link #BUG} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    BUG_O,
    BUG,
    /**
     * @deprecated Use {@link #BUILDING} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    BUILDING_O,
    BUILDING,
    BULLETS,
    BULLSEYE,
    BUS,
    @Deprecated(since = "25.3", forRemoval = true)
    BUTTON,
    CALC_BOOK,
    CALC,
    CALENDAR_BRIEFCASE,
    CALENDAR_CLOCK,
    CALENDAR_ENVELOPE,
    /**
     * @deprecated Use {@link #CALENDAR} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CALENDAR_O,
    CALENDAR_USER,
    CALENDAR,
    CAMERA,
    CAR,
    CARET_DOWN,
    CARET_LEFT,
    CARET_RIGHT,
    /**
     * @deprecated Use {@link #CARET_SQUARE_DOWN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CARET_SQUARE_DOWN_O,
    CARET_SQUARE_DOWN,
    /**
     * @deprecated Use {@link #CARET_SQUARE_LEFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CARET_SQUARE_LEFT_O,
    CARET_SQUARE_LEFT,
    /**
     * @deprecated Use {@link #CARET_SQUARE_RIGHT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CARET_SQUARE_RIGHT_O,
    CARET_SQUARE_RIGHT,
    /**
     * @deprecated Use {@link #CARET_SQUARE_UP} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CARET_SQUARE_UP_O,
    CARET_SQUARE_UP,
    CARET_UP,
    /**
     * @deprecated Use {@link #CART} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CART_O,
    CART,
    CASH,
    CHART_3D,
    CHART_GRID,
    /**
     * @deprecated Use {@link #LINE_CHART} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHART_LINE,
    CHART_TIMELINE,
    CHART,
    CHAT,
    /**
     * @deprecated Use {@link #CHECK_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHECK_CIRCLE_O,
    CHECK_CIRCLE,
    /**
     * @deprecated Use {@link #CHECK_SQUARE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHECK_SQUARE_O,
    CHECK_SQUARE,
    CHECK,
    /**
     * @deprecated Use {@link #CHEVRON_CIRCLE_DOWN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHEVRON_CIRCLE_DOWN_O,
    CHEVRON_CIRCLE_DOWN,
    /**
     * @deprecated Use {@link #CHEVRON_CIRCLE_LEFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHEVRON_CIRCLE_LEFT_O,
    CHEVRON_CIRCLE_LEFT,
    /**
     * @deprecated Use {@link #CHEVRON_CIRCLE_RIGHT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHEVRON_CIRCLE_RIGHT_O,
    CHEVRON_CIRCLE_RIGHT,
    /**
     * @deprecated Use {@link #CHEVRON_CIRCLE_UP} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CHEVRON_CIRCLE_UP_O,
    CHEVRON_CIRCLE_UP,
    CHEVRON_DOWN_SMALL,
    CHEVRON_DOWN,
    CHEVRON_LEFT_SMALL,
    CHEVRON_LEFT,
    CHEVRON_RIGHT_SMALL,
    CHEVRON_RIGHT,
    CHEVRON_UP_SMALL,
    CHEVRON_UP,
    CHILD,
    /**
     * @deprecated Use {@link #CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CIRCLE_THIN,
    CIRCLE,
    CLIPBOARD_CHECK,
    CLIPBOARD_CROSS,
    CLIPBOARD_HEART,
    CLIPBOARD_PULSE,
    CLIPBOARD_TEXT,
    CLIPBOARD_USER,
    CLIPBOARD,
    CLOCK,
    /**
     * @deprecated Use {@link #CLOSE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CLOSE_BIG,
    /**
     * @deprecated Use {@link #CLOSE_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CLOSE_CIRCLE_O,
    CLOSE_CIRCLE,
    /**
     * @deprecated Use {@link #CLOSE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CLOSE_SMALL,
    CLOSE,
    /**
     * @deprecated Use {@link #CLOUD_DOWNLOAD} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CLOUD_DOWNLOAD_O,
    CLOUD_DOWNLOAD,
    /**
     * @deprecated Use {@link #CLOUD} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CLOUD_O,
    /**
     * @deprecated Use {@link #CLOUD_UPLOAD} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CLOUD_UPLOAD_O,
    CLOUD_UPLOAD,
    CLOUD,
    CLUSTER,
    CODE,
    COFFEE,
    /**
     * @deprecated Use {@link #COG} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    COG_O,
    COG,
    COGS,
    COIN_PILES,
    COINS,
    @Deprecated(since = "25.3", forRemoval = true)
    COMBOBOX,
    COMMAND,
    /**
     * @deprecated Use {@link #COMMENT_ELLIPSIS} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    COMMENT_ELLIPSIS_O,
    COMMENT_ELLIPSIS,
    /**
     * @deprecated Use {@link #COMMENT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    COMMENT_O,
    COMMENT,
    /**
     * @deprecated Use {@link #COMMENTS} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    COMMENTS_O,
    COMMENTS,
    COMPASS,
    COMPILE,
    COMPRESS_SQUARE,
    COMPRESS,
    /**
     * @deprecated Use {@link #CONNECT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CONNECT_O,
    CONNECT,
    CONTROLLER,
    /**
     * @deprecated Use {@link #COPY} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    COPY_O,
    COPY,
    COPYRIGHT,
    CORNER_LOWER_LEFT,
    CORNER_LOWER_RIGHT,
    CORNER_UPPER_LEFT,
    CORNER_UPPER_RIGHT,
    CREDIT_CARD,
    CROP,
    CROSS_CUTLERY,
    CROSSHAIRS,
    /**
     * @deprecated Use {@link #CODE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CSS,
    /**
     * @deprecated Use {@link #COMMAND} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CTRL_A,
    /**
     * @deprecated Use {@link #COMMAND} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CTRL,
    CUBE,
    CUBES,
    CURLY_BRACKETS,
    /**
     * @deprecated Use {@link #CURSOR} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    CURSOR_O,
    CURSOR,
    CUTLERY,
    DASHBOARD,
    DATABASE,
    @Deprecated(since = "25.3", forRemoval = true)
    DATE_INPUT,
    /**
     * @deprecated Use {@link #OUTDENT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    DEINDENT,
    /**
     * @deprecated Use {@link #BACKSPACE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    DEL_A,
    /**
     * @deprecated Use {@link #BACKSPACE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    DEL,
    DENTAL_CHAIR,
    DESKTOP,
    /**
     * @deprecated Use {@link #DIAMOND} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    DIAMOND_O,
    DIAMOND,
    DIPLOMA_SCROLL,
    DIPLOMA,
    DISC,
    DOCTOR_BRIEFCASE,
    DOCTOR,
    DOLLAR,
    DOT_CIRCLE,
    DOWNLOAD_ALT,
    DOWNLOAD,
    DROP,
    EDIT,
    EJECT,
    ELASTIC,
    /**
     * @deprecated Use {@link #ELLIPSIS_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ELLIPSIS_CIRCLE_O,
    ELLIPSIS_CIRCLE,
    /**
     * @deprecated Use {@link #ELLIPSIS_H} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ELLIPSIS_DOTS_H,
    /**
     * @deprecated Use {@link #ELLIPSIS_V} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ELLIPSIS_DOTS_V,
    ELLIPSIS_H,
    ELLIPSIS_V,
    ENTER_ARROW,
    ENTER,
    /**
     * @deprecated Use {@link #ENVELOPE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ENVELOPE_O,
    /**
     * @deprecated Use {@link #ENVELOPE_OPEN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ENVELOPE_OPEN_O,
    ENVELOPE_OPEN,
    ENVELOPE,
    /**
     * @deprecated Use {@link #ENVELOPES} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ENVELOPES_O,
    ENVELOPES,
    ERASER,
    @Deprecated(since = "25.3", forRemoval = true)
    ESC_A,
    @Deprecated(since = "25.3", forRemoval = true)
    ESC,
    EURO,
    EXCHANGE,
    /**
     * @deprecated Use {@link #EXCLAMATION_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    EXCLAMATION_CIRCLE_O,
    EXCLAMATION_CIRCLE,
    EXCLAMATION,
    /**
     * @deprecated Use {@link #EXIT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    EXIT_O,
    EXIT,
    EXPAND_FULL,
    EXPAND_SQUARE,
    EXPAND,
    EXTERNAL_BROWSER,
    EXTERNAL_LINK,
    EYE_SLASH,
    EYE,
    EYEDROPPER,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FACEBOOK_SQUARE,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FACEBOOK,
    FACTORY,
    FAMILY,
    FAST_BACKWARD,
    FAST_FORWARD,
    FEMALE,
    FILE_ADD,
    FILE_CODE,
    FILE_FONT,
    FILE_MOVIE,
    /**
     * @deprecated Use {@link #FILE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FILE_O,
    FILE_PICTURE,
    FILE_PRESENTATION,
    FILE_PROCESS,
    FILE_REFRESH,
    FILE_REMOVE,
    FILE_SEARCH,
    FILE_SOUND,
    FILE_START,
    FILE_TABLE,
    /**
     * @deprecated Use {@link #FILE_TEXT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FILE_TEXT_O,
    FILE_TEXT,
    /**
     * @deprecated Use {@link #FILE_TREE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FILE_TREE_SMALL,
    /**
     * @deprecated Use {@link #FILE_TREE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FILE_TREE_SUB,
    FILE_TREE,
    FILE_ZIP,
    FILE,
    FILL,
    FILM,
    FILTER,
    FIRE,
    FLAG_CHECKERED,
    /**
     * @deprecated Use {@link #FLAG} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FLAG_O,
    FLAG,
    FLASH,
    FLASK,
    FLIGHT_LANDING,
    FLIGHT_TAKEOFF,
    FLIP_H,
    FLIP_V,
    FOLDER_ADD,
    /**
     * @deprecated Use {@link #FOLDER} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FOLDER_O,
    /**
     * @deprecated Use {@link #FOLDER_OPEN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FOLDER_OPEN_O,
    FOLDER_OPEN,
    FOLDER_REMOVE,
    FOLDER_SEARCH,
    FOLDER,
    FONT,
    FORKLIFT,
    FORM,
    FORWARD,
    /**
     * @deprecated Use {@link #FROWN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FROWN_O,
    FROWN,
    FUNCTION,
    /**
     * @deprecated Use {@link #FILTER} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    FUNNEL,
    GAMEPAD,
    GAVEL,
    GIFT,
    GLASS,
    GLASSES,
    GLOBE_WIRE,
    GLOBE,
    GOLF,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    GOOGLE_PLUS_SQUARE,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    GOOGLE_PLUS,
    GRAB,
    /**
     * @deprecated Use {@link #GRID} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    GRID_BEVEL,
    /**
     * @deprecated Use {@link #GRID_BIG} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    GRID_BIG_O,
    GRID_BIG,
    GRID_H,
    /**
     * @deprecated Use {@link #GRID} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    GRID_SMALL_O,
    /**
     * @deprecated Use {@link #GRID} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    GRID_SMALL,
    GRID_V,
    GRID,
    GROUP,
    HAMMER,
    HAND,
    HANDLE_CORNER,
    /**
     * @deprecated Use {@link #PERSON} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    HANDS_UP,
    HANDSHAKE,
    /**
     * @deprecated Use {@link #HARDDRIVE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    HARDDRIVE_O,
    HARDDRIVE,
    HASH,
    HEADER,
    HEADPHONES,
    HEADSET,
    HEALTH_CARD,
    /**
     * @deprecated Use {@link #HEART} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    HEART_O,
    HEART,
    /**
     * @deprecated Use {@link #HOME} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    HOME_O,
    HOME,
    HOSPITAL,
    HOURGLASS_EMPTY,
    HOURGLASS_END,
    HOURGLASS_START,
    HOURGLASS,
    INBOX,
    INDENT,
    /**
     * @deprecated Use {@link #INFO_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    INFO_CIRCLE_O,
    INFO_CIRCLE,
    INFO,
    INPUT,
    INSERT,
    INSTITUTION,
    INVOICE,
    ITALIC,
    KEY_O,
    KEY,
    /**
     * @deprecated Use {@link #KEYBOARD} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    KEYBOARD_O,
    KEYBOARD,
    LAPTOP,
    LAYOUT,
    /**
     * @deprecated Use {@link #LEVEL_DOWN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    LEVEL_DOWN_BOLD,
    LEVEL_DOWN,
    /**
     * @deprecated Use {@link #LEVEL_LEFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    LEVEL_LEFT_BOLD,
    LEVEL_LEFT,
    /**
     * @deprecated Use {@link #LEVEL_RIGHT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    LEVEL_RIGHT_BOLD,
    LEVEL_RIGHT,
    /**
     * @deprecated Use {@link #LEVEL_UP} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    LEVEL_UP_BOLD,
    LEVEL_UP,
    LIFEBUOY,
    LIGHTBULB,
    LINE_BAR_CHART,
    LINE_CHART,
    LINE_H,
    LINE_V,
    LINES_LIST,
    LINES,
    LINK,
    LIST_OL,
    @Deprecated(since = "25.3", forRemoval = true)
    LIST_SELECT,
    LIST_UL,
    LIST,
    /**
     * @deprecated Use {@link #COMPASS} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    LOCATION_ARROW_CIRCLE_O,
    /**
     * @deprecated Use {@link #COMPASS} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    LOCATION_ARROW_CIRCLE,
    LOCATION_ARROW,
    LOCK,
    MAGIC,
    MAGNET,
    MAILBOX,
    MALE,
    MAP_MARKER,
    MARGIN_BOTTOM,
    MARGIN_LEFT,
    MARGIN_RIGHT,
    MARGIN_TOP,
    MARGIN,
    MEDAL,
    MEGAPHONE,
    /**
     * @deprecated Use {@link #MEH} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MEH_O,
    MEH,
    MENU,
    MICROPHONE,
    /**
     * @deprecated Use {@link #MINUS_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MINUS_CIRCLE_O,
    MINUS_CIRCLE,
    /**
     * @deprecated Use {@link #MINUS_SQUARE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MINUS_SQUARE_O,
    MINUS_SQUARE,
    MINUS,
    /**
     * @deprecated Use {@link #MOBILE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MOBILE_BROWSER,
    /**
     * @deprecated Use {@link #MOBILE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MOBILE_RETRO,
    MOBILE,
    @Deprecated(since = "25.3", forRemoval = true)
    MODAL_LIST,
    MODAL,
    MONEY_DEPOSIT,
    MONEY_EXCHANGE,
    MONEY_WITHDRAW,
    MONEY,
    /**
     * @deprecated Use {@link #MOON} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MOON_O,
    MOON,
    /**
     * @deprecated Use {@link #SUN_RISE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    MORNING,
    MOVIE,
    MUSIC,
    MUTE,
    @Deprecated(since = "25.3", forRemoval = true)
    NATIVE_BUTTON,
    NEWSPAPER,
    NOTEBOOK,
    NURSE,
    OFFICE,
    OPEN_BOOK,
    /**
     * @deprecated Use {@link #COMMAND} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    OPTION_A,
    /**
     * @deprecated Use {@link #COMMAND} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    OPTION,
    OPTIONS,
    ORIENTATION,
    OUTDENT,
    OUT,
    OUTBOX,
    PACKAGE,
    PADDING_BOTTOM,
    PADDING_LEFT,
    PADDING_RIGHT,
    PADDING_TOP,
    PADDING,
    PAINT_ROLL,
    PAINTBRUSH,
    PALETTE,
    @Deprecated(since = "25.3", forRemoval = true)
    PANEL,
    PAPERCLIP,
    /**
     * @deprecated Use {@link #PAPERPLANE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    PAPERPLANE_O,
    PAPERPLANE,
    PARAGRAPH,
    PASSWORD,
    PASTE,
    PAUSE,
    PENCIL,
    PERSON,
    /**
     * @deprecated Use {@link #PHONE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    PHONE_LANDLINE,
    PHONE,
    PICTURE,
    PIE_BAR_CHART,
    PIE_CHART,
    /**
     * @deprecated Use {@link #PIGGY_BANK} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    PIGGY_BANK_COIN,
    PIGGY_BANK,
    PILL,
    PILLS,
    PIN_POST,
    PIN,
    /**
     * @deprecated Use {@link #PLAY_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    PLAY_CIRCLE_O,
    PLAY_CIRCLE,
    PLAY,
    PLUG,
    /**
     * @deprecated Use {@link #PLUS_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    PLUS_CIRCLE_O,
    PLUS_CIRCLE,
    PLUS_MINUS,
    /**
     * @deprecated Use {@link #PLUS_SQUARE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    PLUS_SQUARE_O,
    PLUS_SQUARE,
    PLUS,
    POINTER,
    POWER_OFF,
    PRESENTATION,
    PRINT,
    PROGRESSBAR,
    PUZZLE_PIECE,
    PYRAMID_CHART,
    QRCODE,
    /**
     * @deprecated Use {@link #QUESTION_CIRCLE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    QUESTION_CIRCLE_O,
    QUESTION_CIRCLE,
    QUESTION,
    QUOTE_LEFT,
    QUOTE_RIGHT,
    RANDOM,
    RASTER_LOWER_LEFT,
    RASTER,
    RECORDS,
    RECYCLE,
    REFRESH,
    REPLY_ALL,
    REPLY,
    RESIZE_H,
    RESIZE_V,
    RETWEET,
    RHOMBUS,
    /**
     * @deprecated Use {@link #BRANCH} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ROAD_BRANCH,
    /**
     * @deprecated Use {@link #BRANCH} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    ROAD_BRANCHES,
    ROAD_SPLIT,
    ROAD,
    ROCKET,
    ROTATE_LEFT,
    ROTATE_RIGHT,
    /**
     * @deprecated Use {@link #RSS} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    RSS_SQUARE,
    RSS,
    SAFE_LOCK,
    SAFE,
    /**
     * @deprecated Use {@link #SCALE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SCALE_UNBALANCE,
    SCALE,
    SCATTER_CHART,
    SCISSORS,
    SCREWDRIVER,
    SEARCH_MINUS,
    SEARCH_PLUS,
    SEARCH,
    @Deprecated(since = "25.3", forRemoval = true)
    SELECT,
    SERVER,
    SHARE_SQUARE,
    SHARE,
    SHIELD,
    /**
     * @deprecated Use {@link #SHIFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SHIFT_ARROW,
    SHIFT,
    SHOP,
    /**
     * @deprecated Use {@link #SIGN_IN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SIGN_IN_ALT,
    SIGN_IN,
    /**
     * @deprecated Use {@link #SIGN_OUT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SIGN_OUT_ALT,
    SIGN_OUT,
    SIGNAL,
    SITEMAP,
    /**
     * @deprecated Use {@link #SLIDERS} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SLIDER,
    SLIDERS,
    /**
     * @deprecated Use {@link #SMILEY} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SMILEY_O,
    SMILEY,
    SORT,
    SOUND_DISABLE,
    SPARK_LINE,
    SPECIALIST,
    /**
     * @deprecated Use {@link #SPINNER} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SPINNER_ARC,
    /**
     * @deprecated Use {@link #SPINNER} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SPINNER_THIRD,
    SPINNER,
    SPLINE_AREA_CHART,
    SPLINE_CHART,
    SPLIT_H,
    SPLIT_V,
    SPLIT,
    /**
     * @deprecated Use {@link #CUTLERY} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SPOON,
    /**
     * @deprecated Use {@link #SQUARE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SQUARE_SHADOW,
    SQUARE,
    /**
     * @deprecated Use {@link #STAR_HALF_LEFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STAR_HALF_LEFT_O,
    /**
     * @deprecated Use {@link #STAR_HALF} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STAR_HALF_LEFT,
    /**
     * @deprecated Use {@link #STAR_HALF_RIGHT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STAR_HALF_RIGHT_O,
    /**
     * @deprecated Use {@link #STAR_HALF} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STAR_HALF_RIGHT,
    STAR_HALF,
    /**
     * @deprecated Use {@link #STAR} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STAR_O,
    STAR,
    START_COG,
    STEP_BACKWARD,
    STEP_FORWARD,
    /**
     * @deprecated Use {@link #DOCTOR} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STETHOSCOPE,
    /**
     * @deprecated Use {@link #FORKLIFT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    STOCK,
    STOP_COG,
    STOP,
    STOPWATCH,
    STORAGE,
    STRIKETHROUGH,
    SUBSCRIPT,
    SUITCASE,
    SUN_DOWN,
    /**
     * @deprecated Use {@link #SUN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    SUN_O,
    SUN,
    SUN_RISE,
    SUPERSCRIPT,
    SWORD,
    /**
     * @deprecated Use {@link #TAB} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    TAB_A,
    TAB,
    TABLE,
    TABLET,
    @Deprecated(since = "25.3", forRemoval = true)
    TABS,
    TAG,
    TAGS,
    TASKS,
    TAXI,
    @Deprecated(since = "25.3", forRemoval = true)
    TEETH,
    TERMINAL,
    TEXT_HEIGHT,
    /**
     * @deprecated Use {@link #INPUT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    TEXT_INPUT,
    /**
     * @deprecated Use {@link #FONT} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    TEXT_LABEL,
    @Deprecated(since = "25.3", forRemoval = true)
    TEXT_WIDTH,
    /**
     * @deprecated Use {@link #SQUARE} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    THIN_SQUARE,
    /**
     * @deprecated Use {@link #THUMBS_DOWN} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    THUMBS_DOWN_O,
    THUMBS_DOWN,
    /**
     * @deprecated Use {@link #THUMBS_UP} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    THUMBS_UP_O,
    THUMBS_UP,
    TICKET,
    TIME_BACKWARD,
    TIME_FORWARD,
    TIMER,
    TOOLBOX,
    TOOLS,
    @Deprecated(since = "25.3", forRemoval = true)
    TOOTH,
    /**
     * @deprecated Use {@link #POINTER} instead.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    TOUCH,
    TRAIN,
    TRASH,
    @Deprecated(since = "25.3", forRemoval = true)
    TREE_TABLE,
    TRENDING_DOWN,
    TRENDING_UP,
    TROPHY,
    TRUCK,
    @Deprecated(since = "25.3", forRemoval = true)
    TWIN_COL_SELECT,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    TWITTER_SQUARE,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    TWITTER,
    UMBRELLA,
    UNDERLINE,
    UNLINK,
    UNLOCK,
    UPLOAD_ALT,
    UPLOAD,
    USER_CARD,
    USER_CHECK,
    USER_CLOCK,
    USER_HEART,
    USER_STAR,
    USER,
    USERS,
    VAADIN_H,
    VAADIN_V,
    @Deprecated(since = "25.3", forRemoval = true)
    VIEWPORT,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    VIMEO_SQUARE,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    VIMEO,
    VOLUME_DOWN,
    VOLUME_OFF,
    VOLUME_UP,
    VOLUME,
    WALLET,
    WARNING,
    WORKPLACE,
    WRENCH,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    YOUTUBE_SQUARE,
    /**
     * @deprecated Use a replacement from
     *             <a href="https://simpleicons.org">simpleicons.org</a>.
     */
    @Deprecated(since = "25.3", forRemoval = true)
    YOUTUBE;

    /**
     * Creates a new {@link Icon} instance with the icon determined by the name
     * of this instance.
     *
     * @return a new instance of {@link Icon} component
     */
    public Icon create() {
        return new Icon(this);
    }
}

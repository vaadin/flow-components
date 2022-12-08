import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';
// import { extractDateParts, parseDate as _parseDate } from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';

(function () {
    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Date Picker', 'vaadin-date-picker-flow');
    };

    /* helper class for parsing regex from formatted date string */

    class FlowDatePickerPart {
        constructor(initial) {
            this.initial = initial;
            this.index = 0;
            this.value = 0;
        }

        static compare(part1, part2) {
            if (part1.index < part2.index) {
                return -1;
            }
            if (part1.index > part2.index) {
                return 1;
            }
            return 0;
        }
    }
    window.Vaadin.Flow.datepickerConnector = {
        initLazy: datepicker => tryCatchWrapper(function (datepicker) {
            // Check whether the connector was already initialized for the datepicker
            if (datepicker.$connector) {
                return;
            }

            datepicker.$connector = {};

            /* init helper parts for reverse-engineering date-regex */
            datepicker.$connector.dayPart = new FlowDatePickerPart("22");
            datepicker.$connector.monthPart = new FlowDatePickerPart("11");
            datepicker.$connector.yearPart = new FlowDatePickerPart("1987");
            datepicker.$connector.parts = [datepicker.$connector.dayPart, datepicker.$connector.monthPart, datepicker.$connector.yearPart];

            datepicker.addEventListener('blur', tryCatchWrapper(e => {
                if (!e.target.value && e.target.invalid) {
                    console.warn("Invalid value in the DatePicker.");
                }
            }));

            const createLocaleBasedDateFormat = function (locale) {
                try {
                    // Check whether the locale is supported or not
                    new Date().toLocaleDateString(locale);
                } catch (e) {
                    console.warn('The locale is not supported, using default locale setting(en-US).');
                    return 'M/d/yyyy';
                }
            }

            const cleanString = tryCatchWrapper(function (string) {
                // Clear any non ascii characters from the date string,
                // mainly the LEFT-TO-RIGHT MARK.
                // This is a problem for many Microsoft browsers where `toLocaleDateString`
                // adds the LEFT-TO-RIGHT MARK see https://en.wikipedia.org/wiki/Left-to-right_mark
                return string.replace(/[^\x00-\x7F]/g, "");
            });

            const getInputValue = tryCatchWrapper(function () {
                let inputValue = '';
                try {
                    inputValue = datepicker._inputValue;
                } catch (err) {
                    /* component not ready: falling back to stored value */
                    inputValue = datepicker.value || '';
                }
                return inputValue;
            });

            const createLocaleBasedFormatterAndParser = tryCatchWrapper(function (locale) {
                try {
                    // Check whether the locale is supported or not
                    new Date().toLocaleDateString(locale);
                } catch (e) {
                    locale = "en-US";
                    console.warn("The locale is not supported, using default locale setting(en-US).");
                }

                /* create test-string where to extract parsing regex */
                let testDate = new Date(Date.UTC(datepicker.$connector.yearPart.initial, datepicker.$connector.monthPart.initial - 1, datepicker.$connector.dayPart.initial));
                let testString = cleanString(testDate.toLocaleDateString(locale, { timeZone: 'UTC' }));
                datepicker.$connector.parts.forEach(function (part) {
                    part.index = testString.indexOf(part.initial);
                });
                /* sort items to match correct places in regex groups */
                datepicker.$connector.parts.sort(FlowDatePickerPart.compare);
                /* create regex
                * regex will be the date, so that:
                * - day-part is '(\d{1,2})' (1 or 2 digits),
                * - month-part is '(\d{1,2})' (1 or 2 digits),
                * - year-part is '(\d{1,4})' (1 to 4 digits)
                *
                * and everything else is left as is.
                * For example, us date "10/20/2010" => "(\d{1,2})/(\d{1,2})/(\d{1,4})".
                *
                * The sorting part solves that which part is which (for example,
                * here the first part is month, second day and third year)
                *  */
                datepicker.$connector.regex = testString.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&')
                    .replace(datepicker.$connector.dayPart.initial, "(\\d{1,2})")
                    .replace(datepicker.$connector.monthPart.initial, "(\\d{1,2})")
                    .replace(datepicker.$connector.yearPart.initial, "(\\d{1,4})");

                function formatDate(date) {
                    let rawDate = datepicker._parseDate(`${date.year}-${date.month + 1}-${date.day}`);

                    // Workaround for Safari DST offset issue when using Date.toLocaleDateString().
                    // This is needed to keep the correct date in formatted result even if Safari
                    // makes an error of an hour or more in the result with some past dates.
                    // See https://github.com/vaadin/vaadin-date-picker-flow/issues/126#issuecomment-508169514
                    rawDate.setHours(12)

                    return cleanString(rawDate.toLocaleDateString(locale));
                }

                function parseDate(dateString) {
                    dateString = cleanString(dateString);

                    if (dateString.length === 0) {
                        return;
                    }

                    let match = dateString.match(datepicker.$connector.regex);
                    if (match && match.length === 4) {
                        for (let i = 1; i < 4; i++) {
                            datepicker.$connector.parts[i - 1].value = parseInt(match[i]);
                        }
                        return {
                            day: datepicker.$connector.dayPart.value,
                            month: datepicker.$connector.monthPart.value - 1,
                            year: datepicker.$connector.yearPart.value
                        };
                    } else {
                        return false;
                    }
                }

                return {
                    formatDate: formatDate,
                    parseDate: parseDate,
                };
            });


            const createFormatterAndParser = tryCatchWrapper(function (formats) {
                if (!formats || formats.length === 0) {
                    throw new Error('Array of custom date formats is null or empty');
                }

                function getShortYearFormat(format) {
                    if (format.includes('yyyy') && !format.includes('yyyyy')) {
                        return format.replace('yyyy', 'yy');
                    }
                    if (format.includes('YYYY') && !format.includes('YYYYY')) {
                        return format.replace('YYYY', 'YY');
                    }
                    return undefined;
                }

                function isShortYearFormat(format) {
                    if (format.includes('y')) {
                        return !format.includes('yyy');
                    }
                    if (format.includes('Y')) {
                        return !format.includes('YYY');
                    }
                    return false;
                }

                function formatDate(dateParts) {
                    const format = formats[0];
                    const date = _parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);
                    return dateFnsFormat(date, format);
                }

                function parseDate(dateString) {
                    const referenceDate = _getReferenceDate();
                    for (let format of formats) {
                        // We first try to match the date with the shorter version.
                        const shortYearFormat = getShortYearFormat(format);
                        if (shortYearFormat) {
                            const shortYearFormatDate = dateFnsParse(dateString, shortYearFormat, referenceDate);
                            if (dateFnsIsValid(shortYearFormatDate)) {
                                let yearValue = shortYearFormatDate.getFullYear();
                                // The last parsed year check handles the case where a four-digit year is parsed, then formatted
                                // as a two-digit year, and then parsed again. In this case we want to keep the century of the
                                // originally parsed year, instead of using the century of the reference date.
                                if (
                                    datepicker.$connector._lastParsedYear &&
                                    yearValue === datepicker.$connector._lastParsedYear % 100
                                ) {
                                    yearValue = datepicker.$connector._lastParsedYear;
                                }
                                return {
                                    day: shortYearFormatDate.getDate(),
                                    month: shortYearFormatDate.getMonth(),
                                    year: yearValue
                                };
                            }
                        }
                        const date = dateFnsParse(dateString, format, referenceDate);

                        if (dateFnsIsValid(date)) {
                            let yearValue = date.getFullYear();
                            if (
                                datepicker.$connector._lastParsedYear &&
                                yearValue % 100 === datepicker.$connector._lastParsedYear % 100 &&
                                isShortYearFormat(format)
                            ) {
                                yearValue = datepicker.$connector._lastParsedYear;
                            } else {
                                datepicker.$connector._lastParsedYear = yearValue;
                            }
                            return {
                                day: date.getDate(),
                                month: date.getMonth(),
                                year: yearValue
                            };
                        }
                    }
                    datepicker.$connector._lastParsedYear = undefined;
                    return false;
                }

                return {
                    formatDate: formatDate,
                    parseDate: parseDate
                };
            });


            const createCustomFormatBasedFormatterAndParser = tryCatchWrapper(function (formats) {
                if (!formats || formats.length === 0) {
                    throw new Error("Array of custom date formats is null or empty");
                }

                const dateFns = window.Vaadin.Flow.datepickerDateFns;
                if (!dateFns) {
                    throw new Error("Custom date-fns bundle for date picker is not registered at window.Vaadin.Flow.datepickerDateFns");
                }

                function formatDate(dateParts) {
                    const format = formats[0];
                    const date = datepicker._parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

                    return dateFns.format(date, format);
                }

                function parseDate(dateString) {
                    for (let format of formats) {
                        const date = dateFns.parse(dateString, format, new Date());

                        if (dateFns.isValid(date)) {
                            return {day: date.getDate(), month: date.getMonth(), year: date.getFullYear()};
                        }
                    }
                    return false;
                }

                return {
                    formatDate: formatDate,
                    parseDate: parseDate,
                };
            });

            function _getReferenceDate() {
                const { referenceDate } = datepicker.i18n;
                return referenceDate ? new Date(referenceDate.year, referenceDate.month - 1, referenceDate.day) : new Date();
            }

            /**
             * Extracts the basic component parts of a date (day, month and year)
             * to the expected format.
             * @param {!Date} date
             * @return {{day: number, month: number, year: number}}
             */
            // TODO have this from properly npm (package.json for vaadin date picker)
            function extractDateParts(date) {
                return {
                    day: date.getDate(),
                    month: date.getMonth(),
                    year: date.getFullYear(),
                };
            }

            /**
             * Parse date string of one of the following date formats:
             * - ISO 8601 `"YYYY-MM-DD"`
             * - 6-digit extended ISO 8601 `"+YYYYYY-MM-DD"`, `"-YYYYYY-MM-DD"`
             * @param {!string} str Date string to parse
             * @return {Date} Parsed date
             */
            function _parseDate(str) {
                // Parsing with RegExp to ensure correct format
                const parts = /^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/.exec(str);
                if (!parts) {
                    return undefined;
                }

                const date = new Date(0, 0); // Wrong date (1900-01-01), but with midnight in local time
                date.setFullYear(parseInt(parts[1], 10));
                date.setMonth(parseInt(parts[2], 10) - 1);
                date.setDate(parseInt(parts[3], 10));
                return date;
            }

            datepicker.$connector.updateI18n = tryCatchWrapper(function (locale, i18n) {
                // Either use custom formats specified in I18N, or create format from locale
                const hasCustomFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
                if (i18n && i18n.referenceDate) {
                    i18n.referenceDate = extractDateParts(new Date(i18n.referenceDate));
                }
                const usedFormats = hasCustomFormats ? i18n.dateFormats : [createLocaleBasedDateFormat(locale)];
                const formatterAndParser = createFormatterAndParser(usedFormats);

                // Merge current web component I18N settings with new I18N settings and the formatting and parsing functions
                datepicker.i18n = Object.assign({}, datepicker.i18n, i18n, formatterAndParser);
            });
        })(datepicker)
    };
})();

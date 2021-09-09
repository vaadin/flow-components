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

                    if (dateString.length == 0) {
                        return;
                    }

                    let match = dateString.match(datepicker.$connector.regex);
                    if (match && match.length == 4) {
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

            datepicker.$connector.updateI18n = tryCatchWrapper(function (locale, i18n) {
                // Create formatting and parsing functions, either based on custom formats set in i18n object,
                // or based on the locale set in the date picker
                // Custom formats take priority over locale
                const hasDateFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
                const formatterAndParser = hasDateFormats
                    ? createCustomFormatBasedFormatterAndParser(i18n.dateFormats)
                    : locale
                        ? createLocaleBasedFormatterAndParser(locale)
                        : null;

                // Merge current web component I18N settings with new I18N settings and the formatting and parsing functions
                datepicker.i18n = Object.assign({}, datepicker.i18n, i18n, formatterAndParser);
            });
        })(datepicker)
    };
})();

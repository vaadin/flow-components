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

            // Old locale should always be the default vaadin-date-picker component
            // locale {English/US} as we init lazily and the date-picker formats
            // the date using the default i18n settings and we need to use the input
            // value as we may need to parse user input so we can't use the _selectedDate value.
            let oldLocale = "en-us";

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
                } catch(err) {
                    /* component not ready: falling back to stored value */
                    inputValue = datepicker.value || '';
                }
                return inputValue;
            });

            datepicker.$connector.setLocale = tryCatchWrapper(function (locale) {

                try {
                    // Check whether the locale is supported or not
                    new Date().toLocaleDateString(locale);
                } catch (e) {
                    locale = "en-US";
                    console.warn("The locale is not supported, using default locale setting(en-US).");
                }

                let currentDate = false;
                let inputValue = getInputValue();
                if (datepicker.i18n.parseDate !== 'undefined' && inputValue) {
                    /* get current date with old parsing */
                    currentDate = datepicker.i18n.parseDate(inputValue);
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
                * - year-part is '(\d{4})' (4 digits)
                *
                * and everything else is left as is.
                * For example, us date "10/20/2010" => "(\d{1,2})/(\d{1,2})/(\d{4})".
                *
                * The sorting part solves that which part is which (for example,
                * here the first part is month, second day and third year)
                *  */
                datepicker.$connector.regex = testString.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&').replace(datepicker.$connector.dayPart.initial, "(\\d{1,2})").replace(datepicker.$connector.monthPart.initial, "(\\d{1,2})").replace(datepicker.$connector.yearPart.initial, "(\\d{4})");

                datepicker.i18n.formatDate = tryCatchWrapper(function (date) {
                    let rawDate = new Date(Date.UTC(date.year, date.month, date.day));
                    return cleanString(rawDate.toLocaleDateString(locale, { timeZone: 'UTC' }));
                });

                datepicker.i18n.parseDate = tryCatchWrapper(function (dateString) {
                    dateString = cleanString(dateString);

                    if (dateString.length == 0) {
                        return;
                    }

                    let match = dateString.match(datepicker.$connector.regex);
                    if (match && match.length == 4) {
                        for (let i = 1; i < 4; i++) {
                            datepicker.$connector.parts[i-1].value = parseInt(match[i]);
                        }
                        return {
                            day: datepicker.$connector.dayPart.value,
                            month: datepicker.$connector.monthPart.value - 1,
                            year: datepicker.$connector.yearPart.value
                        };
                    }  else {
                        return false;
                    }
                });

                if (inputValue === "") {
                    oldLocale = locale;
                } else if (currentDate) {
                    /* set current date to invoke use of new locale */
                    datepicker._selectedDate = new Date(currentDate.year, currentDate.month, currentDate.day);
                }
            });
        })(datepicker)
    };
})();

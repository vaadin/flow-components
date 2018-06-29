window.Vaadin.Flow.datepickerConnector = {
    initLazy: function (datepicker) {
        // Check whether the connector was already initialized for the datepicker
        if (datepicker.$connector) {
            return;
        }

        datepicker.$connector = {};

        // Old locale should always be the default vaadin-date-picker component
        // locale {English/US} as we init lazily and the date-picker formats
        // the date using the default i18n settings and we need to use the input
        // value as we may need to parse user input so we can't use the _selectedDate value.
        let oldLocale = "en-us";

        datepicker.addEventListener('blur', e => {
            if (!e.target.value && e.target.invalid) {
                console.warn("Invalid value in the DatePicker.");
            }
        });

        const cleanString = function (string) {
            // Clear any non ascii characters from the date string,
            // mainly the LEFT-TO-RIGHT MARK.
            // This is a problem for many Microsoft browsers where `toLocaleDateString`
            // adds the LEFT-TO-RIGHT MARK see https://en.wikipedia.org/wiki/Left-to-right_mark
            return string.replace(/[^\x00-\x7F]/g, "");
        };

        // Create a Date from our dateObject that doesn't contain setters/getters
        const generateDate = function (dateObject) {
            let dateString = `${dateObject.year}-${dateObject.month + 1}-${dateObject.day}`;
            var parts = /^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/.exec(dateString);
            if (!parts) {
                console.warn("Couldn't parse generated date string.");
                return;
            }

            // Wrong date (1900-01-01), but with midnight in local time
            var date = new Date(0, 0);
            date.setFullYear(parseInt(parts[1], 10));
            date.setMonth(parseInt(parts[2], 10) - 1);
            date.setDate(parseInt(parts[3], 10));

            return date;
        };

        const updateFormat = function () {
            let inputValue = datepicker._inputValue || '';
            if (inputValue !== "" && datepicker.i18n.parseDate) {
                let selectedDate = datepicker.i18n.parseDate(inputValue);
                if (!selectedDate) {
                    return;
                }

                datepicker._selectedDate = selectedDate && generateDate(selectedDate);
            }
        };

        datepicker.$connector.setLocale = function (locale) {
            try {
                // Check weather the locale is supported or not
                new Date().toLocaleDateString(locale);
            } catch (e) {
                locale = "en-US";
                console.warn("The locale is not supported, use default locale setting(en-US).");
            }

            datepicker.i18n.formatDate = function (date) {
                let rawDate = new Date(date.year, date.month, date.day);
                return cleanString(rawDate.toLocaleDateString(locale));
            };

            datepicker.i18n.parseDate = function (dateString) {
                if (dateString.length == 0) {
                    return;
                }

                //checking separator which is used in the date
                let regexMatcher = /[0-9]+(.\s?)[0-9]+\1[0-9]+\1?/;
                let match = regexMatcher.exec(dateString);

                if (match === null || match.length != 2) {
                    console.error("There was an error when getting the separator for given date string.");
                    return null;
                } else {
                    var separator = match[1];
                }

                const sample = ["2009", "12", "31"].join(separator);
                const sample_parts = sample.split(separator);
                let targetLocaleDate = cleanString(new Date(sample).toLocaleDateString(oldLocale).toString());

                let date;
                if (targetLocaleDate.startsWith(sample)) {
                    //Date format "YYYY/MM/DD"
                    date = new Date(dateString);
                } else if (targetLocaleDate.startsWith(sample.split(separator).reverse().join(separator))) {
                    //Date format "DD/MM/YYYY"
                    date = new Date(dateString.split(separator).reverse().join(separator));
                } else if (targetLocaleDate.startsWith([sample_parts[1], sample_parts[2], sample_parts[0]].join(separator))) {
                    //Date format "MM/DD/YYYY"
                    const parts = dateString.split(separator);
                    date = new Date([parts[2], parts[0], parts[1]].join(separator));
                } else {
                    console.warn("Selected locale is using unsupported date format, which might affect the parsing date.");
                    date = new Date(dateString);
                }

                oldLocale = locale;

                return {
                    day: date.getDate(),
                    month: date.getMonth(),
                    year: date.getFullYear()
                };
            };

            let inputValue = datepicker._inputValue || '';
            if (inputValue === "") {
                oldLocale = locale;
            } else {
                updateFormat();
            }
        }
    }
}

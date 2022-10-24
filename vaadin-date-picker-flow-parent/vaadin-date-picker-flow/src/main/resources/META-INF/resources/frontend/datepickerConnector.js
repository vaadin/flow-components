import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';
import { calculateYearBasedOnReferenceDate } from '@vaadin/date-picker/src/vaadin-date-picker-helper';

(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Date Picker');
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
    initLazy: (datepicker) =>
      tryCatchWrapper(function (datepicker) {
        // Check whether the connector was already initialized for the datepicker
        if (datepicker.$connector) {
          return;
        }

        datepicker.$connector = {};

        /* init helper parts for reverse-engineering date-regex */
        datepicker.$connector.dayPart = new FlowDatePickerPart('22');
        datepicker.$connector.monthPart = new FlowDatePickerPart('11');
        datepicker.$connector.yearPart = new FlowDatePickerPart('1987');
        datepicker.$connector.parts = [
          datepicker.$connector.dayPart,
          datepicker.$connector.monthPart,
          datepicker.$connector.yearPart
        ];

        datepicker.addEventListener(
          'blur',
          tryCatchWrapper((e) => {
            if (!e.target.value && e.target.invalid) {
              console.warn('Invalid value in the DatePicker.');
            }
          })
        );

        const cleanString = tryCatchWrapper(function (string) {
          // Clear any non ascii characters from the date string,
          // mainly the LEFT-TO-RIGHT MARK.
          // This is a problem for many Microsoft browsers where `toLocaleDateString`
          // adds the LEFT-TO-RIGHT MARK see https://en.wikipedia.org/wiki/Left-to-right_mark
          return string.replace(/[^\x00-\x7F]/g, '');
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

        const createLocaleBasedFormatterAndParser = tryCatchWrapper(function (locale, referenceDate) {
          try {
            // Check whether the locale is supported or not
            new Date().toLocaleDateString(locale);
          } catch (e) {
            locale = 'en-US';
            console.warn('The locale is not supported, using default locale setting(en-US).');
          }

          /* create test-string where to extract parsing regex */
          let testDate = new Date(
            Date.UTC(
              datepicker.$connector.yearPart.initial,
              datepicker.$connector.monthPart.initial - 1,
              datepicker.$connector.dayPart.initial
            )
          );
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
          datepicker.$connector.regex = testString
            .replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&')
            .replace(datepicker.$connector.dayPart.initial, '(\\d{1,2})')
            .replace(datepicker.$connector.monthPart.initial, '(\\d{1,2})')
            .replace(datepicker.$connector.yearPart.initial, '(\\d{1,4})');

          const numberOfLeadingZeroesAddedToSingleDigitYear = (cleanString(new Date(2,11,11)
            .toLocaleDateString(locale)).match(/0/g) || []).length;
          const shouldAddLeadingZeroesToYear = testDate.toLocaleDateString(locale)
            .includes(String(testDate.getFullYear())) && numberOfLeadingZeroesAddedToSingleDigitYear != 3;

          function _addLeadingZeroesToYearOfDate(dateString, date) {
            const dateFullYear = date.getFullYear();
            if (dateFullYear >= 1000 || dateFullYear < 0) {
              return dateString;
            }
            const monthString = (date.getMonth() + 1).toString();
            const dayString = date.getDate().toString();
            let unusedDigit;
            for (let i = 1; i < 10; i++) {
              if (!monthString.includes(i) && !dayString.includes(i)) {
                unusedDigit = i.toString();
                break;
              }
            }
            const numberOfYearDigits = dateFullYear.toString().length;
            const dummyDateYearToUse = unusedDigit.repeat(numberOfYearDigits);
            const countOfZeroesAlreadyAdded = Math.max(numberOfLeadingZeroesAddedToSingleDigitYear + 1
              - numberOfYearDigits, 0);
            const dummyDateStringToSearchFor = '0'.repeat(countOfZeroesAlreadyAdded) + dummyDateYearToUse;
            const dummyDateToUse = new Date();
            dummyDateToUse.setFullYear(Number(dummyDateYearToUse), date.getMonth(), date.getDate());
            const indexOfDate = cleanString(dummyDateToUse.toLocaleDateString(locale))
              .indexOf(dummyDateStringToSearchFor);
            const realDateStringToReplace = '0'.repeat(4 - numberOfYearDigits) + dateFullYear;
            return dateString.substring(0, indexOfDate) + realDateStringToReplace + dateString.substring(indexOfDate
              + dummyDateStringToSearchFor.length, dateString.length);
          }

          function formatDate(date) {
            let rawDate = datepicker._parseDate(`${date.year}-${date.month + 1}-${date.day}`);

            // Workaround for Safari DST offset issue when using Date.toLocaleDateString().
            // This is needed to keep the correct date in formatted result even if Safari
            // makes an error of an hour or more in the result with some past dates.
            // See https://github.com/vaadin/vaadin-date-picker-flow/issues/126#issuecomment-508169514
            rawDate.setHours(12);

            let cleanDateString = cleanString(rawDate.toLocaleDateString(locale));
            if (shouldAddLeadingZeroesToYear) {
              cleanDateString = _addLeadingZeroesToYearOfDate(cleanDateString, rawDate);
            }
            return cleanDateString;
          }

          function parseDate(dateString) {
            dateString = cleanString(dateString);

            if (dateString.length == 0) {
              this._lastParsedShortYear = undefined;
              return;
            }

            const match = dateString.match(datepicker.$connector.regex);
            if (!match || match.length != 4) {
              this._lastParsedShortYear = undefined;
              return false;
            }

            for (let i = 1; i < 4; i++) {
              datepicker.$connector.parts[i - 1].value = parseInt(match[i]);
            }

            let yearValue = datepicker.$connector.yearPart.value;
            const yearMatch = match[datepicker.$connector.parts.indexOf(datepicker.$connector.yearPart) + 1];
            // If the provided year has less than 3 digits, the value is calculated using a reference date.
            // The last parsed short year check handles the case when a date with an actual year value is provided
            // with zero padding, but then got reformatted without the zeroes and parsed again.
            if (yearMatch.length < 3 && yearValue >= 0 && yearValue !== this._lastParsedShortYear) {
              yearValue = calculateYearBasedOnReferenceDate(referenceDate, yearValue,
                  datepicker.$connector.monthPart.value - 1, datepicker.$connector.dayPart.value);
            }
            this._lastParsedShortYear = yearValue % 100;
            datepicker.$connector.yearPart.value = yearValue;
            return {
              day: datepicker.$connector.dayPart.value,
              month: datepicker.$connector.monthPart.value - 1,
              year: datepicker.$connector.yearPart.value
            };
          }

          return {
            formatDate: formatDate,
            parseDate: parseDate
          };
        });

        const createCustomFormatBasedFormatterAndParser = tryCatchWrapper(function (formats, referenceDate) {
          if (!formats || formats.length === 0) {
            throw new Error('Array of custom date formats is null or empty');
          }

          function _getShorterFormat(format) {
            if (format.includes('yyyy') && !format.includes('yyyyy')) {
              return format.replace('yyyy', 'yy');
            }
            if (format.includes('YYYY') && !format.includes('YYYYY')) {
              return format.replace('YYYY', 'YY');
            }
            return undefined;
          }

          function _isShortFormat(format) {
            return !format.includes('yyy') && !format.includes('YYY');
          }

          function formatDate(dateParts) {
            const format = formats[0];
            const date = datepicker._parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

            return dateFnsFormat(date, format);
          }

          function parseDate(dateString) {
            for (let format of formats) {
              // We first try to match the date with the shorter version.
              const shorterFormat = _getShorterFormat(format);
              if (shorterFormat) {
                const shorterFormatDate = dateFnsParse(dateString, shorterFormat, referenceDate);
                if (dateFnsIsValid(shorterFormatDate)) {
                  let yearValue = shorterFormatDate.getFullYear();
                  // The last parsed year check handles the case when a date with an actual year value is provided
                  // with zero padding, but then got reformatted without the zeroes and parsed again.
                  if (this._lastParsedYear && yearValue == this._lastParsedYear % 100) {
                    yearValue = this._lastParsedYear;
                  }
                  return {
                    day: shorterFormatDate.getDate(),
                    month: shorterFormatDate.getMonth(),
                    year: yearValue
                  };
                }
              }
              const date = dateFnsParse(dateString, format, referenceDate);

              if (dateFnsIsValid(date)) {
                let yearValue = date.getFullYear();
                if (this._lastParsedYear && yearValue % 100 == this._lastParsedYear % 100 && _isShortFormat(format)) {
                  yearValue = this._lastParsedYear;
                } else {
                  this._lastParsedYear = yearValue;
                }
                return {
                  day: date.getDate(),
                  month: date.getMonth(),
                  year: yearValue
                };
              }
            }
            this._lastParsedYear = undefined;
            return false;
          }

          return {
            formatDate: formatDate,
            parseDate: parseDate
          };
        });

        datepicker.$connector.updateI18n = tryCatchWrapper(function (locale, i18n) {
          // Create formatting and parsing functions, either based on custom formats set in i18n object,
          // or based on the locale set in the date picker
          // Custom formats take priority over locale
          const hasDateFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
          const referenceDate = i18n && i18n.referenceDate ? new Date(i18n.referenceDate) : new Date();
          const formatterAndParser = hasDateFormats
            ? createCustomFormatBasedFormatterAndParser(i18n.dateFormats, referenceDate)
            : locale
            ? createLocaleBasedFormatterAndParser(locale, referenceDate)
            : null;

          // Merge current web component I18N settings with new I18N settings and the formatting and parsing functions
          datepicker.i18n = Object.assign({}, datepicker.i18n, i18n, formatterAndParser);
        });
      })(datepicker)
  };
})();

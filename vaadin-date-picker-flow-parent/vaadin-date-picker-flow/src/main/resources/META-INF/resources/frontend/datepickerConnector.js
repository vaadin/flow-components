import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';
import { parseDate as _parseDate } from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';

(function () {
  const tryCatchWrapper = function (callback) {
    return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Date Picker');
  };

  window.Vaadin.Flow.datepickerConnector = {
    initLazy: (datepicker) =>
      tryCatchWrapper(function (datepicker) {
        // Check whether the connector was already initialized for the datepicker
        if (datepicker.$connector) {
          return;
        }

        datepicker.$connector = {};

        datepicker.addEventListener(
          'blur',
          tryCatchWrapper((e) => {
            if (!e.target.value && e.target.invalid) {
              console.warn('Invalid value in the DatePicker.');
            }
          })
        );

        const createLocaleBasedDateFormat = function (locale) {
          try {
            // Check whether the locale is supported or not
            new Date().toLocaleDateString(locale);
          } catch (e) {
            console.warn('The locale is not supported, using default locale setting(en-US).');
            return 'M/d/y';
          }

          // format test date and convert to date-fns pattern
          const testDate = new Date(Date.UTC(1234, 4, 6));
          let pattern = testDate.toLocaleDateString(locale, { timeZone: 'UTC' });
          pattern = pattern
            // escape date-fns pattern letters by enclosing them in single quotes
            .replace(/([a-zA-Z]+)/g, "'$1'")
            // insert date placeholder
            .replace('06', 'dd')
            .replace('6', 'd')
            // insert month placeholder
            .replace('05', 'MM')
            .replace('5', 'M')
            // insert year placeholder
            .replace('1234', 'y');

          const isValidPattern = pattern.includes('d') && pattern.includes('M') && pattern.includes('y');
          if (!isValidPattern) {
            console.warn('The locale is not supported, using default locale setting(en-US).');
            return 'M/d/y';
          }

          return pattern;
        };

        const createFormatterAndParser = tryCatchWrapper(function (formats) {
          if (!formats || formats.length === 0) {
            throw new Error('Array of custom date formats is null or empty');
          }

          function formatDate(dateParts) {
            const format = formats[0];
            const date = _parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

            return dateFnsFormat(date, format);
          }

          function parseDate(dateString) {
            for (let format of formats) {
              const date = dateFnsParse(dateString, format, new Date());

              if (dateFnsIsValid(date)) {
                return {
                  day: date.getDate(),
                  month: date.getMonth(),
                  year: date.getFullYear()
                };
              }
            }
            return false;
          }

          return {
            formatDate: formatDate,
            parseDate: parseDate
          };
        });

        datepicker.$connector.updateI18n = tryCatchWrapper(function (locale, i18n) {
          // Either use custom formats specified in I18N, or create format from locale
          const hasCustomFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
          const usedFormats = hasCustomFormats ? i18n.dateFormats : [createLocaleBasedDateFormat(locale)];
          const formatterAndParser = createFormatterAndParser(usedFormats);

          // Merge current web component I18N settings with new I18N settings and the formatting and parsing functions
          datepicker.i18n = Object.assign({}, datepicker.i18n, i18n, formatterAndParser);
        });
      })(datepicker)
  };
})();

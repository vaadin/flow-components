import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';

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

        const cleanString = tryCatchWrapper(function (string) {
          // Clear any non ascii characters from the date string,
          // mainly the LEFT-TO-RIGHT MARK.
          // This is a problem for many Microsoft browsers where `toLocaleDateString`
          // adds the LEFT-TO-RIGHT MARK see https://en.wikipedia.org/wiki/Left-to-right_mark
          return string.replace(/[^\x00-\x7F]/g, '');
        });

        const createLocaleBasedDateFormat = tryCatchWrapper(function(locale) {
          try {
            // Check whether the locale is supported or not
            new Date().toLocaleDateString(locale);
          } catch (e) {
            locale = 'en-US';
            console.warn('The locale is not supported, using default locale setting(en-US).');
          }

          // format test date and convert to date-fns pattern
          const testDate = new Date(Date.UTC(1234, 4, 6));
          let pattern = cleanString(testDate.toLocaleDateString(locale, { timeZone: 'UTC' }));

          if (/06/.test(pattern)) {
            pattern = pattern.replace('06', 'dd')
          } else {
            pattern = pattern.replace('6', 'd')
          }
          if (/05/.test(pattern)) {
            pattern = pattern.replace('05', 'MM')
          } else {
            pattern = pattern.replace('5', 'M')
          }
          pattern = pattern.replace('1234', 'y')

          return pattern;
        });

        const createFormatterAndParser = tryCatchWrapper(function (formats) {
          if (!formats || formats.length === 0) {
            throw new Error('Array of custom date formats is null or empty');
          }

          function formatDate(dateParts) {
            const format = formats[0];
            const date = datepicker._parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

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

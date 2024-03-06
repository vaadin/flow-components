import { expect, fixtureSync } from '@open-wc/testing';
import dateFnsFormat from 'date-fns/format';
import { DatePickerDate } from '@vaadin/date-picker';
import { init, extractDateParts, datepickerConnector, type FlowDatePicker } from './shared.js';

describe('date-picker connector', () => {
  let datePicker: FlowDatePicker;

  beforeEach(() => {
    datePicker = fixtureSync('<vaadin-date-picker></vaadin-date-picker>');
    init(datePicker);
  });

  it('should not reinitialize the connector', () => {
    const connector = datePicker.$connector;
    datepickerConnector.initLazy(datePicker);
    expect(datePicker.$connector).to.equal(connector);
  });

  const DATE = new Date(Date.UTC(2023, 11, 1));
  const DATE_OBJ = extractDateParts(DATE);

  [
    // Day, month, year
    'dd.MM.yyyy',
    'ddMMyyyy',
    'yyyy-MM-dd',
    'MM/dd/yyyy',
    'ddMMyy',
    // Day and month only
    'dd.MM',
    'ddMM',
    'dd-MM',
    'MM/dd',
    'M/dd',
    // Day only
    'dd'
  ].forEach((format) => {
    describe(`${format} format`, () => {
      let dateStr;
      let dateObj = { ...DATE_OBJ };

      beforeEach(() => {
        dateStr = dateFnsFormat(DATE, format);
        datePicker.$connector.updateI18n('en-US', { dateFormats: [format] });

        // No year specified assumes current year.
        if (!format.includes('y') && !format.includes('Y')) {
          dateObj.year = new Date().getFullYear();
        }

        // Days only format assumes current month.
        if (format === 'dd') {
          dateObj.month = new Date().getMonth();
        }
      });

      it(`should format date using ${format} format`, () => {
        expect(datePicker.i18n.formatDate(dateObj)).to.equal(dateStr);
      });

      it(`should parse date using ${format} format`, () => {
        expect(datePicker.i18n.parseDate(dateStr)).to.eql(dateObj);
      });
    });
  });

  describe('reference date', () => {
    const DATE = '10-01-14';

    [
      { referenceDate: '1940-01-01', year: 1914 },
      { referenceDate: '1990-01-01', year: 2014 }
    ].forEach(({ referenceDate, year }) => {
      it(`should use ${referenceDate} reference date with short year format`, () => {
        datePicker.$connector.updateI18n('en-US', { dateFormats: ['dd-MM-yy'], referenceDate });
        const result = datePicker.i18n.parseDate(DATE) as DatePickerDate;
        expect(result.year).to.equal(year);
      });

      it(`should not use ${referenceDate} reference date with format without years`, () => {
        datePicker.$connector.updateI18n('en-US', { dateFormats: ['dd-MM'], referenceDate });
        const result = datePicker.i18n.parseDate(DATE.slice(0, 5)) as DatePickerDate;
        expect(result.year).to.equal(new Date().getFullYear());
      });
    });
  });
});

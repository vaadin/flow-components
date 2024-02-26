import { expect, fixtureSync } from '@open-wc/testing';
import { init, datepickerConnector, type DatePickerDate, type FlowDatePicker } from './shared.js';

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

  // Use current year to not hardcode it
  const YEAR = new Date().getFullYear();

  [
    // Day, month, year
    ['dd.MM.yyyy', `31.01.${YEAR}`],
    ['ddMMyyyy', `3101${YEAR}`],
    ['yyyy-MM-dd', `${YEAR}-01-31`],
    ['MM/dd/yyyy', `01/31/${YEAR}`],
    ['ddMMyy', `3101${YEAR - 2000}`],
    // Day and month only
    ['dd.MM', '31.01'],
    ['ddMM', '3101'],
    ['dd-MM', '31-01'],
    ['MM/dd', '01/31'],
    ['M/dd', '1/31'],
  ].forEach(([format, date]) => {
    it(`should parse date using ${format} format`, () => {
      datePicker.$connector.updateI18n('en-US', { dateFormats: [format] });

      const { day, month, year } = datePicker.i18n.parseDate(date) as DatePickerDate;
      expect(day).to.be.equal(31);
      expect(month).to.be.equal(0);
      expect(year).to.be.equal(YEAR);
    });
  });
});

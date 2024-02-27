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
  const YYYY = new Date().getFullYear();
  const YY = YYYY - 2000;

  // Get 0 based month to not hardcode it
  const MONTH = new Date().getMonth();

  const M = MONTH + 1;
  const MM = `${M}`.length == 1 ? `0${M}` : `${M}`;

  const DD = 15;

  [
    // Day, month, year
    ['dd.MM.yyyy', `${DD}.${MM}.${YYYY}`],
    ['ddMMyyyy', `${DD}${MM}${YYYY}`],
    ['yyyy-MM-dd', `${YYYY}-${MM}-${DD}`],
    ['MM/dd/yyyy', `${MM}/${DD}/${YYYY}`],
    ['ddMMyy', `${DD}${MM}${YY}`],
    // Day and month only
    ['dd.MM', `${DD}.${MM}`],
    ['ddMM', `${DD}${MM}`],
    ['dd-MM', `${DD}-${MM}`],
    ['MM/dd', `${MM}/${DD}`],
    ['M/dd', `${M}/${DD}`],
    // Day only
    ['dd', `${DD}`]
  ].forEach(([format, date]) => {
    it(`should parse date using ${format} format`, () => {
      datePicker.$connector.updateI18n('en-US', { dateFormats: [format] });

      const { day, month, year } = datePicker.i18n.parseDate(date) as DatePickerDate;
      expect(day).to.be.equal(DD);
      expect(month).to.be.equal(MONTH);
      expect(year).to.be.equal(YYYY);
    });
  });
});

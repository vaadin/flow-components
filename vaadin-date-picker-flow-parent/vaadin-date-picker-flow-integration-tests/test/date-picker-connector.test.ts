import { expect, fixtureSync } from '@open-wc/testing';
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

  const DATE = new Date();
  const DATE_OBJ = extractDateParts(DATE);

  const D = DATE_OBJ.day;
  const DD = `${D}`.length == 1 ? `0${D}` : `${D}`;

  const M = DATE_OBJ.month + 1;
  const MM = `${M}`.length == 1 ? `0${M}` : `${M}`;

  const YYYY = DATE_OBJ.year;
  const YY = `${YYYY}`.slice(2);

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
    it(`should format date using ${format} format`, () => {
      datePicker.$connector.updateI18n('en-US', { dateFormats: [format] });

      expect(datePicker.i18n.formatDate(DATE_OBJ)).to.equal(date);
    });

    it(`should parse date using ${format} format`, () => {
      datePicker.$connector.updateI18n('en-US', { dateFormats: [format] });

      expect(datePicker.i18n.parseDate(date)).to.eql(DATE_OBJ);
    });
  });
});

import { expect, fixtureSync } from '@open-wc/testing';
import dateFnsFormat from 'date-fns/format';
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
      let date;

      beforeEach(() => {
        date = dateFnsFormat(DATE, format);
        datePicker.$connector.updateI18n('en-US', { dateFormats: [format] });
      });

      it(`should format date using ${format} format`, () => {
        expect(datePicker.i18n.formatDate(DATE_OBJ)).to.equal(date);
      });

      it(`should parse date using ${format} format`, () => {
        expect(datePicker.i18n.parseDate(date)).to.eql(DATE_OBJ);
      });
    });
  });
});

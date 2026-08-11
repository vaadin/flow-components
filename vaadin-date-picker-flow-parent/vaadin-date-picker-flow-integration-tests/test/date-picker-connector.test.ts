import { expect } from 'chai';
import { fixtureSync } from '@vaadin/testing-helpers';
import * as sinon from 'sinon';
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

  describe('date metadata', () => {
    function isDateDisabled(year: number, month: number, day: number): boolean {
      return datePicker.isDateDisabled!({ year, month, day });
    }

    it('should not set isDateDisabled when no static rules are configured', () => {
      datePicker.$connector.setDateMetadataConfig({ disabledDates: [], disabledWeekdays: [] });
      expect(datePicker.isDateDisabled).to.be.undefined;
    });

    it('should disable dates from the fixed list', () => {
      datePicker.$connector.setDateMetadataConfig({
        disabledDates: [
          [2024, 0, 2],
          [2024, 0, 4]
        ]
      });
      expect(isDateDisabled(2024, 0, 2)).to.be.true;
      expect(isDateDisabled(2024, 0, 4)).to.be.true;
      expect(isDateDisabled(2024, 0, 3)).to.be.false;
    });

    it('should disable dates from the weekday list', () => {
      // Monday = 1 and Sunday = 7, the latter being 0 in `Date.prototype.getDay()`.
      datePicker.$connector.setDateMetadataConfig({ disabledWeekdays: [1, 7] });
      expect(isDateDisabled(2024, 0, 1)).to.be.true; // Monday
      expect(isDateDisabled(2024, 0, 7)).to.be.true; // Sunday
      expect(isDateDisabled(2024, 0, 8)).to.be.true; // Monday
      expect(isDateDisabled(2024, 0, 6)).to.be.false; // Saturday
      expect(isDateDisabled(2024, 0, 2)).to.be.false; // Tuesday
    });

    it('should disable dates from the fixed list and the weekday list together', () => {
      datePicker.$connector.setDateMetadataConfig({
        disabledDates: [[2024, 0, 2]],
        disabledWeekdays: [7]
      });
      expect(isDateDisabled(2024, 0, 2)).to.be.true; // Tuesday, from the fixed list
      expect(isDateDisabled(2024, 0, 7)).to.be.true; // Sunday, from the weekday list
      expect(isDateDisabled(2024, 0, 3)).to.be.false; // Wednesday, neither
    });

    it('should compute weekdays correctly for years below 100', () => {
      // Year 50 January 1st is a Saturday, while `new Date(50, 0, 1)` would map to
      // 1950-01-01, which is a Sunday.
      datePicker.$connector.setDateMetadataConfig({ disabledWeekdays: [6] });
      expect(isDateDisabled(50, 0, 1)).to.be.true;

      datePicker.$connector.setDateMetadataConfig({ disabledWeekdays: [7] });
      expect(isDateDisabled(50, 0, 1)).to.be.false;
    });

    it('should replace isDateDisabled when the config changes', () => {
      datePicker.$connector.setDateMetadataConfig({ disabledDates: [[2024, 0, 2]] });
      expect(isDateDisabled(2024, 0, 2)).to.be.true;

      datePicker.$connector.setDateMetadataConfig({ disabledDates: [[2024, 0, 3]] });
      expect(isDateDisabled(2024, 0, 3)).to.be.true;
      expect(isDateDisabled(2024, 0, 2)).to.be.false;
    });

    it('should clear isDateDisabled when the config becomes empty', () => {
      datePicker.$connector.setDateMetadataConfig({
        disabledDates: [[2024, 0, 2]],
        disabledWeekdays: [7]
      });
      expect(datePicker.isDateDisabled).to.be.a('function');

      datePicker.$connector.setDateMetadataConfig({});
      expect(datePicker.isDateDisabled).to.be.undefined;
    });

    // The range the web component would pass for a whole year, with 0-based months.
    const RANGE = {
      start: { year: 2024, month: 0, day: 1 },
      end: { year: 2024, month: 11, day: 31 }
    };

    it('should not set dateMetadataProvider when hasProvider is false', () => {
      datePicker.$connector.setDateMetadataConfig({ hasProvider: false });
      expect(datePicker.dateMetadataProvider).to.be.null;
    });

    it('should keep the same dateMetadataProvider reference across config updates', () => {
      // The web component compares the provider by reference and clears its cache when a new
      // function is assigned, so every config update has to reuse the same function object.
      datePicker.$connector.setDateMetadataConfig({ disabledDates: [[2024, 0, 2]], hasProvider: true });
      const provider = datePicker.dateMetadataProvider;
      expect(provider).to.be.a('function');

      datePicker.$connector.setDateMetadataConfig({ disabledDates: [[2024, 0, 3]], hasProvider: true });
      expect(datePicker.dateMetadataProvider).to.equal(provider);
    });

    it('should set dateMetadataProvider to null when hasProvider becomes false', () => {
      datePicker.$connector.setDateMetadataConfig({ hasProvider: true });
      expect(datePicker.dateMetadataProvider).to.be.a('function');

      datePicker.$connector.setDateMetadataConfig({ hasProvider: false });
      expect(datePicker.dateMetadataProvider).to.be.null;
    });

    it('should call $server.requestDateMetadata with 0-based months', () => {
      const requestDateMetadata = sinon.stub().resolves([]);
      datePicker.$server = { requestDateMetadata };
      datePicker.$connector.setDateMetadataConfig({ hasProvider: true });

      datePicker.dateMetadataProvider!(RANGE);

      expect(requestDateMetadata).to.be.calledOnce;
      expect(requestDateMetadata).to.be.calledWithExactly(2024, 0, 1, 2024, 11, 31);
    });

    it('should resolve the provider with the server response unchanged', async () => {
      const metadata = [{ year: 2024, month: 0, day: 2, disabled: true }];
      datePicker.$server = { requestDateMetadata: sinon.stub().resolves(metadata) };
      datePicker.$connector.setDateMetadataConfig({ hasProvider: true });

      const result = await datePicker.dateMetadataProvider!(RANGE);
      expect(result).to.equal(metadata);
    });

    it('should reject the provider when $server is unavailable', () => {
      datePicker.$connector.setDateMetadataConfig({ hasProvider: true });

      // The connector throws synchronously, which the web component handles like a rejection:
      // it drops the months and requests them again on the next navigation.
      expect(() => datePicker.dateMetadataProvider!(RANGE)).to.throw(
        'Date metadata requested before the server connection was ready'
      );
    });
  });
});

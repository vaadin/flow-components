import { expect, fixtureSync } from '@open-wc/testing';
import { comboBoxConnector, FlowComboBox, init, plainItems, rendererItems } from './shared.ts';
import '@vaadin/combo-box';
import * as sinon from 'sinon';

describe('combo-box connector', () => {
  let comboBox: FlowComboBox;

  beforeEach(() => {
    comboBox = fixtureSync('<vaadin-combo-box></vaadin-combo-box>');
    init(comboBox);
  });

  it('should not reinitialize the connector', () => {
    const connector = comboBox.$connector;
    comboBoxConnector.initLazy(comboBox);
    expect(comboBox.$connector).to.equal(connector);
  });

  describe('filter debouncing', () => {
    let clock: sinon.SinonFakeTimers;

    beforeEach(async () => {
      clock = sinon.useFakeTimers({
        toFake: ['setTimeout', 'clearTimeout']
      });
    });

    afterEach(() => {
      clock.restore();
    });

    it('should debounce filter requests with default timeout', () => {
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'a' }, () => {});
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;

      comboBox.$server.setViewportRange.resetHistory();

      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'ab' }, () => {});
      clock.tick(250);
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'abc' }, () => {});
      clock.tick(250);
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(250);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });

    it('should debounce filter requests with custom timeout', () => {
      comboBox._filterTimeout = 1000;

      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'a' }, () => {});
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.not.called;
      clock.tick(500);
      expect(comboBox.$server.setViewportRange).to.be.calledOnce;
    });

    it('should cancel filter request when the connector is reset', () => {
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: 'test' }, () => {});
      expect(comboBox._filterDebouncer).to.exist;

      comboBox.$connector.reset();
      expect(comboBox._filterDebouncer).to.not.exist;

      clock.tick(600);

      expect(comboBox.$server.setViewportRange).to.not.be.called;
    });
  });

  describe('non-contiguous page requests', () => {
    function commitPage0(items: object[] = plainItems(0, comboBox.pageSize)): void {
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: '' }, () => {});
      comboBox.$connector.set(0, items, '');
      // The WC's data-provider-controller would normally populate
      // filteredItems via the dataProvider callback; mirror that so the
      // connector's eviction logic (which inspects filteredItems[page *
      // pageSize] to detect ComponentRenderer-rendered items) operates
      // on realistic state.
      for (let i = 0; i < items.length; i++) {
        comboBox.filteredItems[i] = items[i];
      }
      comboBox.$connector.confirm(1, '');
    }

    it('covers all pending pages in one setViewportRange when multiple non-contiguous fetches batch', () => {
      // Initial open commits page 0.
      commitPage0();
      comboBox.$server.setViewportRange.resetHistory();

      // Two back-to-back non-contiguous fetches — what
      // `_scroller.scrollIntoView(N)` produces in one rAF batch when
      // the visible buffer spans two adjacent pages.
      comboBox.dataProvider!({ page: 5, pageSize: comboBox.pageSize, filter: '' }, () => {});
      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});

      // The latest RPC must cover BOTH pages (250..349). With per-page
      // RPCs, the server's last-write-wins semantics drop page 5 and
      // its callback never fires → combo-box stuck on loading=true.
      expect(comboBox.$server.setViewportRange.lastCall).to.be.calledWith(250, 100, '');
    });

    it('does not extend the range across already-committed pages', () => {
      // After page 0 commits, requesting a single far page must not
      // drag page 0 back into the requested range. (Regression guard
      // for `commitPage` clearing its `pageCallbacks` entry.)
      commitPage0();
      comboBox.$server.setViewportRange.resetHistory();

      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});

      expect(comboBox.$server.setViewportRange).to.be.calledOnceWith(300, 50, '');
    });

    it('evicts ComponentRenderer-rendered committed pages outside the new range', () => {
      // Page 0 with renderer items (each has a `*_nodeid` property).
      // The server passivates the rendered components when items leave
      // the active set; the connector must reset those filteredItems
      // to placeholders so the next scroll-back re-fetches with fresh
      // node ids.
      commitPage0(rendererItems(0, comboBox.pageSize));
      expect((comboBox.filteredItems[0] as { label: string }).label).to.equal('Item 0');

      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});

      expect(comboBox.filteredItems[0]).to.be.instanceOf((window as any).Vaadin.ComboBoxPlaceholder);
    });

    it('keeps plain-data committed pages outside the new range', () => {
      // No `*_nodeid` → no server-side components → no eviction.
      commitPage0();

      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});

      expect((comboBox.filteredItems[0] as { label: string }).label).to.equal('Item 0');
    });
  });
});

import { expect, fixtureSync } from '@open-wc/testing';
import { comboBoxConnector, FlowComboBox, init, plainItems } from './shared.ts';
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
      // eviction-via-placeholder assertion sees realistic state.
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

    it('evicts committed pages outside the new range when a renderer is set', () => {
      // Renderers create server-side rendering state per item; that
      // state gets passivated when items leave the KeyMapper's active
      // set. The connector evicts the committed page so the next
      // scroll-back re-fetches with fresh ids.
      comboBox.renderer = () => {};
      commitPage0();
      expect((comboBox.filteredItems[0] as { label: string }).label).to.equal('Item 0');

      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});

      expect(comboBox.filteredItems[0]).to.be.instanceOf((window as any).Vaadin.ComboBoxPlaceholder);
    });

    it('keeps committed pages outside the new range when no renderer is set', () => {
      // Without a renderer there is no server-side rendering state to
      // invalidate, so committed pages stay valid in the cache.
      commitPage0();

      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});

      expect((comboBox.filteredItems[0] as { label: string }).label).to.equal('Item 0');
    });

    it('does not evict the page containing the focused index', () => {
      // The WC's `scrollIntoView` runs `_visibleItemsCount`, which
      // re-anchors the virtualizer at index 0. Any rendered placeholder
      // there fires `index-requested`, which arrives at the connector
      // as a request for page 0 — and the focused page (containing
      // `comboBox._focusedIndex`) must not be evicted as a side effect.
      comboBox.renderer = () => {};
      commitPage0();
      // Pretend page 6 is committed and currently the focused page.
      const items = plainItems(300, comboBox.pageSize);
      comboBox.dataProvider!({ page: 6, pageSize: comboBox.pageSize, filter: '' }, () => {});
      comboBox.$connector.set(300, items, '');
      for (let i = 0; i < items.length; i++) {
        comboBox.filteredItems[300 + i] = items[i];
      }
      comboBox.$connector.confirm(2, '');
      comboBox._focusedIndex = 300;
      comboBox.$server.setViewportRange.resetHistory();

      // A page-0 re-fetch arrives. The new range is page 0 alone, so
      // page 6 would otherwise be evicted — but it's the focused page.
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: '' }, () => {});

      expect((comboBox.filteredItems[300] as { label: string }).label).to.equal('Item 300');
    });

    it('drops the farthest pending page when the bounding box exceeds the cap', () => {
      // Simulates a deferred page-0 re-fetch racing a deep scrollToIndex
      // jump: page 0 and page 100 are both pending, bounding box would
      // be 5050 items. The connector evicts the farthest pending page
      // and recurses so the resulting RPC stays within the cap.
      commitPage0();
      comboBox.$server.setViewportRange.resetHistory();

      // Stage page 0 as pending, then request page 100. After commitPage0
      // page 0 is committed, so first pretend a re-fetch is in flight.
      comboBox.dataProvider!({ page: 0, pageSize: comboBox.pageSize, filter: '' }, () => {});
      comboBox.dataProvider!({ page: 100, pageSize: comboBox.pageSize, filter: '' }, () => {});

      // The latest RPC must NOT cover all 100 pages — it should be a
      // single-page request (50 items) for page 100.
      const last = comboBox.$server.setViewportRange.lastCall;
      expect(last.args[1]).to.be.at.most(500);
    });
  });
});

import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getBodyCell } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - scroll to item', () => {
  let grid: FlowGrid;
  let table: HTMLElement;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid style="height: 400px">
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
      <style>
        vaadin-grid::part(cell) {
          min-height: 36px;
        }
      </style>
    `);

    init(grid);

    const items = Array.from({ length: 200 }, (_, i) => ({ key: `${i}`, name: `name-${i}` }));
    setRootItems(grid.$connector, items);
    await nextFrame();

    table = grid.shadowRoot!.querySelector('#table')!;
  });

  it('should scroll to an item outside the viewport', async () => {
    grid.$connector.scrollToItem('100', 100);
    await nextFrame();
    expect(getBodyCell(grid, 100, 0)).to.exist;
  });

  it('should not scroll when the item is already fully in viewport', async () => {
    grid.scrollToIndex(20);
    await nextFrame();
    const scrollTopBefore = table.scrollTop;

    // An item a couple of rows below the first visible one is fully visible
    grid.$connector.scrollToItem('22', 22);
    await nextFrame();
    expect(table.scrollTop).to.equal(scrollTopBefore);
  });

  it('should scroll to an item that is rendered but not fully in viewport', async () => {
    grid.scrollToIndex(20);
    await nextFrame();
    const scrollTopBefore = table.scrollTop;

    // Find the last rendered row index. The row is rendered in the scroll
    // buffer below the visible viewport.
    let lastRenderedIndex = 20;
    while (getBodyCell(grid, lastRenderedIndex + 1, 0)) {
      lastRenderedIndex += 1;
    }

    grid.$connector.scrollToItem(`${lastRenderedIndex}`, lastRenderedIndex);
    await nextFrame();
    expect(table.scrollTop).to.be.greaterThan(scrollTopBefore);
  });
});

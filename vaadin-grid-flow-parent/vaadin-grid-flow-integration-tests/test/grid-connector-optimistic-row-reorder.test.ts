import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getBodyCellText, clear } from './shared.js';
import type { FlowGrid, Item } from './shared.js';
import type { GridSorter } from '@vaadin/grid/src/vaadin-grid-sorter.js';

describe('grid connector - optimistic row reorder', () => {
  let grid: FlowGrid;
  let items: Item[];

  function createItems(count: number): Item[] {
    return Array.from({ length: count }, (_, i) => ({ key: `${i}`, name: `item ${i}` }));
  }

  function itemsInOrder(keys: string[]): Item[] {
    return keys.map((key) => items.find((item) => item.key === key)!);
  }

  function getRenderedNames(count: number): (string | null)[] {
    return Array.from({ length: count }, (_, i) => getBodyCellText(grid, i, 0));
  }

  function drag(draggedItems: Item[]) {
    grid.dispatchEvent(
      new CustomEvent('grid-dragstart', {
        detail: { draggedItems, setDragData: () => {}, setDraggedItemsCount: () => {} }
      })
    );
  }

  function drop(dropTargetItem: Item | undefined, dropLocation: string) {
    grid.dispatchEvent(new CustomEvent('grid-drop', { detail: { dropTargetItem, dropLocation, dragData: [] } }));
    grid.dispatchEvent(new CustomEvent('grid-dragend'));
  }

  // Simulates the server response to a drop: the handled drop count goes up,
  // then the data communicator sends the viewport in the server order
  function serverResponse(keys: string[]) {
    grid.__dropsHandled = (grid.__dropsHandled ?? 0) + 1;
    grid.$connector.set(0, itemsInOrder(keys));
    grid.$connector.confirm(1);
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);
    init(grid);
    items = createItems(5);
    setRootItems(grid.$connector, items);
    grid.__optimisticRowReorder = true;
    await nextFrame();
  });

  it('should move the dragged row below the target row', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    expect(getRenderedNames(5)).to.eql(['item 1', 'item 2', 'item 0', 'item 3', 'item 4']);
  });

  it('should move the dragged row above the target row', () => {
    drag([items[4]]);
    drop(items[1], 'above');
    expect(getRenderedNames(5)).to.eql(['item 0', 'item 4', 'item 1', 'item 2', 'item 3']);
  });

  it('should move multiple dragged rows in their current order', () => {
    drag([items[3], items[0]]);
    drop(items[4], 'below');
    expect(getRenderedNames(5)).to.eql(['item 1', 'item 2', 'item 4', 'item 0', 'item 3']);
  });

  it('should keep the order after dragend re-renders the rows', async () => {
    drag([items[0]]);
    drop(items[2], 'below');
    grid.requestContentUpdate();
    await nextFrame();
    expect(getRenderedNames(3)).to.eql(['item 1', 'item 2', 'item 0']);
  });

  it('should not move rows when optimistic row reorder is off', () => {
    grid.__optimisticRowReorder = false;
    drag([items[0]]);
    drop(items[2], 'below');
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });

  ['on-top', 'empty'].forEach((location) => {
    it(`should not move rows for drop location ${location}`, () => {
      drag([items[0]]);
      drop(location === 'empty' ? undefined : items[2], location);
      expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
    });
  });

  it('should not move rows when dropped on a dragged row', () => {
    drag([items[0], items[1]]);
    drop(items[1], 'below');
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });

  it('should not move rows dragged from another grid', () => {
    drop(items[2], 'below');
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });

  it('should not move rows when a sorter is active', async () => {
    const sorter = document.createElement('vaadin-grid-sorter') as GridSorter;
    sorter.path = 'name';
    grid.appendChild(sorter);
    await nextFrame();
    grid._sorters = [sorter];
    sorter.direction = 'asc';

    drag([items[0]]);
    drop(items[2], 'below');
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });

  it('should not move rows when the affected range is not fully loaded', async () => {
    grid.pageSize = 2;
    setRootItems(grid.$connector, items);
    clear(grid.$connector, 2, 2);
    drag([items[0]]);
    drop(items[4], 'below');
    expect(grid._dataProviderController.rootCache.items.map((item) => item?.key)).to.eql([
      '0',
      '1',
      undefined,
      undefined,
      '4'
    ]);
  });

  it('should show the server order when the server rejects the drop', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    serverResponse(['0', '1', '2', '3', '4']);
    expect(getRenderedNames(5)).to.eql(['item 0', 'item 1', 'item 2', 'item 3', 'item 4']);
  });

  it('should keep the order when the server accepts the drop', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    serverResponse(['1', '2', '0', '3', '4']);
    expect(getRenderedNames(5)).to.eql(['item 1', 'item 2', 'item 0', 'item 3', 'item 4']);
  });

  it('should apply a newer row move again on top of an older server response', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    drag([items[4]]);
    drop(items[1], 'above');
    expect(getRenderedNames(5)).to.eql(['item 4', 'item 1', 'item 2', 'item 0', 'item 3']);

    // Response to the first drop: the server accepted it
    serverResponse(['1', '2', '0', '3', '4']);
    expect(getRenderedNames(5)).to.eql(['item 4', 'item 1', 'item 2', 'item 0', 'item 3']);

    // Response to the second drop: the server accepted it
    serverResponse(['4', '1', '2', '0', '3']);
    expect(getRenderedNames(5)).to.eql(['item 4', 'item 1', 'item 2', 'item 0', 'item 3']);
  });

  it('should apply a pending row move again on top of an unrelated data update', () => {
    drag([items[0]]);
    drop(items[2], 'below');

    // An update that the server sent before it received the drop
    grid.$connector.set(0, itemsInOrder(['0', '1', '2', '3', '4']));
    grid.$connector.confirm(1);
    expect(getRenderedNames(5)).to.eql(['item 1', 'item 2', 'item 0', 'item 3', 'item 4']);
  });

  it('should count drops that did not move rows', () => {
    drag([items[0]]);
    drop(items[2], 'on-top');
    drag([items[4]]);
    drop(items[0], 'above');

    // Response to the on-top drop
    serverResponse(['0', '1', '2', '3', '4']);
    expect(getRenderedNames(5)).to.eql(['item 4', 'item 0', 'item 1', 'item 2', 'item 3']);

    // Response to the second drop: the server rejected it
    serverResponse(['0', '1', '2', '3', '4']);
    expect(getRenderedNames(5)).to.eql(['item 0', 'item 1', 'item 2', 'item 3', 'item 4']);
  });

  it('should keep the optimistic order until the data of a handled drop arrives', () => {
    drag([items[0]]);
    drop(items[2], 'below');

    // E.g. with an asynchronous data communicator, the data arrives later
    grid.__dropsHandled = 1;
    expect(getRenderedNames(5)).to.eql(['item 1', 'item 2', 'item 0', 'item 3', 'item 4']);

    grid.$connector.set(0, itemsInOrder(['0', '1', '2', '3', '4']));
    grid.$connector.confirm(1);
    expect(getRenderedNames(5)).to.eql(['item 0', 'item 1', 'item 2', 'item 3', 'item 4']);
  });

  it('should continue counting drops from the server count after a reattach', () => {
    grid.__dropsHandled = 7;
    drag([items[0]]);
    drop(items[2], 'below');

    // An older response does not include the drop
    grid.$connector.set(0, itemsInOrder(['0', '1', '2', '3', '4']));
    grid.$connector.confirm(1);
    expect(getRenderedNames(3)).to.eql(['item 1', 'item 2', 'item 0']);

    // The response to the drop: the server rejected it
    serverResponse(['0', '1', '2', '3', '4']);
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });

  it('should add the drop number to the event detail before other listeners run', () => {
    let dropNumbers: number[] = [];
    grid.addEventListener('grid-drop', (e) => dropNumbers.push((e.detail as any).optimisticDropNumber));
    drag([items[0]]);
    drop(items[2], 'below');
    drag([items[1]]);
    drop(items[2], 'on-top');
    expect(dropNumbers).to.eql([1, 2]);
  });

  it('should not add a drop number when optimistic row reorder is off', () => {
    grid.__optimisticRowReorder = false;
    let detail: any;
    grid.addEventListener('grid-drop', (e) => (detail = e.detail));
    drag([items[0]]);
    drop(items[2], 'below');
    expect(detail.optimisticDropNumber).to.be.undefined;
  });

  it('should discard an earlier row move that the server never handled', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    drag([items[4]]);
    drop(items[0], 'above');

    // The first drop never reached the server, the server rejected the second
    grid.__dropsHandled = 2;
    grid.$connector.set(0, itemsInOrder(['0', '1', '2', '3', '4']));
    grid.$connector.confirm(1);
    expect(getRenderedNames(5)).to.eql(['item 0', 'item 1', 'item 2', 'item 3', 'item 4']);
  });

  it('should discard pending row moves when optimistic row reorder is turned off', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    grid.__optimisticRowReorder = false;
    grid.$connector.set(0, itemsInOrder(['0', '1', '2', '3', '4']));
    grid.$connector.confirm(1);
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });

  it('should not move rows again on a later drop without a new drag start', () => {
    drag([items[0]]);
    grid.dispatchEvent(
      new CustomEvent('grid-drop', { detail: { dropTargetItem: items[2], dropLocation: 'below', dragData: [] } })
    );
    // No dragend, e.g. the dragged row element was removed during the drag.
    // Then a file from the desktop is dropped.
    grid.dispatchEvent(
      new CustomEvent('grid-drop', { detail: { dropTargetItem: items[4], dropLocation: 'below', dragData: [] } })
    );
    expect(getRenderedNames(5)).to.eql(['item 1', 'item 2', 'item 0', 'item 3', 'item 4']);
  });

  it('should discard pending row moves on reset', () => {
    drag([items[0]]);
    drop(items[2], 'below');
    grid.$connector.reset();
    setRootItems(grid.$connector, items);
    expect(getRenderedNames(3)).to.eql(['item 0', 'item 1', 'item 2']);
  });
});

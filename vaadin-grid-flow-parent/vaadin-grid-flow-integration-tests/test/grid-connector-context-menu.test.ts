import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getBodyCellContent, initSelectionColumn } from './shared.js';
import type { FlowGrid, FlowGridSelectionColumn } from './shared.js';
import type { GridColumn } from '@vaadin/grid/vaadin-grid-column.js';

describe('grid connector - context menu', () => {
  let grid: FlowGrid;
  let column: GridColumn;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-flow-selection-column></vaadin-grid-flow-selection-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    const selectionColumn = grid.querySelector<FlowGridSelectionColumn>('vaadin-grid-flow-selection-column')!;
    initSelectionColumn(grid, selectionColumn);

    column = grid.querySelector('vaadin-grid-column')!;
    column.id = 'name-column';

    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  it('should update context menu target item on before-open event', () => {
    grid.dispatchEvent(
      new CustomEvent('vaadin-context-menu-before-open', {
        detail: { key: '0', columnId: 'name-column' }
      })
    );
    expect(grid.$server.updateContextMenuTargetItem.calledWith('0', 'name-column')).to.be.true;
  });

  it('should return item key and column id in before-open detail', () => {
    let detail: { key: string, columnId: string };
    // The detail is resolved while the source event is still being dispatched
    grid.addEventListener('contextmenu', (e) => {
      detail = grid.getContextMenuBeforeOpenDetail(new CustomEvent('contextmenu', { detail: { sourceEvent: e } }));
    });
    getBodyCellContent(grid, 0, 1)!.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, composed: true }));

    expect(detail!).to.deep.equal({ key: '0', columnId: 'name-column' });
  });

  it('should prevent context menu on selection column left click', () => {
    let prevented: boolean;
    grid.addEventListener('click', (e) => {
      prevented = grid.preventContextMenu(e);
    });

    getBodyCellContent(grid, 0, 0)!.click();
    expect(prevented!).to.be.true;
  });

  it('should not prevent context menu on regular column left click', () => {
    let prevented: boolean;
    grid.addEventListener('click', (e) => {
      prevented = grid.preventContextMenu(e);
    });

    getBodyCellContent(grid, 0, 1)!.click();
    expect(prevented!).to.be.false;
  });
});

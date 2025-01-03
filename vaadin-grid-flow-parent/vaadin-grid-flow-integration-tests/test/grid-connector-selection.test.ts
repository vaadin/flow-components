import { expect, fixtureSync, nextFrame } from '@open-wc/testing';
import { init, getBodyCellContent, setRootItems } from './shared.js';
import type { FlowGrid } from './shared.js';
import sinon from 'sinon';

describe('grid connector - selection', () => {
  let grid: FlowGrid;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  describe('single selection mode', () => {
    beforeEach(() => {
      grid.$connector.setSelectionMode('SINGLE');
    });

    it('should select item on click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('0');
    });

    it('should mark the item selected', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems[0].selected).to.be.true;
    });

    it('should deselect old selection on another item click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 1, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('1');
    });

    it('should mark the item deselected', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      const item = grid.selectedItems[0];
      getBodyCellContent(grid, 0, 0)!.click();
      expect(item.selected).not.to.be.true;
    });

    it('should deselect on selected item click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems).to.be.empty;
    });

    it('should not deselect on selected item click when deselect is disallowed', () => {
      grid.__deselectDisallowed = true;
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('0');
    });

    it('should not select item on click when grid is disabled', () => {
      grid.disabled = true;
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems).to.be.empty;
    });

    it('should select on server', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.select).to.be.calledWith('0');
    });

    it('should deselect on server', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.$server.deselect).to.be.calledWith('0');
    });

    it('should apply selection from data', async () => {
      setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true }]);
      expect(grid.selectedItems.length).to.equal(1);
      expect(grid.selectedItems[0].key).to.equal('0');
    });

    it('should apply deselection from data', async () => {
      getBodyCellContent(grid, 0, 0)!.click();
      setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
      expect(grid.selectedItems).to.be.empty;
    });

    it('should avoid another re-render on items update', async () => {
      const items = [{ key: '0', name: 'foo', selected: true }];
      setRootItems(grid.$connector, items);
      await nextFrame();

      const spy = sinon.spy(grid, '__updateVisibleRows');
      grid.$connector.updateFlatData(items);
      // This number may come down from further optimization
      expect(spy.callCount).to.equal(1);
    });

    describe('server to client', () => {
      it('should select an item', () => {
        grid.$connector.doSelection([{ key: '0' }], false);
        expect(grid.selectedItems).to.have.lengthOf(1);
        expect(grid.selectedItems[0].key).to.equal('0');
      });

      it('should update activeItem when selecting an item', () => {
        grid.$connector.doSelection([{ key: '0' }], false);
        expect(grid.activeItem).to.deep.equal({ key: '0', selected: true });
      });

      it('should deselect the item when selecting null', () => {
        grid.$connector.doSelection([{ key: '0' }], false);
        grid.$connector.doSelection([null], false);
        expect(grid.selectedItems).to.be.empty;
      });

      it('should reset activeItem when selecting null', () => {
        grid.$connector.doSelection([{ key: '0' }], false);
        grid.$connector.doSelection([null], false);
        expect(grid.activeItem).not.to.exist;
      });

      it('should not request server to select already selected items', () => {
        grid.$connector.doSelection([{ key: '0' }], false);
        expect(grid.$server.select).not.to.be.called;
      });

      it('should not request server to deselect already deselected items', () => {
        grid.$connector.doSelection([{ key: '0' }], false);
        grid.$connector.doDeselection([{ key: '0' }], false);
        expect(grid.$server.deselect).not.to.be.called;
      });
    });

    describe('conditional selection', () => {
      let items;

      beforeEach(async () => {
        items = Array.from({ length: 4 }, (_, i) => ({
          key: i.toString(),
          name: i.toString(),
          selectable: i >= 2
        }));
        setRootItems(grid.$connector, items);
        await nextFrame();
        grid.requestContentUpdate();
      });

      it('should prevent selection of non-selectable items on click', () => {
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems).to.be.empty;
        expect(grid.$server.select).to.not.be.called;
      });

      it('should allow selection of selectable items on click', async () => {
        getBodyCellContent(grid, 2, 0)!.click();
        expect(grid.selectedItems).to.deep.equal([items[2]]);
        expect(grid.$server.select).to.be.calledWith(items[2].key);
      });

      it('should prevent deselection of non-selectable items on click', () => {
        grid.$connector.doSelection([items[0]], false);
        getBodyCellContent(grid, 0, 0)!.click();
        expect(grid.selectedItems).to.deep.equal([items[0]]);
        expect(grid.$server.deselect).to.not.be.called;
      });

      it('should prevent deselection of non-selectable items when clicking another non-selectable item', () => {
        grid.$connector.doSelection([items[0]], false);
        getBodyCellContent(grid, 1, 0)!.click();
        expect(grid.selectedItems).to.deep.equal([items[0]]);
        expect(grid.$server.deselect).to.not.be.called;
      });

      it('should prevent deselection of non-selectable items on row click when active item data is stale', () => {
        // item is selectable initially and is selected
        grid.$connector.doSelection([items[2]], false);

        // update grid items to make the item non-selectable
        const updatedItems = items.map((item) => ({ ...item, selectable: false }));
        setRootItems(grid.$connector, updatedItems);

        // active item still references the original item with selectable: true
        expect(grid.activeItem.selectable).to.be.true;

        // however clicking the row should not deselect the item
        getBodyCellContent(grid, 2, 0)!.click();
        expect(grid.selectedItems).to.deep.equal([updatedItems[2]]);
        expect(grid.$server.deselect).to.not.be.called;
      });

      it('should allow deselection of selectable items on row click', () => {
        grid.$connector.doSelection([items[2]], false);
        getBodyCellContent(grid, 2, 0)!.click();
        expect(grid.selectedItems).to.be.empty;
        expect(grid.$server.deselect).to.be.calledWith(items[2].key);
      });

      it('should always allow selection from server', () => {
        // non-selectable item
        grid.$connector.doSelection([items[0]], false);
        expect(grid.selectedItems).to.deep.equal([items[0]]);
        expect(grid.activeItem).to.deep.equal(items[0]);

        // selectable item
        grid.$connector.doSelection([items[2]], false);
        expect(grid.selectedItems).to.deep.equal([items[2]]);
        expect(grid.activeItem).to.deep.equal(items[2]);
      })

      it('should always allow deselection from server', () => {
        // non-selectable item
        grid.$connector.doSelection([items[0]], false);
        grid.$connector.doDeselection([items[0]], false);
        expect(grid.selectedItems).to.deep.equal([]);

        // selectable item
        grid.$connector.doSelection([items[2]], false);
        grid.$connector.doDeselection([items[2]], false);
        expect(grid.selectedItems).to.deep.equal([]);
      })
    });
  });

  describe('none selection mode', () => {
    beforeEach(() => {
      grid.$connector.setSelectionMode('NONE');
    });

    it('should not select item on click', () => {
      getBodyCellContent(grid, 0, 0)!.click();
      expect(grid.selectedItems).to.be.empty;
    });

    it('should not apply selection from data', async () => {
      setRootItems(grid.$connector, [{ key: '0', name: 'foo', selected: true }]);
      expect(grid.selectedItems).to.be.empty;
    });
  });
});

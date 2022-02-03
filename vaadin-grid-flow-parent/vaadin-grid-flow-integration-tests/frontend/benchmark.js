import {
  registerStyles,
  css,
} from '@vaadin/vaadin-themable-mixin/register-styles.js';

var style = document.createElement('style');
style.type = 'text/css';
style.appendChild(
  document.createTextNode(`
  @keyframes content-ready {
    from {
      opacity: 1;
    }
    to {
      opacity: 1;
    }
  }

  [benchmark] vaadin-grid-tree-toggle[leaf] {
    transition: opacity 1s;
    opacity: 0.9;
  }
`)
);
document.head.appendChild(style);

registerStyles(
  'vaadin-grid',
  css`
    :host([benchmark]) [part~='body-cell'] {
      height: 50px;
    }

    @keyframes content-ready {
      to {
        opacity: 1;
      }
    }

    :host([benchmark]) [part~='cell'] {
      animation: content-ready 1s;
    }

    :host([benchmark]) {
      transition: opacity 1s;
    }

    :host([benchmark][loading]) {
      opacity: 0.9;
    }
  `
);

registerStyles(
  'vaadin-*',
  css`
    @keyframes content-ready {
      to {
        opacity: 1;
      }
    }

    :host([benchmark]) {
      animation: content-ready 1s;
    }
  `
);

/** @type {any} */
const _window = window;

_window.whenRendered = (grid) => {
  return new Promise((resolve) => {
    let readyTimer;
    const listener = (e) => {
      if (e.animationName === 'content-ready' || e.propertyName === 'opacity') {
        const endTime = performance.now();
        readyTimer && clearTimeout(readyTimer);
        if (!grid.loading) {
          readyTimer = setTimeout(() => {
            grid.$.scroller.removeEventListener('animationstart', listener);
            grid.removeEventListener('animationstart', listener);
            grid.removeEventListener('transitionstart', listener);
            resolve(endTime);
            // The timeout needs to be large enough so everything gets rendered
            // but small enough so the tests won't take forever. This resolves with
            // the timestamp of the listener's last invocation.
          }, 1000);
        }
      }
    };

    grid.$.scroller.addEventListener('animationstart', listener);
    grid.addEventListener('animationstart', listener);
    grid.addEventListener('transitionstart', listener);
  });
};

const reportResult = (result) => {
  _window.tachometerResult = result;

  const resultDiv = document.createElement('div');
  resultDiv.textContent = `Result: ${result}`;
  resultDiv.style.position = 'fixed';
  resultDiv.style.left = resultDiv.style.top = '0';
  document.body.appendChild(resultDiv);
};

let start = 0;

_window.startWhenReady = () => {
  return Promise.all([
    customElements.whenDefined('vaadin-grid'),
    customElements.whenDefined('vaadin-grid-column'),
  ]).then(() => (start = performance.now()));
};

_window.startWhenRendered = (grid) => {
  return _window.whenRendered(grid).then(() => {
    start = performance.now();
  });
};

_window.measureRender = (grid) => {
  _window.whenRendered(grid).then((endTime) => reportResult(endTime - start));
};

const SCROLL_TIME = 4000;

const scroll = (
  grid,
  frames,
  startTime,
  previousTime,
  deltaXMultiplier,
  deltaYMultiplier
) => {
  const now = performance.now();

  /** @type {any} */
  const e = new CustomEvent('wheel', { bubbles: true, cancelable: true });
  e.deltaX = (now - previousTime) * deltaXMultiplier;
  e.deltaY = (now - previousTime) * deltaYMultiplier;

  grid.dispatchEvent(e);

  // Switch horizontal scroll direction in case end was reached
  if (deltaXMultiplier) {
    if (deltaXMultiplier === 1) {
      const remaining = (grid.$.table.scrollWidth - grid.$.table.clientWidth) - grid.$.table.scrollLeft;
      if (remaining < 100) {
        deltaXMultiplier = -1;
      }
    } else if (deltaXMultiplier === -1) {
      if (grid.$.table.scrollLeft < 100) {
        deltaXMultiplier = 1;
      }
    }
  }

  if (now < startTime + SCROLL_TIME) {
    requestAnimationFrame(() =>
      scroll(
        grid,
        frames + 1,
        startTime,
        now,
        deltaXMultiplier,
        deltaYMultiplier
      )
    );
  } else {
    const frameTime = SCROLL_TIME / frames;
    reportResult(frameTime);
  }
};

_window.measureScrollFrameTime = (grid, horizontal) => {
  scroll(
    grid,
    0,
    performance.now(),
    performance.now(),
    horizontal ? 1 : 0,
    horizontal ? 0 : 1
  );
};

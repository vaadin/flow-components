import {
  registerStyles,
  css,
} from "@vaadin/vaadin-themable-mixin/register-styles.js";

// Add global styles
const globalStyles = css`
  @keyframes tree-toggle-leaf {
  }

  vaadin-grid-tree-toggle[leaf] {
    animation: tree-toggle-leaf;
  }

  /* Changes in attributes like loading trigger animations so need to use transitions instead */
  vaadin-grid,
  vaadin-checkbox {
    transition: opacity 0.5s;
  }

  vaadin-grid[loading] {
    opacity: 0.9;
  }

  vaadin-checkbox[checked] {
    opacity: 0.9;
  }

  vaadin-grid-sorter {
    transition: opacity 0.5s;
  }

  vaadin-grid-sorter[direction="asc"] {
    opacity: 0.9;
  }

  vaadin-grid-sorter[direction="desc"] {
    opacity: 0.8;
  }
`;

const style = document.createElement("style");
style.textContent = globalStyles.cssText;
document.head.appendChild(style);

// Add internal grid styles
registerStyles(
  "vaadin-grid",
  css`
    @keyframes cell-rendered {
    }

    [part~="cell"] {
      animation: cell-rendered;
    }

    [part~="row"] {
      transition: opacity 0.5s;
    }

    [part~="row"][selected] {
      opacity: 0.9;
    }

    [part~="cell"].test-classname {
      text-decoration: underline;
    }
  `
);

/**
 * This function returns a promise that resolves when the grid is finished rendering.
 *
 * The implementation is based on listening to animationstart and transitionstart events
 * that are triggered when selected attributes (for example "loading") change on the grid
 * or its parts or when new elements (like grid cells) get rendered to the DOM.
 */
window.whenRendered = (grid) => {
  return new Promise((resolve) => {
    let readyTimer;
    const listener = () => {
      const endTime = performance.now();
      readyTimer && clearTimeout(readyTimer);
      if (!grid.loading) {
        readyTimer = setTimeout(() => {
          const thisReadyTimer = readyTimer;
          requestIdleCallback(() => {
            if (thisReadyTimer === readyTimer) {
              grid.shadowRoot.removeEventListener("animationstart", listener);
              grid.shadowRoot.removeEventListener("transitionstart", listener);
              grid.removeEventListener("animationstart", listener);
              grid.removeEventListener("transitionstart", listener);
              resolve(endTime);
            }
          });
          // The timeout needs to be large enough so everything gets rendered
          // but small enough so the tests won't take forever. This resolves with
          // the timestamp of the listener's last invocation.
        }, 1000);
      }
    };

    grid.shadowRoot.addEventListener("animationstart", listener);
    grid.shadowRoot.addEventListener("transitionstart", listener);
    grid.addEventListener("animationstart", listener);
    grid.addEventListener("transitionstart", listener);
  });
};

/**
 * Report the result to the Tachometer framework and to the UI in a human-readable format.
 */
function reportResult(result) {
  window.tachometerResult = result;

  const resultDiv = document.createElement("div");
  resultDiv.textContent = `Result: ${result}`;
  // TODO: Add a container for the results
  document.querySelector("#outlet").appendChild(resultDiv);
}

let start = 0;

/**
 * Resolves and marks the start timestamp when the UI has been initialized
 * and the test is ready to start.
 */
window.startWhenReady = async () => {
  // Wait for the custom elements to be defined
  await Promise.all([
    customElements.whenDefined("vaadin-grid"),
    customElements.whenDefined("vaadin-grid-column"),
  ]);

  // Wait for an idle period
  if (window.requestIdleCallback) {
    // Safari doesn't support requestIdleCallback
    await new Promise((resolve) => requestIdleCallback(resolve));
  }

  // Half a second additional cooldown
  await new Promise((resolve) => setTimeout(resolve, 500));

  start = performance.now();
};

/**
 * Resolves and marks the start timestamp when the grid is fully rendered.
 */
window.startWhenRendered = async (grid) => {
  await window.whenRendered(grid);
  start = performance.now();
};

/**
 * Marks the end timestamp when the grid is fully rendered and reports the test result.
 */
window.measureRender = (grid) => {
  window.whenRendered(grid).then((endTime) => reportResult(endTime - start));
};

const SCROLL_TIME = 4000;

/**
 * Simulates scrolling the grid by dispatching wheel events.
 * The function measures frame time while scrolling and once the scrolling is done,
 * reports the result.
 */
function scroll(grid, frames, startTime, previousTime) {
  const now = performance.now();

  const e = new CustomEvent("wheel", { bubbles: true, cancelable: true });
  e.deltaY = (now - previousTime) * 4;

  grid.$.items.dispatchEvent(e);

  if (now < startTime + SCROLL_TIME) {
    requestAnimationFrame(() =>
      scroll(grid, [...frames, now - previousTime], startTime, now)
    );
  } else {
    const sortedFrames = frames.sort();
    // Eliminate the 30% fastest and 30% slowest frames
    const trimmedFrames = sortedFrames.slice(
      Math.floor(sortedFrames.length * 0.3),
      Math.ceil(sortedFrames.length * 0.7)
    );
    // Get the average of the remaining frames
    const averageFrameTime =
      trimmedFrames.reduce((a, b) => a + b, 0) / trimmedFrames.length;
    reportResult(averageFrameTime);
  }
}

/**
 * Measures the frame time while scrolling the grid and reports the result.
 */
window.measureScrollFrameTime = (grid) => {
  scroll(grid, [], performance.now(), performance.now());
};

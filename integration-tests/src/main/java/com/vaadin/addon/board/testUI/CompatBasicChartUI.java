package com.vaadin.addon.board.testUI;

import static com.vaadin.addon.board.testUI.UIFunctions.testLayout;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.vaadin.addon.charts.ChartOptions;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.Theme;
import com.vaadin.addon.charts.themes.ValoLightTheme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;

/**
 *
 */
public abstract class CompatBasicChartUI extends AbstractTestUI {

  protected abstract Component nextChartInstance();

  @Override
  protected void init(VaadinRequest vaadinRequest) {
    setContent(
        testLayout()
            .apply(Stream.of(
                nextChartInstance(),
                nextChartInstance(),
                nextChartInstance())));
  }

  protected Color[] getThemeColors() {
    Theme theme = ChartOptions.get().getTheme();
    return (theme != null) ? theme.getColors() : new ValoLightTheme()
        .getColors();
  }

  protected Theme getCurrentTheme() {
    Theme theme = ChartOptions.get().getTheme();
    return (theme != null) ? theme : new ValoLightTheme();
  }
}

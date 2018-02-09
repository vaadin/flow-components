package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * Set options on specific levels. Takes precedence over series options, but not
 * point options.
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Level extends AbstractConfigurationObject {

	private DataLabels dataLabels;
	private TreeMapLayoutAlgorithm layoutAlgorithm;
	private TreeMapLayoutStartingDirection layoutStartingDirection;
	private Number level;

	public Level() {
	}

	/**
	 * @see #setDataLabels(DataLabels)
	 */
	public DataLabels getDataLabels() {
		if (dataLabels == null) {
			dataLabels = new DataLabels();
		}
		return dataLabels;
	}

	/**
	 * Can set the options of dataLabels on each point which lies on the level.
	 * <a
	 * href="#plotOptions.treemap.dataLabels">plotOptions.treemap.dataLabels</a>
	 * for possible values.
	 * <p>
	 * Defaults to: undefined
	 */
	public void setDataLabels(DataLabels dataLabels) {
		this.dataLabels = dataLabels;
	}

	/**
	 * @see #setLayoutAlgorithm(TreeMapLayoutAlgorithm)
	 */
	public TreeMapLayoutAlgorithm getLayoutAlgorithm() {
		return layoutAlgorithm;
	}

	/**
	 * Can set the layoutAlgorithm option on a specific level.
	 */
	public void setLayoutAlgorithm(TreeMapLayoutAlgorithm layoutAlgorithm) {
		this.layoutAlgorithm = layoutAlgorithm;
	}

	/**
	 * @see #setLayoutStartingDirection(TreeMapLayoutStartingDirection)
	 */
	public TreeMapLayoutStartingDirection getLayoutStartingDirection() {
		return layoutStartingDirection;
	}

	/**
	 * Can set the layoutStartingDirection option on a specific level.
	 */
	public void setLayoutStartingDirection(
			TreeMapLayoutStartingDirection layoutStartingDirection) {
		this.layoutStartingDirection = layoutStartingDirection;
	}

	/**
	 * @see #setLevel(Number)
	 */
	public Number getLevel() {
		return level;
	}

	/**
	 * Decides which level takes effect from the options set in the levels
	 * object.
	 */
	public void setLevel(Number level) {
		this.level = level;
	}
}
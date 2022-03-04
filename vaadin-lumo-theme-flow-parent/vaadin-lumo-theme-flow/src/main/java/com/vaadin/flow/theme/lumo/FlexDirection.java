package com.vaadin.flow.theme.lumo;

public class FlexDirection {

	public static String COLUMN = "flex-col";
	public static String COLUMN_REVERSE = "flex-col-reverse";
	public static String ROW = "flex-row";
	public static String ROW_REVERSE = "flex-row-reverse";

	public static class Breakpoint {

		public static class Small {
			public static String COLUMN = "sm:flex-col";
			public static String ROW = "sm:flex-row";
		}

		public static class Medium {
			public static String COLUMN = "md:flex-col";
			public static String ROW = "md:flex-row";
		}

		public static class Large {
			public static String COLUMN = "lg:flex-col";
			public static String ROW = "lg:flex-row";
		}

		public static class XLarge {
			public static String COLUMN = "xl:flex-col";
			public static String ROW = "xl:flex-row";
		}

		public static class XXLarge {
			public static String COLUMN = "2xl:flex-col";
			public static String ROW = "2xl:flex-row";
		}

	}

}

package com.vaadin.flow.theme.lumo;

public class Display {

	public static String BLOCK = "block";
	public static String FLEX = "flex";
	public static String GRID = "grid";
	public static String HIDDEN = "hidden";
	public static String INLINE = "inline";
	public static String INLINE_BLOCK = "inline-block";
	public static String INLINE_FLEX = "inline-flex";
	public static String INLINE_GRID = "inline-grid";

	public static class Breakpoint {

		public static class Small {
			public static String FLEX = "sm:flex";
			public static String HIDDEN = "sm:hidden";
		}

		public static class Medium {
			public static String FLEX = "md:flex";
			public static String HIDDEN = "md:hidden";
		}

		public static class Large {
			public static String FLEX = "lg:flex";
			public static String HIDDEN = "lg:hidden";
		}

		public static class XLarge {
			public static String FLEX = "xl:flex";
			public static String HIDDEN = "xl:hidden";
		}

		public static class XXLarge {
			public static String FLEX = "2xl:flex";
			public static String HIDDEN = "2xl:hidden";
		}
	}
}

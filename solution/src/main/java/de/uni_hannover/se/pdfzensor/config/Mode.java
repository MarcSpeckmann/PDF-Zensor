package de.uni_hannover.se.pdfzensor.config;

import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A mode specifying how the PDF-file should be censored. {@link #MARKED} to censor only segments marked beforehand with
 * a different software, {@link #UNMARKED} to censor everything but segments marked beforehand with a different software
 * or {@link #ALL} to censor everything regardless of whether or not it has been marked before.
 */
public enum Mode {
	ALL, MARKED, UNMARKED;
	
	/**
	 * Converts a String into a Mode if it was valid.
	 *
	 * @param sMode The mode as a String.
	 * @return A mode with the same name as the given String or null if the String was invalid.
	 */
	@Nullable
	@Contract(pure = true)
	static Mode stringToMode(@Nullable final String sMode) {
		return EnumUtils.getEnumIgnoreCase(Mode.class, sMode);
	}
}
package de.uni_hannover.se.pdfzensor.text;

/**
 * TokenDef represents an abstraction of any token. A token by itself is only identified by the (regex-) pattern that is
 * matched by occurrences in a text.
 */
public interface TokenDef {
	/**
	 * Retrieves a regex pattern that should be used to find occurrences of this token. To guarantee integrity the
	 * pattern should not be constant for each instance of TokenDef. So if it gets called multiple times on the same
	 * instance the returned value should always be the same.
	 *
	 * @return the regex pattern that each occurrence of this token should match.
	 */
	String getRegex();
}
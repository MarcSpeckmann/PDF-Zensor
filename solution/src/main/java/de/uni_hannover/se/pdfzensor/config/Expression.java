package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.utils.Utils.*;

/**
 * The Expression class represents a regex-color-tuple that may be defined in the configuration file or with the command
 * line arguments. When a text that matches the stored regex is censored within a pdf-file it should be censored in the
 * provided color.
 */
final class Expression {
	@NotNull
	private final String regex;
	@Nullable
	private Color color;
	
	/**
	 * Initializes a new expression object with the provided regular expression and color (in hexadecimal notation). The
	 * provided color may be null. The color can than be set at a later time. The regex on the other hand may not be
	 * null and can not be changed later on.
	 *
	 * @param regex    The regex that should be matched to use this expression's color for censoring.
	 * @param hexColor The color that should be used to censor a text that matches this expression.
	 * @throws NullPointerException     if regex is null
	 * @throws IllegalArgumentException if the hexColor is not null and incorrectly formatted
	 * @see de.uni_hannover.se.pdfzensor.utils.Utils#colorToString(Color)
	 * @see de.uni_hannover.se.pdfzensor.utils.Utils#getColorOrNull(String)
	 */
	@JsonCreator()
	Expression(@NotNull @JsonProperty("regex") final String regex,
			   @Nullable @JsonProperty("color") final String hexColor) {
		this.regex = Objects.requireNonNull(regex);
		this.color = getColorOrNull(hexColor);
	}
	
	/**
	 * Returns the regex that is used to match this expression in text.
	 *
	 * @return The regular expression associated with this object.
	 */
	@Contract(pure = true)
	@NotNull
	public String getRegex() {
		return regex;
	}
	
	/**
	 * Returns the color that should be used to censor a text-passage that matches this expression's regex.
	 *
	 * @return The color associated with this object. May be null if none was set.
	 * @see #getRegex()
	 */
	@Contract(pure = true)
	@Nullable
	public Color getColor() {
		return color;
	}
	
	/**
	 * Returns a string representation that represents this object.
	 *
	 * @return A string representation of this object's regular expression and its associated color.
	 */
	@NotNull
	@Override
	public String toString() {
		return String.format("[regex: \"%s\"; color: %s]", regex, colorToString(color));
	}
}
package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

import static de.uni_hannover.se.pdfzensor.utils.Utils.getColorOrNull;

/**
 * A class containing regular expressions and information on the color with which to censor text which matches the
 * regular expression.
 */
final class Expression {
	
	@NotNull
	private final String regex;
	@Nullable
	private Color color;
	
	/**
	 * An Expression object may be initialized without a color, in which case one would be assigned later.
	 *
	 * @param regex    This Expression's regex pattern.
	 * @param hexColor This Expression's color.
	 */
	@JsonCreator()
	Expression(@NotNull @JsonProperty("regex") final String regex,
			   @Nullable @JsonProperty("color") final String hexColor) {
		this.regex = Objects.requireNonNull(regex);
		this.color = getColorOrNull(hexColor);
	}
	
	/**
	 * @return The regular expression associated with this object.
	 */
	@Contract(pure = true)
	@NotNull
	public String getRegex() {
		return regex;
	}
	
	/**
	 * @return The color associated with this object.
	 */
	@Contract(pure = true)
	@Nullable
	public Color getColor() {
		return color;
	}
	
	/**
	 * @return A string representation of this object's regular expression and its associated color.
	 */
	@NotNull
	@Override
	public String toString() {
		return String.format("\"%s\" - %s", regex, Settings.colorToString(color));
	}
}
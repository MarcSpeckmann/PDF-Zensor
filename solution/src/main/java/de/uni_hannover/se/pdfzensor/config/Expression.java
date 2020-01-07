package de.uni_hannover.se.pdfzensor.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uni_hannover.se.pdfzensor.text.TokenDef;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;

import static de.uni_hannover.se.pdfzensor.utils.Utils.*;

/**
 * The Expression class represents a regex-color-tuple that may be defined in the configuration file or with the command
 * line arguments. When a text that matches the stored regex is censored within a pdf-file it should be censored in the
 * provided color.
 */
public final class Expression implements TokenDef {
	@NotNull
	private final String regex;
	@Nullable
	private Color color;
	
	/**
	 * Initializes a new expression object with the provided regular expression and color (in hexadecimal notation). The
	 * provided color may be null. The color can then be set at a later time. The regex on the other hand may not be
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
		this(regex, getColorOrNull(hexColor));
	}
	
	/**
	 * Initializes a new expression object with the provided regular expression and color. The provided color may be
	 * null. The color can then be set at a later time. The regex on the other hand may not be null and can not be
	 * changed later on.
	 *
	 * @param regex The regex that should be matched to use this expression's color for censoring.
	 * @param color The color that should be used to censor a text that matches this expression.
	 * @throws NullPointerException if regex is null
	 */
	Expression(@NotNull final String regex,
			   @Nullable final Color color) {
		this.regex = Objects.requireNonNull(regex);
		this.color = color;
	}
	
	/**
	 * Returns the regex that is used to match this expression in text.
	 *
	 * @return The regular expression associated with this object.
	 */
	@Contract(pure = true)
	@NotNull
	@Override
	public String getRegex() {
		return regex;
	}
	
	/**
	 * Returns the color that should be used to censor a text-passage that matches this expression's regex. If none has
	 * been set the <code>Settings.DEFAULT_CENSOR_COLOR</code> will be returned.
	 * <br>
	 * In theory, this allows two texts which match this object's regex to be colored differently (when this object is
	 * used for censoring before setting the color, then setting the color and using it to censor text again).
	 *
	 * @return The color associated with this object.
	 * @see #getRegex()
	 */
	@Contract(pure = true)
	@NotNull
	public Color getColor() {
		return Optional.ofNullable(color).orElse(Settings.DEFAULT_CENSOR_COLOR);
	}
	
	/**
	 * Sets the color of this expression object iff it was previously null. Does nothing if the given color is null.
	 *
	 * @param color The new color of this expression object.
	 * @return True if a new color was set, false otherwise.
	 */
	public boolean setColor(@Nullable Color color) {
		var ret = this.color == null && color != null;
		if (this.color == null)
			this.color = color;
		return ret;
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
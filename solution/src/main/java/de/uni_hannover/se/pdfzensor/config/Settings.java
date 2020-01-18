package de.uni_hannover.se.pdfzensor.config;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.utils.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static de.uni_hannover.se.pdfzensor.utils.Utils.colorToString;
import static java.util.Arrays.stream;

/**
 * The Settings class constitutes an abstraction and unification of the configuration file ({@link Config}) and the
 * command line arguments ({@link CLArgs}). Instead of accessing each configuration entity separately, they should be
 * unified via settings and passed to the outside from there. Upon construction settings passes the command line
 * arguments to {@link CLArgs}, loads the corresponding configuration file via {@link Config} and takes their parameters
 * according to the following rules:<br>
 * <ol>
 *     <li><b>CLArgs</b> overrides <b>Config</b> overrides <b>Default values</b><br></li>
 *     <li>No value will be set to null (for this purpose the default values exist)<br></li>
 *     <li><b>input</b> may only be specified in the CLArgs</li>
 * </ol>
 */
public final class Settings {
	/** The color that text should be censored in if it does not match any other specified expression. */
	static final Color DEFAULT_CENSOR_COLOR = Color.BLACK;
	/** The color links should be censored in if nothing else was specified. */
	private static final Color DEFAULT_LINK_COLOR = Color.BLUE;
	
	/**
	 * Kenneth Kelly's 22 colors of maximum contrast except the first two (#F2F3F4, #222222), since high contrast to
	 * black and white should be present (for {@link #DEFAULT_CENSOR_COLOR} and white backgrounds).
	 */
	@NotNull
	static final Color[] DEFAULT_COLORS;
	
	static {
		final var defColorCodes = "#F3C300,#875692,#F38400,#A1CAF1,#BE0032,#C2B280,#848482,#008856,#E68FAC,#0067A5,#F99379,#604E97,#F6A600,#B3446C,#DCD300,#882D17,#8DB600,#654522,#E25822,#2B3D26";
		DEFAULT_COLORS = stream(defColorCodes.split(",")).map(Utils::getColorOrNull).filter(Objects::nonNull)
														 .toArray(Color[]::new);
	}
	
	/** The path at which the pdf-file that should be censored is located. */
	@NotNull
	private final File input;
	/** The path into which the censored pdf-file should be written. */
	@NotNull
	private final File output;
	/** The color with which to censor links if {@link #distinguishLinks} is true. */
	@NotNull
	private final Color linkColor;
	/**
	 * Whether links should be distinguishable from normal text by their censor color ({@link #DEFAULT_CENSOR_COLOR}) or
	 * be considered normal text.
	 */
	private final boolean distinguishLinks;
	/** The mode to use for censoring. See {@link Mode} for more information. */
	@NotNull
	private final Mode mode;
	/**
	 * A set of regex-color-tuples to identify with what color to censor which text. Should at least contain the tuple
	 * (".", {@link #DEFAULT_CENSOR_COLOR}).
	 */
	@NotNull
	private final Expression[] expressions;
	/**
	 * True if text censor bars may be drawn atop of censored images, false otherwise (text will be removed but no
	 * censor bar is drawn).
	 */
	private final boolean intersectImages;
	
	/**
	 * Constructs the settings object from the configuration file and the commandline arguments.
	 *
	 * @param args The commandline arguments.
	 */
	public Settings(@NotNull final String... args) {
		final var clArgs = CLArgs.fromStringArray(args);
		final var configFile = ObjectUtils.firstNonNull(clArgs.getConfigFile(), Config.getDefaultConfigFile(false));
		final var config = Config.fromFile(configFile);
		final var verbose = ObjectUtils.firstNonNull(clArgs.getVerbosity(), config.getVerbosity(), Level.WARN);
		Logging.init(clArgs.getQuiet() ? Level.OFF : verbose);
		
		input = clArgs.getInput();
		output = checkOutput(ObjectUtils.firstNonNull(clArgs.getOutput(), config.getOutput(),
													  input.getAbsoluteFile().getParentFile()));
		linkColor = DEFAULT_LINK_COLOR;
		distinguishLinks = clArgs.distinguishLinks() || config.distinguishLinks();
		mode = ObjectUtils.firstNonNull(clArgs.getMode(), config.getMode(), Mode.ALL);
		final var defColors = ObjectUtils.firstNonNull(config.getDefaultColors(), DEFAULT_COLORS);
		expressions = combineExpressions(clArgs.getExpressions(), config.getExpressions(), defColors);
		intersectImages = clArgs.getIntersectImages() || config.getIntersectImages();
		
		//Dump to log
		final var logger = Logging.getLogger();
		if (configFile == null)
			logger.error("The default configuration file could not be created.");
		logger.debug("Finished parsing the settings:");
		logger.debug("\tInput-file: {}", input);
		logger.debug("\tConfig-file: {}",
					 () -> Optional.ofNullable(configFile).map(File::getAbsolutePath).orElse("none"));
		logger.debug("\tOutput-file: {}", output);
		logger.debug("\tLogger verbosity: {}", verbose);
		logger.debug("\tQuiet: {}", clArgs::getQuiet);
		logger.debug("\tIntersect Images: {}", intersectImages);
		logger.debug("\tCensor mode: {}", mode);
		logger.debug("\tDistinguish Links by color: {}", distinguishLinks);
		logger.debug("\tLink-Color: {}", () -> colorToString(linkColor));
		logger.debug("\tExpressions");
		for (var exp : expressions)
			logger.debug("\t\t{}", exp);
		logger.debug("\tDefault Colors");
		for (var col : defColors)
			logger.debug("\t\t{}", () -> colorToString(col));
	}
	
	/**
	 * @return The input file as it was specified in the command-line arguments.
	 */
	@NotNull
	@Contract(pure = true)
	public File getInput() {
		return input;
	}
	
	/**
	 * @return The output file as it was specified in the command-line arguments and config.
	 */
	@NotNull
	@Contract(pure = true)
	public File getOutput() {
		return output;
	}
	
	/**
	 * @return The color links should be censored in if {@link #distinguishLinks} is true.
	 */
	@NotNull
	@Contract(pure = true)
	public Color getLinkColor() {
		return linkColor;
	}
	
	/**
	 * @return True if links should be distinguishable from normal text, false otherwise. As specified in either the
	 * command-line arguments or config.
	 */
	@Contract(pure = true)
	public boolean distinguishLinks() {
		return distinguishLinks;
	}
	
	/**
	 * @return The censor mode which should be used when censoring PDF-files.
	 */
	@NotNull
	@Contract(pure = true)
	public Mode getMode() {
		return mode;
	}
	
	/**
	 * @return The expressions as they were specified in the command-line arguments and config.
	 */
	@NotNull
	@Contract(pure = true)
	public Expression[] getExpressions() {
		return ObjectUtils.cloneIfPossible(expressions);
	}
	
	/**
	 * @return True if text censor bars may overlap with censored images, false otherwise.
	 */
	@Contract(pure = true)
	public boolean getIntersectImages() {
		return intersectImages;
	}
	
	/**
	 * Validates the provided output file. If it is a file it itself will be returned. If it is a folder (or does not
	 * exist and has no suffix) a path to <code>{out}/{input name}_cens.pdf</code> is returned.
	 *
	 * @param out The output file that should be validated. May not be null.
	 * @return the validated output file the censored PDF should be written into.
	 * @throws NullPointerException if out is null
	 * @see #getDefaultOutput(String)
	 */
	@NotNull
	private File checkOutput(@NotNull final File out) {
		var result = Objects.requireNonNull(out);
		if (!out.isFile() && StringUtils.isEmpty(FileUtils.getFileExtension(out)))
			result = getDefaultOutput(out.getPath());
		return result;
	}
	
	/**
	 * Will return the absolute default filename in directory <code>path</code>. The default filename is
	 * <code>in_cens.pdf</code>, where <code>in</code> is the name of the input file.
	 *
	 * @param path The path in which the output file with default naming should be located.
	 * @return The absolute default output file.
	 */
	@NotNull
	private File getDefaultOutput(@NotNull final String path) {
		final var inName = FilenameUtils.removeExtension(input.getName());
		return new File(Objects.requireNonNull(path) + File.separatorChar + inName + "_cens.pdf").getAbsoluteFile();
	}
	
	/**
	 * Merges two {@link Expression} arrays together while keeping them in the given order (<code>expressions1</code> is
	 * in front). Applies a color from the default color array (if it has unused colors remaining) to expressions which
	 * do not yet have a color assigned. Finally appends the fallback regex "." with the {@link #DEFAULT_CENSOR_COLOR}.
	 *
	 * @param expressions1  The array of Expressions.
	 * @param expressions2  The array of Expressions that is appended to <code>expressions1</code>.
	 * @param defaultColors The array of default colors to use if an Expression does not yet have a color assigned.
	 * @return The Expression array with the fallback Expression added.
	 */
	@NotNull
	private Expression[] combineExpressions(@Nullable final Expression[] expressions1,
											@Nullable final Expression[] expressions2,
											@NotNull final Color[] defaultColors) {
		final var ret = ArrayUtils.addAll(expressions1, expressions2);
		if (ret != null) {
			var cIndex = 0;
			for (var i = 0; i < ret.length && cIndex < defaultColors.length; i++)
				cIndex += ret[i].setColor(defaultColors[cIndex]) ? 1 : 0;
		}
		return ArrayUtils.addAll(ret, new Expression(".", DEFAULT_CENSOR_COLOR));
	}
}
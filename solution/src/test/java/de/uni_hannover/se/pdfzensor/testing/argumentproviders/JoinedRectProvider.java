package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * The JoinedRectProvider implements an ArgumentsProvider to provide arguments of the type {@link Rectangle2D}, {@link
 * Rectangle2D}, {@link Rectangle2D}. The first and second arguments are arbitrary rectangles and the 3rd rectangle
 * spans between the first two.
 */
public class JoinedRectProvider implements ArgumentsProvider {
	
	/**
	 * Creates a new rectangle from the specified coordinates. This method is mainly intended as a shortcut for {@link
	 * Rectangle2D.Double}.
	 *
	 * @param x1 the first x coordinate.
	 * @param y1 the first y coordinate.
	 * @param x2 the second x coordinate.
	 * @param y2 the second y coordinate.
	 * @return The {@link Rectangle2D.Double} created from the given coordinates.
	 */
	@NotNull
	@Contract("_, _, _, _ -> new")
	private static Rectangle2D rect(double x1, double y1, double x2, double y2) {
		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
	}
	
	/** {@inheritDoc} */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var list = new ArrayList<Arguments>();
		//Horizontally aligned
		list.add(Arguments.of(rect(0, 0, 1, 1), rect(2, 0, 3, 1), rect(1, 0, 2, 1)));
		list.add(Arguments.of(rect(2, 0, 3, 1), rect(0, 0, 1, 1), rect(1, 0, 2, 1)));
		list.add(Arguments.of(rect(1, 2, 3, 4), rect(10, 0, 12, 13), rect(3, 0, 10, 13)));
		list.add(Arguments.of(rect(10, 0, 12, 13), rect(1, 2, 3, 4), rect(3, 0, 10, 13)));
		
		//Vertically aligned
		list.add(Arguments.of(rect(0, 0, 1, 1), rect(0, 2, 1, 3), rect(0, 1, 1, 2)));
		list.add(Arguments.of(rect(0, 2, 1, 3), rect(0, 0, 1, 1), rect(0, 1, 1, 2)));
		list.add(Arguments.of(rect(2, 1, 4, 3), rect(0, 10, 13, 12), rect(0, 3, 13, 10)));
		list.add(Arguments.of(rect(0, 10, 13, 12), rect(2, 1, 4, 3), rect(0, 3, 13, 10)));
		
		//Intersecting
		list.add(Arguments.of(rect(0, 0, 3, 1), rect(2, 0, 3, 1), rect(0, 0, 0, 0)));
		list.add(Arguments.of(rect(2, 0, 3, 1), rect(0, 0, 3, 1), rect(0, 0, 0, 0)));
		
		return list.stream();
	}
	
}

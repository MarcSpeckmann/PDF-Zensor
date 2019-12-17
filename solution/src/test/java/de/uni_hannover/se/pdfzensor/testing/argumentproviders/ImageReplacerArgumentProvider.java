package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class ImageReplacerArgumentProvider implements ArgumentsProvider {
	static String PATH = "src/test/resources/pdf-files/";
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		List<Rectangle2D> rectList1 =  new ArrayList<Rectangle2D>();
		rectList1.add(new Rectangle(10, 50, 200, 200));
		rectList1.add(new Rectangle(250, 250, 200, 200));
		rectList1.add(new Rectangle(10, 400, 200, 200));
		String path1 = PATH + "threeImages.pdf";
		return Stream.of(
				Arguments.of(rectList1, path1)
		);
	}
}

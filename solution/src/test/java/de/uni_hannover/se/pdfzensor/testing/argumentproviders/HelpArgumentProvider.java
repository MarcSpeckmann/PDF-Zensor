package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HelpArgumentProvider implements ArgumentsProvider {
	
	@NotNull
	private static Arguments createArgumentCLHelp(boolean help, boolean version) {
		var arguments = new ArrayList<String>();
		if (help)
			arguments.add("-h");
		if (version)
			arguments.add("-V");
		return Arguments.of(arguments.toArray(new String[0]), help, version);
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		final var booleans = List.of(true, false);
		return TestUtility.crossJoin(booleans.stream(), booleans, HelpArgumentProvider::createArgumentCLHelp);
	}
}

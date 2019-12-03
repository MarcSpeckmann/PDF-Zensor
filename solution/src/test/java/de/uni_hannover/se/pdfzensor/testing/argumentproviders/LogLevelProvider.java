package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * This class implements JUnit's {@link ArgumentsProvider} to enable its usage in {@link
 * org.junit.jupiter.params.provider.ArgumentsSource}. It provides each value of {@link Level#values()} as an argument.
 */
public final class LogLevelProvider implements ArgumentsProvider {
	/** Returns a Stream of all log-levels as arguments. */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		return Stream.of(Level.values()).map(Arguments::of);
	}
}

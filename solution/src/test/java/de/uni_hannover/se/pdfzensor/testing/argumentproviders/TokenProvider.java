package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.stream.Stream;

public class TokenProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var args = new ArrayList<Arguments>();
		//Single pass (no flushing)
		args.add(Arguments.of(new String[][]{{}}, new String[]{}));
		args.add(Arguments.of(new String[][]{{"helloworld"}}, new String[]{"hello", "world"}));
		args.add(Arguments.of(new String[][]{{"h", "e", "l", "l", "o", "w", "o", "r", "l", "d"}},
							  new String[]{"hello", "world"}));
		args.add(Arguments.of(new String[][]{{"w", "o", "r", "l", "d"}}, new String[]{"world"}));
		args.add(Arguments.of(new String[][]{{"w", "o", "r", "l", "d", "s"}}, new String[]{"worlds"}));
		args.add(Arguments.of(new String[][]{{"he", "lloworld", "swo", "rld"}},
							  new String[]{"hello", "worlds", "world"}));
		args.add(Arguments.of(new String[][]{{"worldsay"}}, new String[]{"world", "say"}));
		//Two passes (flushing twice)
		args.add(Arguments.of(new String[][]{{"hello"}, {"world"}}, new String[]{"hello", "world"}));
		args.add(Arguments.of(new String[][]{{"hello"}, {"worlds"}}, new String[]{"hello", "worlds"}));
		args.add(Arguments.of(new String[][]{{"hello"}, {}, {}, {"worlds"}, {}}, new String[]{"hello", "worlds"}));
		return args.stream();
	}
}
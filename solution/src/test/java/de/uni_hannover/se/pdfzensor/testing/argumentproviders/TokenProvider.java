package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * The TokenProvider is an {@link ArgumentsProvider} that should be used by JUnit tests to provide Arguments of type
 * String[][], String[], where the String[][] is a series of inputs and String[] should be the tokenized output. The
 * tokenizer should be flushed between each String[] in the String[][].
 */
public class TokenProvider implements ArgumentsProvider {
	/** {@inheritDoc} */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		var args = new ArrayList<Arguments>();
		//Single pass (no flushing)
		args.add(Arguments.of(new String[][]{{}}, new String[]{}));
		args.add(Arguments.of(new String[][]{{"world"}}, new String[]{"world"}));
		args.add(Arguments.of(new String[][]{{"worlds"}}, new String[]{"worlds"}));
		args.add(Arguments.of(new String[][]{{"helloworld"}}, new String[]{"hello", "world"}));
		args.add(Arguments.of(new String[][]{{"h", "e", "l", "l", "o", "w", "o", "r", "l", "d"}},
							  new String[]{"hello", "world"}));
		args.add(Arguments.of(new String[][]{{"w", "o", "r", "l", "d"}}, new String[]{"world"}));
		args.add(Arguments.of(new String[][]{{"w", "o", "r", "l", "d", "s"}}, new String[]{"worlds"}));
		args.add(Arguments.of(new String[][]{{"he", "lloworld", "swo", "rld"}},
							  new String[]{"hello", "worlds", "world"}));
		//This may not be tokenized as world-say but as worlds-a-y because worlds should be matched first.
		args.add(Arguments.of(new String[][]{{"worldsay"}}, new String[]{"worlds", "a", "y"}));
		//Two passes (flushing twice)
		args.add(Arguments.of(new String[][]{{"hello"}, {"world"}}, new String[]{"hello", "world"}));
		args.add(Arguments.of(new String[][]{{"hello"}, {"worlds"}}, new String[]{"hello", "worlds"}));
		args.add(Arguments.of(new String[][]{{"hello"}, {}, {}, {"worlds"}, {}}, new String[]{"hello", "worlds"}));
		
		//Unmatched chars
		args.add(Arguments.of(new String[][]{{"he"}, {"llo"}}, new String[]{"h", "e", "l", "l", "o"}));
		args.add(Arguments.of(new String[][]{{"hello_world"}}, new String[]{"hello", "_", "world"}));
		args.add(Arguments.of(new String[][]{{"world says"}}, new String[]{"world", " ", "says"}));
		args.add(Arguments.of(new String[][]{{"wwwoworld"}}, new String[]{"w", "w", "w", "o", "world"}));
		return args.stream();
	}
}
package de.uni_hannover.se.pdfzensor.text;

import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.TokenProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import static java.text.Normalizer.Form.NFD;
import static org.junit.jupiter.api.Assertions.*;

/** Tokenizer Test contains all tests related solely to the {@link Tokenizer}. */
class TokenizerTest {
	
	/**
	 * Ensures that the default-handler does not induce any exceptions.
	 */
	@Test
	void testDefaultHandler() {
		try (var tokenizer = new Tokenizer<>(new SimpleToken("test"))) {
			tokenizer.input("hello", Collections.nCopies(5, new Object()));
			assertTrue(tokenizer.tryFlush());
			
			assertDoesNotThrow(() -> tokenizer.setHandler(null));
			tokenizer.input("hello", Collections.nCopies(5, new Object()));
		} catch (Exception e) {
			fail(e);
		}
	}
	
	/**
	 * Checks if the provided argument is correctly identified as erroneous.
	 *
	 * @param token the token to assert invalidity for.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"", ".*", "(ab)", "ab|.*", "(?:a*)|b", "()", "(?)", "(?:)", "(", "\\", "[^3", "\\()"})
	void testInvalidToken(String token) {
		var method = TestUtility.getPrivateMethod(Tokenizer.class, "isTokenValid", String.class);
		try {
			assertEquals(Boolean.FALSE, method.invoke(null, token));
		} catch (IllegalAccessException | InvocationTargetException e) {
			fail(e);
		}
	}
	
	/**
	 * Checks if the provided argument is correctly identified a correct token.
	 *
	 * @param token the token to assert validity for.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"a", ".+", "(?:xy)", "\\(\\)", "(?:)d+"})
	void testValidToken(String token) {
		var method = TestUtility.getPrivateMethod(Tokenizer.class, "isTokenValid", String.class);
		try {
			assertEquals(Boolean.TRUE, method.invoke(null, token));
		} catch (IllegalAccessException | InvocationTargetException e) {
			fail(e);
		}
	}
	
	/**
	 * Checks if the tokenizer can correctly tokenize with ligatures in the token or in the input.
	 */
	@Test
	void testLigatureTokenization() {
		try (var tokenizer = new Tokenizer<>(new SimpleToken("a\u030A"))) {
			var queue = "aaaa\u030Aaaaaaaa\u030Aaaa".chars().boxed().collect(Collectors.toCollection(ArrayDeque::new));
			final boolean[] valid = {true};
			tokenizer.setHandler((value, payload, token) -> {
				value = Normalizer.normalize(value, NFD);
				
				var provided = value.chars();
				provided.forEach(actual -> {
					var expected = queue.pop();
					if (!Objects.equals(expected, actual)) {
						System.out.printf("Expected: <%s> but was: <%s>%n", expected, actual);
						valid[0] = false;
					}
				});
			});
			
			tokenizer.input("aaa\u00E5aaa", Collections.nCopies(7, new Object()));
			tokenizer.input("aaaa\u030Aaaa", Collections.nCopies(8, new Object()));
			assertTrue(valid[0]);
		} catch (IOException e) {
			fail(e);
		}
		try (var tokenizer = new Tokenizer<>(new SimpleToken("\u00E5"))) {
			var queue = "aaaa\u030Aaaaaaaa\u030Aaaa".chars().boxed().collect(Collectors.toCollection(ArrayDeque::new));
			final boolean[] valid = {true};
			tokenizer.setHandler((value, payload, token) -> {
				value = Normalizer.normalize(value, NFD);
				
				var provided = value.chars().boxed();
				provided.forEach(actual -> {
					var expected = queue.pop();
					if (!Objects.equals(expected, actual)) {
						System.out.printf("Expected: <%s> but was: <%s>%n", expected, actual);
						valid[0] = false;
					}
				});
			});
			
			tokenizer.input("aaa\u00E5aaa", Collections.nCopies(7, new Object()));
			tokenizer.input("aaaa\u030Aaaa", Collections.nCopies(8, new Object()));
			assertTrue(valid[0]);
		} catch (IOException e) {
			fail(e);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(TokenProvider.class)
	void simpleStreamTokenization(@NotNull String[][] inputs, String[] tokenized) {
		var queue = new ArrayDeque<>(List.of(tokenized));
		SimpleToken[] tokens = {new SimpleToken("hello"), new SimpleToken("says?"), new SimpleToken(
				"worlds"), new SimpleToken(
				"world")};
		try (var tokenizer = new Tokenizer<>(tokens)) {
			final boolean[] valid = {true};
			
			tokenizer.setHandler((token, payload, type) -> {
				if (queue.isEmpty()) {
					valid[0] = false;
					return;
				}
				var expected = queue.pop();
				if (!Objects.equals(expected, token)) {
					System.out.printf("Expected: <%s> but was: <%s>%n", expected, token);
					valid[0] = false;
				}
				
			});
			
			for (String[] input : inputs) {
				for (String str : input)
					assertDoesNotThrow(() -> tokenizer.input(str, Collections.nCopies(str.length(), new Object())));
				assertTrue(tokenizer.tryFlush());
			}
			assertTrue(valid[0]);
			
		} catch (IOException e) {
			fail(e);
		}
		assertTrue(queue.isEmpty());
	}
	
	/** The TestToken is a simple implementation of {@link TokenDef} and should only be used by unit-tests. */
	static class SimpleToken implements TokenDef {
		private String regex;
		
		/**
		 * Initializes a new token-type using the given regex. The regex describes what text-patterns should be
		 * considered instances of this token.
		 *
		 * @param regex the regex that identifies instances of this token.
		 */
		@Contract(pure = true)
		SimpleToken(String regex) {
			this.regex = regex;
		}
		
		/** {@inheritDoc} */
		@Override
		public String getRegex() {
			return regex;
		}
	}
}
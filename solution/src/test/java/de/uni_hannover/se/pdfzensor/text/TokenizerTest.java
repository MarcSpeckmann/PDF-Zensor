package de.uni_hannover.se.pdfzensor.text;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.TestUtility;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.TokenProvider;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.text.Normalizer.Form.NFD;
import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
	
	@BeforeAll
	static void initLogging() {
		Logging.init(Level.DEBUG);
	}
	
	@Test
	void testDefaultHandler() {
		try (var tokenizer = new Tokenizer<>(TestToken.values())) {
			tokenizer.input("hello", Collections.nCopies(5, new Object()));
			assertTrue(tokenizer.tryFlush());
			
			assertDoesNotThrow(() -> tokenizer.setHandler(null));
			tokenizer.input("hello", Collections.nCopies(5, new Object()));
		} catch (Exception e) {
			fail(e);
		}
	}
	
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
	
	@Test
	void testLigatureTokenization() {
		try (var tokenizer = new Tokenizer<>(new SimpleToken("a\u030A"))) {
			var queue = "aaaa\u030Aaaaaaaa\u030Aaaa".chars().boxed().collect(Collectors.toCollection(ArrayDeque::new));
			tokenizer.setHandler((value, payload, token) -> {
				value = Normalizer.normalize(value, NFD);
				assertEquals(queue.pop(), value.chars().findFirst().orElseThrow());
			});
			
			tokenizer.input("aaa\u00E5aaa", Collections.nCopies(7, new Object()));
			tokenizer.input("aaaa\u030Aaaa", Collections.nCopies(8, new Object()));
		} catch (Exception e) {
			fail(e);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(TokenProvider.class)
	void simpleStreamTokenization(@NotNull String[][] inputs, String[] tokenized) {
		var queue = new ArrayDeque<>(List.of(tokenized));
		try (var tokenizer = new Tokenizer<>(TestToken.values())) {
			tokenizer.setHandler((token, payload, type) -> {
				assertFalse(queue.isEmpty());
				assertEquals(queue.pop(), token);
			});
			
			for (String[] input : inputs) {
				for (String str : input)
					assertDoesNotThrow(() -> tokenizer.input(str, Collections.nCopies(str.length(), new Object())));
				assertTrue(tokenizer.tryFlush());
			}
			
		} catch (IOException e) {
			fail(e);
		}
		assertTrue(queue.isEmpty());
	}
	
	/**
	 * The TestToken is a simple implementation of {@link TokenDef} and should only be used by unit-tests.
	 */
	enum TestToken implements TokenDef {
		HELLO("hello"), SAY("says?"), WORLDS("worlds"), WORLD("world");
		private String regex;
		
		/**
		 * Initializes a new token-type using the given regex. The regex describes what text-patterns should be
		 * considered instances of this token.
		 *
		 * @param regex the regex that identifies instances of this token.
		 */
		TestToken(String regex) {
			this.regex = regex;
		}
		
		/** {@inheritDoc} */
		@Override
		public String getRegex() {
			return regex;
		}
	}
	
	static class SimpleToken implements TokenDef {
		private String regex;
		
		SimpleToken(String regex) {
			this.regex = regex;
		}
		
		@Override
		public String getRegex() {
			return regex;
		}
	}
}
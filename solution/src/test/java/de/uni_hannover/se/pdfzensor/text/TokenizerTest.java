package de.uni_hannover.se.pdfzensor.text;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.testing.argumentproviders.TokenProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.Collections;

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
	@ArgumentsSource(TokenProvider.class)
	void simpleStreamTokenization(@NotNull String[][] inputs, String[] tokenized) {
		var ref = new Object() {
			int index = 0;
		};
		try (var tokenizer = new Tokenizer<>(TestToken.values())) {
			tokenizer.setHandler((token, payload, type) -> {
				assertTrue(ArrayUtils.isArrayIndexValid(tokenized, ref.index));
				assertEquals(tokenized[ref.index], token);
				ref.index++;
			});
			
			for (String[] input : inputs) {
				for (String str : input)
					assertDoesNotThrow(() -> tokenizer.input(str, Collections.nCopies(str.length(), new Object())));
				assertTrue(tokenizer.tryFlush());
			}
			
		} catch (IOException e) {
			fail(e);
		}
		assertEquals(tokenized.length, ref.index);
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
}
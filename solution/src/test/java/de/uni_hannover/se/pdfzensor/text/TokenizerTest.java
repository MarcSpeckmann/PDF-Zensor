package de.uni_hannover.se.pdfzensor.text;

import de.uni_hannover.se.pdfzensor.testing.argumentproviders.TokenProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
	
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
			e.printStackTrace();
		}
		assertEquals(ref.index, tokenized.length);
	}
	
	
	enum TestToken implements TokenDef {
		HELLO("hello", Color.black), WORLD("world", Color.white), WORLDS("worlds", Color.gray), SAY("say", Color.blue);
		Color color;
		String regex;
		
		TestToken(String regex, Color color) {
			this.color = color;
			this.regex = regex;
		}
		
		@Override
		public String getRegex() {
			return regex;
		}
	}
}
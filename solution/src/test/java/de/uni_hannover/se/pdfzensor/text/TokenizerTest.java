package de.uni_hannover.se.pdfzensor.text;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {
	
	@Test
	void callSST() {
		simpleStreamTokenization(new String[]{"helloworld"}, new String[]{"hello", "world"});
		simpleStreamTokenization(new String[]{"h", "e", "l", "l", "o", "w", "o", "r", "l", "d"}, new String[]{"hello", "world"});
		simpleStreamTokenization(new String[]{"w", "o", "r", "l", "d"}, new String[]{"world"});
		simpleStreamTokenization(new String[]{"w", "o", "r", "l", "d", "y"}, new String[]{"worldy"});
		simpleStreamTokenization(new String[]{"he", "lloworld", "ywo", "rld"}, new String[]{"hello", "worldy", "world"});
	}
	
	void simpleStreamTokenization(String[] input, String[] tokenized) {
		var tokenizer = new Tokenizer<>(TestToken.values());
		var ref = new Object() {
			int index = 0;
		};
		tokenizer.setHandler((token, payload, type) -> {
			assertEquals(tokenized[ref.index], token);
			ref.index++;
		});
		
		for (String str : input)
			assertDoesNotThrow(() -> tokenizer.input(str, Collections.nCopies(str.length(), new Object())));
	}
	
	
	enum TestToken implements TokenDef {
		HELLO("hello", Color.black), WORLD("world", Color.white), WORLDY("worldy", Color.gray);
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
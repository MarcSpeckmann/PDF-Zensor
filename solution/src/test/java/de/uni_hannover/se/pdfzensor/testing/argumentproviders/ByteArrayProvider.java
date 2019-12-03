package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class ByteArrayProvider implements ArgumentsProvider {
	private static final byte[] EMPTY = {};
	private static final byte[] ONLY_ZERO = {0x00};
	private static final byte[] EVERY_BYTE = new byte[256];
	private static final byte[] EVERY_BYTE_TWICE;
	
	static {
		for (int i = 0; i < EVERY_BYTE.length; i++)
			EVERY_BYTE[i] = (byte) (Byte.MIN_VALUE + i);
		EVERY_BYTE_TWICE = ArrayUtils.addAll(EVERY_BYTE, EVERY_BYTE);
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		return Stream.of(EMPTY, ONLY_ZERO, EVERY_BYTE, EVERY_BYTE_TWICE).map(Arguments::of);
	}
}

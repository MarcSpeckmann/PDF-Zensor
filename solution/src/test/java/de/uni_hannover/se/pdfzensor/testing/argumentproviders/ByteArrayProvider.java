package de.uni_hannover.se.pdfzensor.testing.argumentproviders;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * The ByteArrayProvider may be used as an argument-provider for JUnit tests. It only provides a single byte[] as an
 * argument. That byte[] may be used for what-ever purpose is desired.
 */
public class ByteArrayProvider implements ArgumentsProvider {
	/** An empty data-set. */
	private static final byte[] EMPTY = {};
	/** A data-set that only stores a single 0. */
	private static final byte[] ONLY_ZERO = {0x00};
	/** A data-set that contains every byte in order. */
	private static final byte[] EVERY_BYTE = new byte[256];
	/** The {@link #EVERY_BYTE} data-set appended to itself. */
	private static final byte[] EVERY_BYTE_TWICE;
	
	static {
		for (int i = 0; i < EVERY_BYTE.length; i++)
			EVERY_BYTE[i] = (byte) (Byte.MIN_VALUE + i);
		EVERY_BYTE_TWICE = ArrayUtils.addAll(EVERY_BYTE, EVERY_BYTE);
	}
	
	/** {@inheritDoc} */
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
		return Stream.of(EMPTY, ONLY_ZERO, EVERY_BYTE, EVERY_BYTE_TWICE).map(Arguments::of);
	}
}

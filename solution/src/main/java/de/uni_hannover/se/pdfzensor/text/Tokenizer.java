package de.uni_hannover.se.pdfzensor.text;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Flushable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Tokenizer provides the means to tokenize an input-stream that contains payload. That means that the tokenizer
 * operates on a series of (char, payload)-tuples. When it finds a specified tokens a handler gets called with the
 * token's value, each character's payload and the token-definition that matched.
 *
 * @param <T> the token-implementation to be used by the tokenizer
 * @param <C> the payload-type
 */
public class Tokenizer<T extends TokenDef, C> implements AutoCloseable, Flushable {
	private static final Logger LOGGER = Logging.getLogger();
	private final T[] tokens;
	private final Pattern pattern;
	/**
	 * OutputStream and InputStream form a pipe. Data gets written to the output-stream via {@link #input(String,
	 * Collection)} and the scanner reads from the input-stream.
	 */
	private PipedOutputStream outputStream;
	private Thread thread;
	/**
	 * Payload contains a queue with the payload for each token that is currently in the stream. It is mandatory that
	 * they payload of any character that gets added to the stream is added to this queue. When a character got matched
	 * and is removed from the stream it's corresponding payload is removed from the queue to be passed to the handler.
	 */
	@NotNull
	private Queue<C> payload = new ConcurrentLinkedQueue<>();
	
	/**
	 * Holds the current handler-callback that will be called once a token was read. By default it is {@link
	 * #emptyHandle(String, List, TokenDef)}.
	 */
	@NotNull
	private TriConsumer<String, List<C>, T> handler = this::emptyHandle;
	
	/**
	 * Creates a new tokenizer from the passed tokens.
	 *
	 * @param tokens the token-types the tokenizer parses
	 */
	@SafeVarargs
	public Tokenizer(@NotNull T... tokens) {
		this.tokens = Validate.noNullElements(tokens);
		final var regex = Arrays.stream(tokens).map(T::getRegex).collect(Collectors.joining(")|(", "(", ")"));
		pattern = Pattern.compile(String.format("(?:%s)(?=%s|$)", regex, regex), Pattern.DOTALL);
		LOGGER.debug("Initialized tokenizer with the pattern: {}", pattern::pattern);
		setupParser();
	}
	
	/**
	 * Initializes the pipe and starts a scanner to scan in a new thread for the tokens on the piped input. You may use
	 * {@link #close()} to deinitialize the parsing-process.
	 */
	private void setupParser() {
		var latch = new CountDownLatch(1); //a latch used to wait for the initialization to be done in thread
		thread = new Thread(() -> {
			try {
				outputStream = new PipedOutputStream();
				var inputStream = new PipedInputStream(outputStream);
				try (var scanner = new Scanner(inputStream)) {
					latch.countDown();
					scanner.findAll(pattern).forEach(this::onTokenEncountered);
					inputStream.close();
				}
			} catch (IOException e) {
				LOGGER.fatal("An error occurred while tokenizing", e);
			}
		});
		thread.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.warn("Waiting for the thread to initialize got interrupted", e);
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * <b><i>Do not call this method manually! It is meant to be a callback only.</i></b><br>
	 *
	 * @param result
	 */
	private void onTokenEncountered(@NotNull MatchResult result) {
		Objects.requireNonNull(result);
		int group = IntStream.rangeClosed(1, result.groupCount()).filter(i -> Objects.nonNull(result.group(i)))
							 .findFirst()
							 .orElseThrow(//This case should be impossible
										  () -> new RuntimeException(
												  "The token matched anything but no capture group"));
		var resultPayload = pop(result.group().length());
		//group is 1-based (0 marks the entire match) so we have to subtract one to make it 0-based
		handler.accept(result.group(), resultPayload, tokens[group - 1]);
	}
	
	/**
	 * Returns an unmodifiable list of the first count values in the {@link #payload}-Queue.
	 *
	 * @param count the number of entries to dequeue (should not be negative)
	 * @return an unmodifiable list containing the former first count elements of {@link #payload}
	 */
	private List<C> pop(int count) {
		return Stream.generate(payload::remove).limit(count).collect(Collectors.toUnmodifiableList());
	}
	
	@Override
	public void flush() throws IOException {
		close();
		setupParser();
	}
	
	/**
	 * Closes all streams and other resources associated with the tokenizer. Waits for the scanner-thread to close down
	 * before returning.
	 *
	 * @throws IOException if an I/O Error occurs
	 */
	@Override
	public void close() throws IOException {
		outputStream.close();
		try {
			thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Failed to join thread", e);
		}
	}
	
	public boolean tryFlush() {
		boolean success = false;
		try {
			flush();
			success = true;
		} catch (Exception e) {
			LOGGER.error("Failed to flush the current stream", e);
		}
		return success;
	}
	
	public void input(@NotNull String data, @NotNull Collection<? extends C> payload) throws IOException {
		Objects.requireNonNull(data);
		Objects.requireNonNull(payload);
		Validate.validState(data.length() == payload.size(),
							String.format("Data length (%d) and payload size (%d) do not match for data: %s",
										  data.length(), payload.size(), data));
		this.payload.addAll(payload);
		outputStream.write(data.getBytes());
	}
	
	
	/**
	 * Sets a new handler to handle parsed tokens. Overwrites an existing one if set. Removes the handler if null was
	 * passed.
	 *
	 * @param handler the new handler or null to just remove the old one
	 */
	public void setHandler(@Nullable TriConsumer<String, List<C>, T> handler) {
		this.handler = Optional.ofNullable(handler).orElse(this::emptyHandle);
	}
	
	/**
	 * A dummy-(/empty) handler. Should be used as a default value for {@link #handler} instead of null.
	 */
	private void emptyHandle(String value, List<C> payload, T token) {
		/*Intentionally left blank*/
	}
}

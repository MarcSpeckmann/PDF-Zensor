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
import java.util.regex.PatternSyntaxException;
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
@SuppressWarnings("WeakerAccess")
public class Tokenizer<T extends TokenDef, C> implements AutoCloseable, Flushable {
	/** The Logger-instance instances of this class should log their output into. */
	private static final Logger LOGGER = Logging.getLogger();
	/** The T[] represents the token-definitions as they were passed to {@link #Tokenizer(TokenDef[])}. */
	@NotNull
	private final T[] tokens;
	/**
	 * The pattern contains a regex that is used to find occurrences of the {@link #tokens} within the input. To enable
	 * resolving which token was responsible for the match, each token-definition gets its own capture group. When a
	 * match was found we can just search for the matching capture group and index it into the {@link #tokens}. To allow
	 * for that the order of the tokens' regex has to coincide with the order of the token-definitions in {@link
	 * #tokens}.
	 *
	 * @see #onTokenEncountered(MatchResult)
	 */
	@NotNull
	private final Pattern pattern;
	/**
	 * Payload contains a queue with the payload for each token that is currently in the stream. It is mandatory that
	 * they payload of any character that gets added to the stream is added to this queue. When a character got matched
	 * and is removed from the stream it's corresponding payload is removed from the queue to be passed to the handler.
	 */
	@NotNull
	private final Queue<C> payload = new ConcurrentLinkedQueue<>();
	/**
	 * OutputStream and InputStream form a pipe. Data gets written to the output-stream via {@link #input(String,
	 * Collection)} and the scanner reads from the input-stream inside the scanner-thread ({@link #thread}).
	 */
	private PipedOutputStream outputStream;
	/** Stores the current scanner-thread that is responsible for waiting for input and tokenizing it. */
	private Thread thread;
	/**
	 * Holds the current handler-callback that will be called once a token was read. By default it is {@link
	 * #emptyHandle(String, List, TokenDef)}.
	 */
	@NotNull
	private TriConsumer<String, List<C>, @Nullable T> handler = this::emptyHandle;
	
	/**
	 * Creates a new tokenizer from the passed tokens.
	 *
	 * @param tokens the token-types the tokenizer parses
	 */
	@SafeVarargs
	public Tokenizer(@NotNull T... tokens) {
		this.tokens = Validate.noNullElements(tokens);
		final var regex = Arrays.stream(tokens).map(T::getRegex).filter(Tokenizer::isTokenValid)
								.collect(Collectors.joining(")|(", "(", ")"));
		pattern = Pattern.compile(regex + "|.", Pattern.DOTALL | Pattern.CANON_EQ);
		LOGGER.debug("Initialized tokenizer with the pattern: {}", pattern::pattern);
		setupParser();
	}
	
	/**
	 * Checks if the passed regex is a valid token. A regex is considered valid if it is a valid regex, does not match
	 * the empty string and has no capture-groups.
	 *
	 * @param regex the regex to check for if it is a valid token.
	 * @return true if the regex is a valid token.
	 */
	private static boolean isTokenValid(String regex) {
		boolean valid = false;
		try {
			boolean matchEmpty = "".matches(regex);
			boolean hasCaptureGroup = Pattern.compile(regex).matcher("").groupCount() > 0;
			if (matchEmpty)
				LOGGER.warn("The defined token '{}' could match the empty string and thus is ignored", regex);
			else if (hasCaptureGroup)
				LOGGER.warn("The defined token '{}' contains a capture group and thus is ignored", regex);
			
			valid = !matchEmpty;
			valid &= !hasCaptureGroup;
		} catch (PatternSyntaxException exception) {
			LOGGER.warn("The defined token '{}' is not a correct regex: {}", regex, exception.getDescription());
		}
		return valid;
	}
	
	/**
	 * Initializes the pipe and starts a scanner to scan in a new thread for the tokens on the piped input. You may use
	 * {@link #close()} to deinitialize the parsing-process.
	 */
	private void setupParser() {
		var latch = new CountDownLatch(1); //a latch used to wait for the initialization to be done in thread
		thread = new Thread(() -> {
			LOGGER.debug("Initializing in the scanner-thread");
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
		}, "Token-Scanner");
		LOGGER.debug("Starting the scanner-thread");
		thread.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			LOGGER.warn("Waiting for the thread to initialize got interrupted", e);
			Thread.currentThread().interrupt();
		}
		LOGGER.debug("Tokenizer initialized and thread started");
	}
	
	/**
	 * <b><i>Do not call this method manually! It is meant to be a callback only.</i></b><br>
	 * This method is called when a token was encountered by the scanner-thread ({@link #thread}. It contains the
	 * information about the found token-match in form of a {@link MatchResult} and is responsible for handling the
	 * match. "Handling" includes finding the first capturing-group that is not null and indexing that index into the
	 * token-definitions. This works because we generate our {@link #pattern} such that each token has its own capture
	 * group and these are ordered according to the ordering in the token-definition array ({@link #tokens}). When a
	 * group is found (or <code>null</code> for when the character could not be matched), the correct amount of payload
	 * is dequeued from {@link #payload} via {@link #pop(int)} and the {@link #handler} is called.
	 *
	 * @param result the match result representing the found token in the input-stream. May not be null.
	 */
	private void onTokenEncountered(@NotNull MatchResult result) {
		Objects.requireNonNull(result);
		//group is 1-based (0 marks the entire match) so we have to subtract one to make it 0-based and index into tokens
		T token = IntStream.rangeClosed(1, result.groupCount()).filter(i -> Objects.nonNull(result.group(i)))
						   .mapToObj(g -> tokens[g - 1]).findFirst().orElse(null);
		var resultPayload = pop(result.group().length());
		handler.accept(result.group(), resultPayload, token);
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
	
	/**
	 * Closes the parser and reopens it. This triggers the current input to be matched and thus the handler being called
	 * on it.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	@Override
	public void flush() throws IOException {
		LOGGER.debug("Flushing the tokenizer...");
		close();
		setupParser();
	}
	
	/**
	 * Closes all streams and other resources associated with the tokenizer. Waits for the scanner-thread to close down
	 * before returning. A closed closed tokenizer may be reopened by calling {@link #flush()}.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public void close() throws IOException {
		LOGGER.debug("Closing the tokenizer...");
		outputStream.close();
		try {
			thread.join();
		} catch (InterruptedException e) {
			LOGGER.warn("Failed to join thread", e);
			Thread.currentThread().interrupt();
		}
		payload.clear();
		thread = null;
		outputStream = null;
	}
	
	/**
	 * Tries to {@link #flush()} the tokenizer. If that throws an {@link IOException} false is returned. On success true
	 * is returned.
	 *
	 * @return Returns true if {@link #flush()} was called successfully (nothing was thrown). False otherwise.
	 * @see #flush()
	 */
	public boolean tryFlush() {
		boolean success = false;
		try {
			flush();
			success = true;
		} catch (IOException e) {
			LOGGER.error("Failed to flush the current stream", e);
		}
		return success;
	}
	
	/**
	 * The input-method should be used to pass data into the tokenizer. Via the {@link #outputStream}-End of the pipe
	 * the data is passed into the scanner-thread ({@link #thread}) where it is matched. The payload at index
	 * <code>i</code> corresponds to the character at index <code>i</code> in the data. Once a character was matched it
	 * is passed to the {@link #handler} with its correlating payload.
	 *
	 * @param data    the input-text that should be tokenized. Not <code>null</code>.
	 * @param payload the payload of the data. When the handler is called the payload corresponding to the token is
	 *                passed back. Not <code>null</code>.
	 * @throws IOException              if an I/O error occurs.
	 * @throws NullPointerException     if data or payload are <code>null</code>.
	 * @throws IllegalArgumentException if data.length() and payload.size() are not equal.
	 * @see #setHandler(TriConsumer)
	 */
	public void input(@NotNull String data, @NotNull Collection<? extends C> payload) throws IOException {
		Objects.requireNonNull(data);
		Objects.requireNonNull(payload);
		Validate.isTrue(data.length() == payload.size(),
						String.format("Data length (%d) and payload size (%d) do not match for data: \"%s\"",
									  data.length(), payload.size(), data));
		outputStream.write(data.getBytes());
		this.payload.addAll(payload);
	}
	
	/**
	 * Sets a new handler to handle parsed tokens. Overwrites an existing one if set. Removes the handler if null was
	 * passed. The handler has to take 3 arguments: a {@link String} containing the value of the matched token, a
	 * {@link List<C>} containing the payload of each character of the token, and a nullable {@link T} that represents
	 * the token-definition that was matched. It is guaranteed that the length of the String and the List is the same
	 * and the order of the payload in the list corresponds to the character-order in the String. The token-definition
	 * is null if no token could be matched.
	 *
	 * @param handler the new handler or null to just remove the old one
	 */
	public void setHandler(@Nullable TriConsumer<String, List<C>, @Nullable T> handler) {
		this.handler = Optional.ofNullable(handler).orElse(this::emptyHandle);
	}
	
	/**
	 * A dummy-(/empty) handler. Should be used as a default value for {@link #handler} instead of null.
	 */
	private void emptyHandle(String value, List<C> payload, @Nullable T token) {
		/*Intentionally left blank*/
	}
}

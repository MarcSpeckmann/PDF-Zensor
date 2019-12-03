package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.censor.utils.DoubleBufferedStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class StackCheckingStreamProcessor extends PDFStreamProcessor {
	private int expStackSize;
	
	/**
	 * @throws IOException If there is an error loading the properties.
	 */
	StackCheckingStreamProcessor() throws IOException {}
	
	@Nullable
	@SuppressWarnings("unchecked")
	private static Deque<DoubleBufferedStream> getStreamStack(PDFStreamProcessor processor) {
		Deque<DoubleBufferedStream> result = null;
		try {
			var field = PDFStreamProcessor.class.getDeclaredField("currentStream");
			field.setAccessible(true);
			var value = field.get(processor);
			result = (Deque<DoubleBufferedStream>) value;
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the stream-stack", e);
		}
		return result;
	}
	
	/**
	 * Tests if the stack state is correct before and after starting to process a new document.
	 * <ul>
	 * 	<li>null before starting a new document
	 * 	<li>not null after starting a new document
	 * 	<li>empty after starting a new document
	 * 	<li>stack is empty: peeking will return null
	 * </ul>
	 *
	 * @param document The PDF document that is being processed.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void startDocument(@NotNull final PDDocument document) throws IOException {
		assertNull(getStreamStack(this));
		
		super.startDocument(document);
		
		var stackAfter = getStreamStack(this);
		assertNotNull(stackAfter);
		expStackSize = 0;
		assertEquals(expStackSize, stackAfter.size());
		assertThrows(NullPointerException.class, this::getCurrentContentStream);
	}
	
	/**
	 * Tests if the stack state is correct before and after ending to process a new document.
	 * <ul>
	 * 	<li>not null before ending the document
	 * 	<li>empty before ending the document
	 * 	<li>null after ending the document
	 * 	<li>stack is null: contentStream will return null and not an exception
	 * </ul>
	 *
	 * @param document The PDF document that has been processed.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void endDocument(PDDocument document) throws IOException {
		var stackBefore = getStreamStack(this);
		assertNotNull(stackBefore);
		assertEquals(expStackSize, stackBefore.size());
		assertTrue(stackBefore.isEmpty());
		assertEquals(0, expStackSize);
		
		super.endDocument(document);
		
		assertNull(getStreamStack(this));
		assertNull(this.getCurrentContentStream());
	}
	
	/**
	 * Tests if the stack state is correct before and after starting to process a new page.
	 * <ul>
	 * 	<li>not null before starting the page
	 * 	<li>variable for testing the stack size is still correct before process on a new page is started
	 * 	<li>not null after starting the page
	 * 	<li>variable for testing the stack size is still correct after process on a new page has been started
	 * </ul>
	 *
	 * @param page The page we are about to process.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void startPage(@NotNull final PDPage page) throws IOException {
		var stackBefore = getStreamStack(this);
		assertNotNull(stackBefore);
		assertEquals(expStackSize, stackBefore.size());
		
		super.startPage(page);
		
		expStackSize++;
		var stackAfter = getStreamStack(this);
		assertNotNull(stackAfter);
		assertEquals(expStackSize, stackAfter.size());
		assertDoesNotThrow(this::getCurrentContentStream);
	}
	
	/**
	 * Tests if the stack state is correct before and after starting to process a new page.
	 * <ul>
	 * 	<li>not null before ending the page
	 * 	<li>variable for testing the stack size is still correct before process on the page is ended
	 * 	<li>not null after ending the page
	 * 	<li>variable for testing the stack size is still correct after process on the page has ended
	 * </ul>
	 *
	 * @param page The page we just finished processing.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	protected void endPage(PDPage page) throws IOException {
		var stackBefore = getStreamStack(this);
		assertNotNull(stackBefore);
		assertEquals(expStackSize, stackBefore.size());
		
		super.endPage(page);
		
		expStackSize--;
		var stackAfter = getStreamStack(this);
		assertNotNull(stackAfter);
		assertEquals(expStackSize, stackAfter.size());
	}
	
	/**
	 * Tests if the stack state is correct before and after processing a transparency group.
	 * <ul>
	 * 	<li>not null before processing the transparency group
	 * 	<li>variable for testing the stack size is still correct before processing the transparency group
	 * 	<li>not null after processing the transparency group
	 * 	<li>variable for testing the stack size is still correct after the transparency group has been processed
	 * </ul>
	 *
	 * @param form transparency group (form) XObject
	 * @throws IOException if the transparency group cannot be processed
	 */
	@Override
	public void showTransparencyGroup(@NotNull final PDTransparencyGroup form) throws IOException {
		var stackBefore = getStreamStack(this);
		assertNotNull(stackBefore);
		assertEquals(expStackSize, stackBefore.size());
		
		expStackSize++;
		super.showTransparencyGroup(form);
		expStackSize--;
		
		var stackAfter = getStreamStack(this);
		assertNotNull(stackAfter);
		assertEquals(expStackSize, stackAfter.size());
	}
	
	/**
	 * Tests if the stack state is correct before and after processing a formXObject.
	 * <ul>
	 * 	<li>not null before processing the formXObject
	 * 	<li>variable for testing the stack size is still correct before processing the formXObject
	 * 	<li>not null after processing the formXObject
	 * 	<li>variable for testing the stack size is still correct after the formXObject has been processed
	 * </ul>
	 *
	 * @param form form XObject
	 * @throws IOException if the form cannot be processed
	 */
	@Override
	public void showForm(@NotNull final PDFormXObject form) throws IOException {
		var stackBefore = getStreamStack(this);
		assertNotNull(stackBefore);
		assertEquals(expStackSize, stackBefore.size());
		
		expStackSize++;
		super.showForm(form);
		expStackSize--;
		
		var stackAfter = getStreamStack(this);
		assertNotNull(stackAfter);
		assertEquals(expStackSize, stackAfter.size());
	}
}
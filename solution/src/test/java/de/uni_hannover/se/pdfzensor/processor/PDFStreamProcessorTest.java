package de.uni_hannover.se.pdfzensor.processor;

import de.uni_hannover.se.pdfzensor.TestUtility;
import de.uni_hannover.se.pdfzensor.censor.utils.DoubleBufferedStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PDFStreamProcessorTest {
	private static final String PDF_PATH = "/pdf-files/";
	
	@Nullable
	@SuppressWarnings("unchecked")
	private static Deque<DoubleBufferedStream> getStreamStack(PDFStreamProcessor processor) {
		try {
			var field = PDFStreamProcessor.class.getDeclaredField("currentStream");
			field.setAccessible(true);
			var value = field.get(processor);
			return (Deque<DoubleBufferedStream>) value;
		} catch (Exception e) {
			Assertions.fail("Could not retrieve the stream-stack", e);
		}
		return null;
	}
	
	/**
	 * @return A stream of all files in the directory {@link #PDF_PATH}.
	 * @throws IOException If there is an error loading the files.
	 */
	private static Stream<Arguments> testForFile() throws IOException {
		return Files.walk(Paths.get(TestUtility.getResource(PDF_PATH).getAbsolutePath())).map(Path::toFile)
					.filter(File::isFile).map(Arguments::of);
	}
	
	/**
	 * @throws IOException If there is an error loading the properties.
	 */
	@Test
	void testIllegalArguments() throws IOException {
		final var processor = new PDFStreamProcessor();
		assertThrows(NullPointerException.class, () -> processor.getText(null));
	}
	
	/**
	 * @throws IOException If there is an error loading the file or properties.
	 */
	@ParameterizedTest(name = "Run {index}: file: {0}")
	@MethodSource("testForFile")
	void testStreamStack(File file) throws IOException {
		final var processor = new CheckPDFStreamProcessorStack();
		
		var doc = PDDocument.load(file);
		assertNotNull(doc);
		assertDoesNotThrow(() -> processor.writeText(doc, new StringWriter()));
		doc.close();
	}
	
	private static class CheckPDFStreamProcessorStack extends PDFStreamProcessor {
		private int expStackSize;
		
		/**
		 * @throws IOException If there is an error loading the properties.
		 */
		private CheckPDFStreamProcessorStack() throws IOException {}
		
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
}
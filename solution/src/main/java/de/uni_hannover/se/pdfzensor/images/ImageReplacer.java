package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.Logging;
import de.uni_hannover.se.pdfzensor.censor.utils.PDFUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.pdfbox.contentstream.operator.OperatorName.DRAW_OBJECT;

/**
 * This class is responsible for replacing pictures with a box. For finding picture inside the PDF {@link ImageReplacer}
 * inherits from {@link PDFStreamEngine} and overrides the {@link PDFStreamEngine#processOperator(Operator, List)}
 */
public class ImageReplacer extends PDFStreamEngine {
	/** A {@link Logger}-instance that should be used by this class' member methods to log their state and errors. */
	private static final Logger LOGGER = Logging.getLogger();
	/** The unit-rect is at the origin an has extends of one in each direction. It may be used as default image-bounds. */
	private static final PDRectangle UNIT_RECT = new PDRectangle(0, 0, 1, 1);
	
	/** A {@link List} should contain all bounding boxes of the found pictures on the actual page. */
	final List<Rectangle2D> rects = new ArrayList<>();
	
	
	/**
	 * The Constructor of {@link ImageReplacer}, which is responsible for preparing the {@link PDFStreamEngine}. It is
	 * adding operators to the {@link PDFStreamEngine} which will be processed by the {@link PDFStreamEngine}.
	 */
	public ImageReplacer() {
		//cm: Concatenate matrix to current transformation matrix.
		addOperator(new Concatenate());
		//Do: Draws an XObject.
		addOperator(new DrawObject());
		//gs: Set parameters from graphics state parameter dictionary.
		addOperator(new SetGraphicsStateParameters());
		//q: Save the graphics state.
		addOperator(new Save());
		//Q: Restore the graphics state.
		addOperator(new Restore());
		//Tm: Set text matrix and text line matrix.
		addOperator(new SetMatrix());
	}
	
	/**
	 * Removes the image data from each page of the provided document.
	 *
	 * @param document the document to remove all image resources from.
	 * @see #removeImageData(PDPage)
	 */
	public static void removeImageData(@NotNull PDDocument document) {
		Objects.requireNonNull(document).getPages().forEach(ImageReplacer::removeImageData);
	}
	
	/**
	 * Strips the data of the image resources from the provided page. We define all PDXObjects to be "image resources".
	 *
	 * @param page the page to remove all image resources from.
	 */
	public static void removeImageData(@NotNull PDPage page) {
		var resources = Objects.requireNonNull(page).getResources();
		if (resources == null) {
			LOGGER.warn("The page does not contain a resource dictionary which conflicts with the pdf-specification");
			resources = new PDResources();
			page.setResources(resources);
		}
		resources.getCOSObject().setItem(COSName.XOBJECT, null);
	}
	
	/**
	 * This method is provided to the outside of {@link ImageReplacer}.
	 *
	 * @param doc  the {@link PDDocument} which is being worked on
	 * @param page the {@link PDPage} (current pdf page) that is being worked on
	 * @return an Array with all bounding boxes of the pictures on the actual page as {@link Rectangle2D }
	 * @throws IOException If there was an I/O error writing the contents of the page.
	 */
	@NotNull
	public List<Rectangle2D> replaceImages(PDDocument doc, PDPage page) throws IOException {
		Objects.requireNonNull(doc);
		Objects.requireNonNull(page);
		LOGGER.info("Starting to process Images of page {}/{}", () -> doc.getPages().indexOf(page) + 1,
					doc::getNumberOfPages);
		
		this.processPage(page);
		
		try (var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true,
															 true)) {
			pageContentStream.setStrokingColor(Color.DARK_GRAY);
			pageContentStream.setLineWidth(2);
			drawPictureCensorBox(pageContentStream);
		}
		return this.rects;
		
	}
	
	/**
	 * This method overwrites the {@link PDFStreamEngine#processOperator(Operator, List)}. This method checks if the
	 * actual found operator is a {@link DrawObject} operator and adds it to the {@link #rects}.
	 *
	 * @param operator The operation to perform.
	 * @param operands The list of arguments.
	 * @throws IOException If there is an error processing the operation.
	 */
	@Override
	protected void processOperator(@NotNull final Operator operator, final List<COSBase> operands) throws IOException {
		Objects.requireNonNull(operands);
		Objects.requireNonNull(operator);
		if (DRAW_OBJECT.equals(operator.getName())) {
			COSName objectName = (COSName) operands.get(0);
			// get the PDF object
			PDXObject xObject = getResources().getXObject(objectName);
			rects.add(getBounds(xObject, objectName));
		} else {
			super.processOperator(operator, operands);
		}
	}
	
	/**
	 * Retrieves the transformed bounding-box of the provided PDXObject. That is the bounding-box of the XObject
	 * transformed using the current transformation matrix (ctm). If the XObject is no FormXObject, the unit-rect
	 * (0,&nbsp;0,&nbsp;&nbsp;1,&nbsp;1) is used.
	 *
	 * @param object the XObject to retrieve the transformed bounding-box of.
	 * @param name   the name of the XObject under which it is stored in the resources. This only serves logging
	 *               purposes.
	 * @return the transformed bounding-box of the PDXObject.
	 */
	@NotNull
	private Rectangle2D getBounds(@NotNull PDXObject object, @NotNull COSName name) {
		Objects.requireNonNull(object);
		Objects.requireNonNull(name);
		
		var ctm = getGraphicsState().getCurrentTransformationMatrix();
		var at = ctm.createAffineTransform();
		var bounds = UNIT_RECT;
		if (object instanceof PDFormXObject)
			bounds = ((PDFormXObject) object).getBBox();
		var transformed = at.createTransformedShape(PDFUtils.pdRectToRect2D(bounds)).getBounds2D();
		LOGGER.info("{} \"{}\" at ({}, {}) size: {}\u00D7{}", object.getClass().getSimpleName(), name.getName(),
					transformed.getX(), transformed.getY(), transformed.getWidth(), transformed.getHeight());
		return transformed;
	}
	
	/**
	 * Draws the censor bars stored in {@link #rects} in the given
	 * <code>document</code> on the given <code>page</code>.
	 *
	 * @throws IOException If there was an I/O error writing the contents of the page.
	 */
	private void drawPictureCensorBox(@NotNull PDPageContentStream pageContentStream) throws IOException {
		Objects.requireNonNull(pageContentStream);
		for (var rect : this.rects) {
			pageContentStream.addRect((float) rect.getMinX(), (float) rect.getMinY(), (float) rect.getWidth(),
									  (float) rect.getHeight());
			pageContentStream.moveTo((float) rect.getMaxX(), (float) rect.getMaxY());
			pageContentStream.lineTo((float) rect.getMinX(), (float) rect.getMinY());
			pageContentStream.moveTo((float) rect.getMaxX(), (float) rect.getMinY());
			pageContentStream.lineTo((float) rect.getMinX(), (float) rect.getMaxY());
			pageContentStream.stroke();
		}
	}
	
}

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
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
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
	
	/**
	 * A {@link List} should contain all bounding boxes of the found pictures on the actual page
	 */
	final List<Rectangle2D> rects = new ArrayList<>();
	
	
	/**
	 * The Constructor of {@link ImageReplacer}, which is responsible for preparing the {@link PDFStreamEngine}.
	 * It is adding operators to the {@link PDFStreamEngine} which will be processed by the {@link PDFStreamEngine}.
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
		LOGGER.info("Starting to process Images of page{}", page);
		
		this.processPage(page);
		
		var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.PREPEND, true);
		pageContentStream.saveGraphicsState();
		pageContentStream.close();
		
		pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
		pageContentStream.restoreGraphicsState();
		pageContentStream.setStrokingColor(Color.DARK_GRAY);
		pageContentStream.setLineWidth(2);
		drawPictureCensorBox(pageContentStream);
		pageContentStream.close();
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
			PDXObject xobject = getResources().getXObject(objectName);
			if (xobject instanceof PDImageXObject) {
				
				this.rects.add(getPDImageBB((PDImageXObject) xobject, objectName));
				
			} else if (xobject instanceof PDFormXObject) {
				
				this.rects.add(getPDFormBB((PDFormXObject) xobject, objectName));
				
			}
		} else {
			super.processOperator(operator, operands);
		}
	}
	
	/**
	 * This method is adding the bounding box of the {@link PDImageXObject} to the {@link #rects}, but only if the
	 * {@link PDImageXObject} isn't a stencil.
	 *
	 * @param image      The {@link PDImageXObject} where we want to get the position from.
	 * @param objectName The name of the {@link PDImageXObject} image.
	 */
	@NotNull
	private Rectangle2D getPDImageBB(@NotNull PDImageXObject image, COSName objectName) {
		Objects.requireNonNull(image);
		Objects.requireNonNull(objectName);
		
		Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
		var at = ctm.createAffineTransform();
		var shape = at.createTransformedShape(new Rectangle2D.Double(0, 0, 1, 1));
		var boundingbox = shape.getBounds2D();
		LOGGER.info("PDImageXObject [{}]", objectName.getName());
		LOGGER.info("Position in PDF = \"{}\", \"{}\" in user space units", boundingbox.getX(),
					boundingbox.getY());
		LOGGER.info("Displayed size  = \"{}\", \"{}\" in user space units", boundingbox.getWidth(),
					boundingbox.getHeight());
		return boundingbox;
		
	}
	
	/**
	 * This method is adding the bounding box of the {@link PDFormXObject} to the {@link #rects}.
	 *
	 * @param form       The {@link PDFormXObject} where we want to get the position from.
	 * @param objectName The name of the {@link PDFormXObject} form.
	 */
	@NotNull
	private Rectangle2D getPDFormBB(@NotNull PDFormXObject form, @NotNull COSName objectName) {
		Objects.requireNonNull(form);
		Objects.requireNonNull(objectName);
		Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
		var at = ctm.createAffineTransform();
		var shape = at.createTransformedShape(PDFUtils.pdRectToRect2D(form.getBBox()));
		var boundingbox = shape.getBounds2D();
		LOGGER.info("PDFormXObject [{}]", objectName.getName());
		LOGGER.info("Position in PDF = \"{}\", \"{}\" in user space units", boundingbox.getX(),
					boundingbox.getY());
		LOGGER.info("Displayed size  = \"{}\", \"{}\" in user space units", boundingbox.getWidth(),
					boundingbox.getHeight());
		return boundingbox;
	}
	
	
	/**
	 * Draws the censor bars stored in {@link #rects} in the given
	 * <code>document</code> on the given <code>page</code>.
	 *
	 * @throws IOException If there was an I/O error writing the contents of the page.
	 */
	private void drawPictureCensorBox(PDPageContentStream pageContentStream) throws IOException {
		Objects.requireNonNull(pageContentStream);
		for (var rect : this.rects) {
			pageContentStream.addRect((float) rect.getX(), (float) rect.getY(), (float) rect.getWidth(),
									  (float) rect.getHeight());
			pageContentStream.moveTo((float) rect.getX(), (float) rect.getY());
			pageContentStream.lineTo((float) rect.getX() + (float) rect.getWidth(),
									 (float) rect.getY() + (float) rect.getHeight());
			pageContentStream.moveTo((float) rect.getX(), (float) rect.getY() + (float) rect.getHeight());
			pageContentStream.lineTo((float) rect.getX() + (float) rect.getWidth(), (float) rect.getY());
			pageContentStream.stroke();
		}
		
	}
	
}

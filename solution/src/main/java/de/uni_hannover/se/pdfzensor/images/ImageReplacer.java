package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.Logging;
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
	 * A {@link PDPageContentStream} contains the ContentStream of the actual page
	 */
	private PDPageContentStream pageContentStream;
	
	/**
	 * The Constructor of {@link ImageReplacer}, which is responsible for preparing the {@link PDFStreamEngine}.
	 */
	public ImageReplacer() {
		// preparing PDFStreamEngine
		addOperator(new Concatenate());
		addOperator(new DrawObject());
		addOperator(new SetGraphicsStateParameters());
		addOperator(new Save());
		addOperator(new Restore());
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
		LOGGER.info("Starting to process Images of page{}", page);
		
		this.processPage(page);
		
		pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
		pageContentStream.setStrokingColor(Color.DARK_GRAY);
		pageContentStream.setLineWidth(2);
		drawPictureCensorBox();
		pageContentStream.close();
		return this.rects;
		
	}
	
	/**
	 * This method overwrites the {@link PDFStreamEngine#processOperator(Operator, List)}. This method checks if the
	 * actual found operator is a {@link DrawObject} operator and adds it to the {@link #rects}.
	 *
	 * @param operator
	 * @param operands
	 * @throws IOException
	 */
	@Override
	protected void processOperator(final Operator operator, final List<COSBase> operands) throws IOException {
		if (DRAW_OBJECT.equals(operator.getName())) {
			COSName objectName = (COSName) operands.get(0);
			// get the PDF object
			PDXObject xobject = getResources().getXObject(objectName);
			// check if the object is an image object
			if (xobject instanceof PDImageXObject) {
				//PDImageXObject image = (PDImageXObject) xobject;
				Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
				LOGGER.info("Image [{}]", objectName.getName());
				LOGGER.info("Position in PDF = \"{}\", \"{}\" in user space units", ctmNew.getTranslateX(),
							ctmNew.getTranslateY());
				LOGGER.info("Displayed size  = \"{}\", \"{}\" in user space units", ctmNew.getScalingFactorX(),
							ctmNew.getScalingFactorY());
				rects.add(new Rectangle2D.Float(ctmNew.getTranslateX(), ctmNew.getTranslateY(),
												ctmNew.getScalingFactorX(),
												ctmNew.getScalingFactorY()));
				//((PDImageXObject) xobject).setBitsPerComponent(0);
			} else if (xobject instanceof PDFormXObject) {
				PDFormXObject form = (PDFormXObject) xobject;
				showForm(form);
			}
		} else {
			super.processOperator(operator, operands);
		}
	}
	
	/**
	 * Draws the censor bars stored in {@link #rects} in the given
	 * <code>document</code> on the given <code>page</code>.
	 *
	 * @throws IOException If there was an I/O error writing the contents of the page.
	 */
	private void drawPictureCensorBox() throws IOException {
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

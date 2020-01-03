package de.uni_hannover.se.pdfzensor.images;

import de.uni_hannover.se.pdfzensor.Logging;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
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
	 */
	public ImageReplacer() {
		// preparing PDFStreamEngine
		addOperator(new Concatenate());
		addOperator(new DrawObject());
		addOperator(new SetGraphicsStateParameters());
		addOperator(new Save());
		addOperator(new Restore());
		addOperator(new SetMatrix());
		addOperator(new Concatenate());
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
		
		var pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.PREPEND, true);
		pageContentStream.saveGraphicsState();
		pageContentStream.close();
		
		pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
		pageContentStream.restoreGraphicsState();
		pageContentStream.setStrokingColor(Color.DARK_GRAY);
		pageContentStream.setLineWidth(2);
		drawPictureCensorBox(pageContentStream);
		pageContentStream.close();
		
		removeImageData(page);
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
				Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
				LOGGER.info("Image [{}]", objectName.getName());
				LOGGER.info("Position in PDF = \"{}\", \"{}\" in user space units", ctmNew.getTranslateX(),
							ctmNew.getTranslateY());
				LOGGER.info("Displayed size  = \"{}\", \"{}\" in user space units", ctmNew.getScalingFactorX(),
							ctmNew.getScalingFactorY());
				rects.add(new Rectangle2D.Float(ctmNew.getTranslateX(), ctmNew.getTranslateY(),
												ctmNew.getScalingFactorX(),
												ctmNew.getScalingFactorY()));
			} else if (xobject instanceof PDFormXObject) {
				PDFormXObject form = (PDFormXObject) xobject;
				Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
				LOGGER.info("Image [{}]", objectName.getName());
				LOGGER.info("Position in PDF = \"{}\", \"{}\" in user space units", ctmNew.getTranslateX(),
							ctmNew.getTranslateY());
				LOGGER.info("Displayed size  = \"{}\", \"{}\" in user space units", ctmNew.getScalingFactorX(),
							ctmNew.getScalingFactorY());
				var bounds = form.getBBox();
				rects.add(new Rectangle2D.Float(
						ctmNew.getTranslateX() + bounds.getLowerLeftX() * ctmNew.getScalingFactorX(),
						ctmNew.getTranslateY() + bounds.getLowerLeftY() * ctmNew.getScalingFactorY(),
						ctmNew.getScalingFactorX() * bounds.getWidth(),
						ctmNew.getScalingFactorY() * bounds.getHeight()));
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
	private void drawPictureCensorBox(PDPageContentStream pageContentStream) throws IOException {
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
	
	/**
	 * Strips the data of the image resources from the provided page. We define all PDXObjects to be "image resources".
	 *
	 * @param page the page to remove all image resources from.
	 */
	private void removeImageData(@NotNull PDPage page) {
		var resources = Objects.requireNonNull(page).getResources();
		resources.getCOSObject().setItem(COSName.XOBJECT, null);
	}
	
}

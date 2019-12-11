package de.uni_hannover.se.pdfzensor.images;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Utility-class that removes Images ({@link PDImageXObject}) from a given {@link PDDocument}.
 */
public final class ImageRemover {
	/**
	 * Private constructor of a utility-class that is not supposed to be called.
	 * @throws OperationNotSupportedException when called.
	 */
	private ImageRemover() throws OperationNotSupportedException {
		throw new OperationNotSupportedException("");
	}

	/**
	 * Static function that will remove the Images {@link PDImageXObject} from the given {@link PDDocument}.
	 * @param doc {@link PDDocument} that will have its Images {@link PDImageXObject} removed.
	 * @throws IOException
	 */
	static public void remove(PDDocument doc) throws IOException {
		var myListMap = new ArrayList<AbstractMap.SimpleEntry<Integer, org.apache.pdfbox.cos.COSName>>();
		for (var i = 0; i < doc.getNumberOfPages(); ++i) {
			for (var name : doc.getPage(i).getResources().getXObjectNames()) {
				var xObject = doc.getPage(i).getResources().getXObject(name);
				if (xObject instanceof PDImageXObject)
					myListMap.add(new AbstractMap.SimpleEntry<>(i, name));
			}
		}
		for (var item : myListMap)
			doc.getPage(item.getKey()).getResources().put(item.getValue(), (PDXObject) null);
	}
}

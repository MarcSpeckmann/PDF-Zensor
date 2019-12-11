package de.uni_hannover.se.pdfzensor.images;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * TODO add javaDoc
 */
public final class ImageRemover {
	static void remove(PDDocument doc) throws IOException {
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

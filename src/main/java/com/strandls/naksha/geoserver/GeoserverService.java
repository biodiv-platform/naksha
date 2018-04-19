package com.strandls.naksha.geoserver;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.strandls.naksha.geoserver.models.GeoserverLayerStyles;

public class GeoserverService {

	private GeoserverService() {
		
	}

	public static List<GeoserverLayerStyles> getLayerStyles(Document doc) {
		List<GeoserverLayerStyles> styles = new ArrayList<>();
		if(doc == null)
			return styles;

		NodeList nList = doc.getDocumentElement().getElementsByTagName("sld:UserStyle");
		if(nList == null)
			return styles;
		
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String styleName = eElement.getElementsByTagName("sld:Name").item(0).getTextContent();
				String styleTitle = eElement.getElementsByTagName("sld:Title").item(0).getTextContent();
				
				styles.add(new GeoserverLayerStyles(styleName, styleTitle));
			}
		}

		return styles;
	}
}

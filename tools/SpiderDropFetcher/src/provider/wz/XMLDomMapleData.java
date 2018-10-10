/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package provider.wz;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import provider.MapleData;
import provider.MapleDataEntity;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLDomMapleData implements MapleData {
	private Node node;
	private File imageDataDir;
        private final static NumberFormat nfParser = NumberFormat.getInstance(Locale.FRANCE);

	public XMLDomMapleData(FileInputStream fis, File imageDataDir) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(fis);
			this.node = document.getFirstChild();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.imageDataDir = imageDataDir;
	}

	private XMLDomMapleData(Node node) {
		this.node = node;
	}

	@Override
	public synchronized MapleData getChildByPath(String path) {  // the whole XML reading system seems susceptible to give nulls on strenuous read scenarios
		String segments[] = path.split("/");
		if (segments[0].equals("..")) {
			return ((MapleData) getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
		}

                Node myNode;
                myNode = node;
                for (String s : segments) {
                        NodeList childNodes = myNode.getChildNodes();
                        boolean foundChild = false;
                        for (int i = 0; i < childNodes.getLength(); i++) {
                                Node childNode = childNodes.item(i);
                                if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getAttributes().getNamedItem("name").getNodeValue().equals(s)) {
                                        myNode = childNode;
                                        foundChild = true;
                                        break;
                                }
                        }
                        if (!foundChild) {
                                return null;
                        }
                }
                
		XMLDomMapleData ret = new XMLDomMapleData(myNode);
		ret.imageDataDir = new File(imageDataDir, getName() + "/" + path).getParentFile();
		return ret;
	}

	@Override
	public synchronized List<MapleData> getChildren() {
		List<MapleData> ret = new ArrayList<>();
                
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                        Node childNode = childNodes.item(i);
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                XMLDomMapleData child = new XMLDomMapleData(childNode);
                                child.imageDataDir = new File(imageDataDir, getName());
                                ret.add(child);
                        }
                }
                
		return ret;
	}

        public synchronized static Number parseNumber(String value) {
            try {
                return nfParser.parse(value);
            } catch(Exception e) {
                e.printStackTrace();
                return 0.0f;
            }
        }
        
	@Override
	public synchronized Object getData() {
                NamedNodeMap attributes = node.getAttributes();
                MapleDataType type = getType();
                switch (type) {
                        case DOUBLE:
                        case FLOAT:
                        case INT:
                        case SHORT: {
                                String value = attributes.getNamedItem("value").getNodeValue();
                                Number nval = parseNumber(value);

                                switch (type) {
                                        case DOUBLE:
                                                return nval.doubleValue();
                                        case FLOAT:
                                                return nval.floatValue();
                                        case INT:
                                                return nval.intValue();
                                        case SHORT:
                                                return nval.shortValue();
                                        default:
                                                return null;
                                }
                        }
                        case STRING:
                        case UOL: {
                                String value = attributes.getNamedItem("value").getNodeValue();
                                return value;
                        }
                        case VECTOR: {
                                String x = attributes.getNamedItem("x").getNodeValue();
                                String y = attributes.getNamedItem("y").getNodeValue();
                                return new Point(Integer.parseInt(x), Integer.parseInt(y));
                        }
                        case CANVAS: {
                                String width = attributes.getNamedItem("width").getNodeValue();
                                String height = attributes.getNamedItem("height").getNodeValue();
                                return new FileStoredPngMapleCanvas(Integer.parseInt(width), Integer.parseInt(height), new File(
                                                imageDataDir, getName() + ".png"));
                        }
                        default:
                                return null;
                }
	}

	@Override
	public synchronized MapleDataType getType() {
                String nodeName = node.getNodeName();
                
                switch (nodeName) {
                    case "imgdir":
                        return MapleDataType.PROPERTY;
                    case "canvas":
                        return MapleDataType.CANVAS;
                    case "convex":
                        return MapleDataType.CONVEX;
                    case "sound":
                        return MapleDataType.SOUND;
                    case "uol":
                        return MapleDataType.UOL;
                    case "double":
                        return MapleDataType.DOUBLE;
                    case "float":
                        return MapleDataType.FLOAT;
                    case "int":
                        return MapleDataType.INT;
                    case "short":
                        return MapleDataType.SHORT;
                    case "string":
                        return MapleDataType.STRING;
                    case "vector":
                        return MapleDataType.VECTOR;
                    case "null":
                        return MapleDataType.IMG_0x00;
                }
		return null;
	}

	@Override
	public synchronized MapleDataEntity getParent() {
                Node parentNode;
                parentNode = node.getParentNode();
                if (parentNode.getNodeType() == Node.DOCUMENT_NODE) {
                        return null;
                }
		XMLDomMapleData parentData = new XMLDomMapleData(parentNode);
		parentData.imageDataDir = imageDataDir.getParentFile();
		return parentData;
	}

	@Override
	public synchronized String getName() {
                return node.getAttributes().getNamedItem("name").getNodeValue();
	}

	@Override
	public synchronized Iterator<MapleData> iterator() {
		return getChildren().iterator();
	}
}

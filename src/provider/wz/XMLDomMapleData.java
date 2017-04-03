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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
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
        private NumberFormat nf;

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
                this.nf = NumberFormat.getInstance(Locale.FRANCE);
	}

	private XMLDomMapleData(Node node) {
		this.node = node;
                this.nf = NumberFormat.getInstance(Locale.FRANCE);
	}

	@Override
	public MapleData getChildByPath(String path) {
		String segments[] = path.split("/");
		if (segments[0].equals("..")) {
			return ((MapleData) getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
		}

		Node myNode = node;
		for (int x = 0; x < segments.length; x++) {
			NodeList childNodes = myNode.getChildNodes();
			boolean foundChild = false;
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getAttributes().getNamedItem("name").getNodeValue().equals(segments[x])) {
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
	public List<MapleData> getChildren() {
		List<MapleData> ret = new ArrayList<MapleData>();
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

	@Override
	public Object getData() {
		NamedNodeMap attributes = node.getAttributes();
		MapleDataType type = getType();
		switch (type) {
		case DOUBLE:
		case FLOAT:
		case INT:
                case SHORT: {
                        String value = attributes.getNamedItem("value").getNodeValue();
                        Number nval;
                        
                        try {
                                nval = nf.parse(value);
                        }
                        catch(java.text.ParseException pe) {
                                pe.printStackTrace();
                                nval = 0.0f;
                        }
                                
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
	public MapleDataType getType() {
		String nodeName = node.getNodeName();
		if (nodeName.equals("imgdir")) {
			return MapleDataType.PROPERTY;
		} else if (nodeName.equals("canvas")) {
			return MapleDataType.CANVAS;
		} else if (nodeName.equals("convex")) {
			return MapleDataType.CONVEX;
		} else if (nodeName.equals("sound")) {
			return MapleDataType.SOUND;
		} else if (nodeName.equals("uol")) {
			return MapleDataType.UOL;
		} else if (nodeName.equals("double")) {
			return MapleDataType.DOUBLE;
		} else if (nodeName.equals("float")) {
			return MapleDataType.FLOAT;
		} else if (nodeName.equals("int")) {
			return MapleDataType.INT;
		} else if (nodeName.equals("short")) {
			return MapleDataType.SHORT;
		} else if (nodeName.equals("string")) {
			return MapleDataType.STRING;
		} else if (nodeName.equals("vector")) {
			return MapleDataType.VECTOR;
		} else if (nodeName.equals("null")) {
			return MapleDataType.IMG_0x00;
		}
		return null;
	}

	@Override
	public MapleDataEntity getParent() {
		Node parentNode = node.getParentNode();
		if (parentNode.getNodeType() == Node.DOCUMENT_NODE) {
			return null;
		}
		XMLDomMapleData parentData = new XMLDomMapleData(parentNode);
		parentData.imageDataDir = imageDataDir.getParentFile();
		return parentData;
	}

	@Override
	public String getName() {
		return node.getAttributes().getNamedItem("name").getNodeValue();
	}

	@Override
	public Iterator<MapleData> iterator() {
		return getChildren().iterator();
	}
}

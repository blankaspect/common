/*====================================================================*\

PropertySet.java

Property set class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.UrlException;

import uk.blankaspect.common.tuple.StringKVPair;

import uk.blankaspect.common.xml.XmlFile;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// PROPERTY SET CLASS


public class PropertySet
	implements Property.ISource, Property.ITarget, XmlFile.IElementWriter
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private interface ElementName
	{
		String	PROPERTY	= "property";
	}

	private interface AttrName
	{
		String	KEY		= "key";
		String	VALUE	= "value";
		String	VERSION	= "version";
		String	XMLNS	= "xmlns";
	}

	private static final	String	PROPERTY_SET_STR	= "Property set";
	private static final	String	FILE_STR			= "file";

	private static final	int	INDENT_INCREMENT	= 2;

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		UNEXPECTED_FILE_FORMAT
		("The %1 does not have the expected format."),

		NO_ATTRIBUTE
		("The required attribute is missing.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PropertySet()
	{
		properties = new Hashtable<>();
	}

	//------------------------------------------------------------------

	public PropertySet(String rootElementName)
		throws AppException
	{
		this();
		document = XmlUtils.createDocumentBuilder(false).newDocument();
		document.appendChild(document.createElement(rootElementName));
	}

	//------------------------------------------------------------------

	public PropertySet(String rootElementName,
					   String namespaceNameStr,
					   String versionStr)
		throws AppException
	{
		this(rootElementName);
		if (namespaceNameStr != null)
			setDocumentAttribute(AttrName.XMLNS, namespaceNameStr);
		setDocumentAttribute(AttrName.VERSION, versionStr);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.ISource interface
////////////////////////////////////////////////////////////////////////

	public String getSourceName()
	{
		return PROPERTY_SET_STR;
	}

	//------------------------------------------------------------------

	public String getProperty(String key)
	{
		return get(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.ITarget interface
////////////////////////////////////////////////////////////////////////

	public boolean putProperty(String key,
							   String value)
	{
		return put(key, value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : XmlFile.IElementWriter interface
////////////////////////////////////////////////////////////////////////

	public void writeElement(XmlWriter writer,
							 Element   element,
							 int       indent)
		throws IOException
	{
		if (element.getTagName().equals(ElementName.PROPERTY))
			writer.writeEmptyElement(element.getTagName(), XmlWriter.createAttributeList(element),
									 indent, true);
		else
		{
			// Write start tag
			writer.writeElementStart(element.getTagName(), XmlWriter.createAttributeList(element),
									 indent, true, true);

			// Write child elements
			NodeList nodes = element.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				switch (node.getNodeType())
				{
					case Node.ELEMENT_NODE:
						writeElement(writer, (Element)node, indent + INDENT_INCREMENT);
						break;

					case Node.TEXT_NODE:
						writer.write(node.getNodeValue());
						break;
				}
			}

			// Write end tag
			writer.writeElementEnd(element.getTagName(), indent);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws IllegalArgumentException
	 */

	public String get(String key)
	{
		if (key == null)
			throw new IllegalArgumentException();

		return properties.get(key);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public boolean put(String key,
					   String value)
	{
		if (key == null)
			throw new IllegalArgumentException();

		boolean valueSet = false;
		if (value != null)
		{
			properties.put(key, value);
			valueSet = true;
		}
		return valueSet;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public String remove(String key)
	{
		if (key == null)
			throw new IllegalArgumentException();

		return properties.remove(key);
	}

	//------------------------------------------------------------------

	public Map<String, String> getProperties()
	{
		return Collections.unmodifiableMap(properties);
	}

	//------------------------------------------------------------------

	public List<StringKVPair> toList()
	{
		List<StringKVPair> entries = new ArrayList<>();
		for (String key : properties.keySet())
			entries.add(new StringKVPair(key, properties.get(key)));
		Collections.sort(entries);
		return entries;
	}

	//------------------------------------------------------------------

	public void clear()
	{
		properties.clear();
	}

	//------------------------------------------------------------------

	public String getDocumentAttribute(String name)
	{
		return XmlUtils.getAttribute(document.getDocumentElement(), name);
	}

	//------------------------------------------------------------------

	public String getVersionString()
	{
		return getDocumentAttribute(AttrName.VERSION);
	}

	//------------------------------------------------------------------

	public String getNamespaceName()
	{
		return getDocumentAttribute(AttrName.XMLNS);
	}

	//------------------------------------------------------------------

	public void setDocumentAttribute(String name,
									 String value)
	{
		document.getDocumentElement().setAttribute(name, value);
	}

	//------------------------------------------------------------------

	public void read(File   file,
					 String rootElementName)
		throws AppException
	{
		try
		{
			// Read document
			document = XmlFile.read(file);

			// Validate name of root element
			if (!document.getDocumentElement().getTagName().equals(rootElementName))
				throw new FileException(ErrorId.UNEXPECTED_FILE_FORMAT, file);

			// Update properties
			updateProperties();
		}
		catch (AppException e)
		{
			e.setSubstitutionStrings(getFileKindString());
			throw e;
		}
	}

	//------------------------------------------------------------------

	public void read(URL    url,
					 String rootElementName)
		throws AppException
	{
		try
		{
			// Read document
			document = XmlFile.read(url);

			// Validate name of root element
			if (!document.getDocumentElement().getTagName().equals(rootElementName))
				throw new UrlException(ErrorId.UNEXPECTED_FILE_FORMAT, url);

			// Update properties
			updateProperties();
		}
		catch (AppException e)
		{
			e.setSubstitutionStrings(getFileKindString());
			throw e;
		}
	}

	//------------------------------------------------------------------

	public void write(File file)
		throws AppException
	{
		write(file, null);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalStateException
	 */

	public void write(File   file,
					  String versionStr)
		throws AppException
	{
		// Test for document
		if (document == null)
			throw new IllegalStateException();

		// Remove elements from document
		Element documentElement = document.getDocumentElement();
		NodeList nodes = documentElement.getChildNodes();
		for (int i = nodes.getLength() - 1; i >= 0; i--)
		{
			Node node = nodes.item(i);
			if ((node.getNodeType() != Node.ATTRIBUTE_NODE) ||
				 !node.getNodeName().equals(AttrName.VERSION))
				documentElement.removeChild(node);
		}

		// Add version attribute to document element
		if (versionStr != null)
			documentElement.setAttribute(AttrName.VERSION, versionStr);

		// Add property elements to document
		for (StringKVPair entry : toList())
		{
			Element element = document.createElement(ElementName.PROPERTY);
			element.setAttribute(AttrName.KEY, entry.getKey());
			element.setAttribute(AttrName.VALUE, entry.getValue());
			documentElement.appendChild(element);
		}

		// Write file
		try
		{
			XmlFile.write(file, document, XmlWriter.Standalone.NO, this);
		}
		catch (AppException e)
		{
			e.setSubstitutionStrings(getFileKindString());
			throw e;
		}
	}

	//------------------------------------------------------------------

	protected String getFileKindString()
	{
		return FILE_STR;
	}

	//------------------------------------------------------------------

	private void updateProperties()
		throws XmlParseException
	{
		properties.clear();
		NodeList nodes = document.getDocumentElement().getElementsByTagName(ElementName.PROPERTY);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Element element = (Element)nodes.item(i);
			String elementPath = XmlUtils.getElementPath(element);

			// Attribute: key
			String attrName = AttrName.KEY;
			String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			String attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			String key = attrValue;

			// Attribute: value
			attrName = AttrName.VALUE;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			String value = attrValue;

			properties.put(key, value);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	protected	Hashtable<String, String>	properties;
	protected	Document					document;

}

//----------------------------------------------------------------------

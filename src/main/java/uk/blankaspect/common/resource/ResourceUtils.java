/*====================================================================*\

ResourceUtils.java

Class: resource-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.resource;

//----------------------------------------------------------------------


// IMPORTS


import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import uk.blankaspect.common.dataio.ByteDataList;

//----------------------------------------------------------------------


// CLASS: RESOURCE-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to resources.
 */

public class ResourceUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Miscellaneous strings. */
	private static final	String	FILE_NOT_FOUND_STR	= "File was not found";
	private static final	String	FILE_TOO_LONG_STR	= "File is too long";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private ResourceUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the path of the package that contains the specified class.  The path formed from the name of the package
	 * by replacing each occurrence of '.' with '/'.
	 *
	 * @param  cls
	 *           the class for whose package the path is required.
	 * @return the path of the package that contains <i>cls</i>.
	 */

	public static String getPackagePath(Class<?> cls)
	{
		return cls.getPackage().getName().replace('.', '/');
	}

	//------------------------------------------------------------------

	/**
	 * Returns the normalised absolute name of the resource of the specified name.  If the specified name is not
	 * absolute, it is resolved against the package that contains the specified class.
	 *
	 * @param  cls
	 *           a class against whose package <i>name</i> will be resolved; ignored if <i>name</i> starts with '/'.
	 * @param  name
	 *           the name of the resource for which the absolute name is required.  If <i>name</i> starts with '/', it
	 *           is deemed to be absolute and <i>cls</i> is ignored.
	 * @return the normalised absolute name of the resource whose name is <i>name</i>, resolved against the package that
	 *         contains <i>cls</i>, if <i>name</i> is not absolute.
	 */

	public static String absoluteName(Class<?> cls,
									  String   name)
	{
		// Create absolute path
		String path = name.startsWith("/") ? name : "/" + getPackagePath(cls) + "/" + name;

		// Split path into its components
		String[] inComponents = path.split("/", -1);

		// Initialise list of components of normalised path
		List<String> outComponents = new ArrayList<>();

		// Normalise path
		for (String component : inComponents)
		{
			if (component.equals("."))
				continue;
			if (component.equals(".."))
			{
				if (!outComponents.isEmpty())
					outComponents.remove(outComponents.size() - 1);
			}
			else
				outComponents.add(component);
		}

		// Concatenate components of normalised path and return result.
		return outComponents.stream().collect(Collectors.joining("/"));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the location of the resource with the specified name.
	 *
	 * @param  cls
	 *           the class with which the resource is associated.  If this is {@code null}, the system class loader will
	 *           be used to find the resource.
	 * @param  name
	 *           the name of the required resource.
	 * @return the location of the resource whose name is <i>name</i>, or {@code null} if no such resource was found.
	 */

	public static URL getResource(Class<?> cls,
								  String   name)
	{
		return (cls == null) ? ClassLoader.getSystemResource(name) : cls.getResource(name);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if there is a resource with the specified name.
	 *
	 * @param  cls
	 *           the class with which the resource is associated.  If this is {@code null}, the system class loader will
	 *           be used to find the resource.
	 * @param  name
	 *           the name of the resource of interest.
	 * @return {@code true} if there is a resource whose name is <i>name</i>.
	 */

	public static boolean hasResource(Class<?> cls,
									  String   name)
	{
		return (getResource(cls, name) != null);
	}

	//------------------------------------------------------------------

	/**
	 * Reads the resource with the specified name and returns it as a byte array.
	 *
	 * @param  cls
	 *           the class with which the resource is associated.  If this is {@code null}, the system class loader will
	 *           be used to open the resource.
	 * @param  name
	 *           the name of the required resource.
	 * @return a byte array of the contents of the resource.
	 * @throws IOException
	 *           if an error occurred when reading the resource.
	 */

	public static byte[] readResource(Class<?> cls,
									  String   name)
		throws IOException
	{
		// Open input stream on resource
		InputStream inStream = (cls == null) ? ClassLoader.getSystemResourceAsStream(name)
											 : cls.getResourceAsStream(name);
		if (inStream == null)
			throw new IOException(FILE_NOT_FOUND_STR + ": " + name);

		// Read data from stream
		ByteDataList byteData = new ByteDataList();
		while (true)
		{
			// Allocate buffer for block
			byte[] buffer = new byte[4096];

			// Read block
			int blockLength = inStream.read(buffer);

			// Test for end of input stream
			if (blockLength < 0)
				break;

			// Add block to list
			if (blockLength > 0)
				byteData.add(buffer, 0, blockLength);

			// Test cumulative length of data
			if (byteData.getLength() > Integer.MAX_VALUE)
				throw new IOException(FILE_TOO_LONG_STR + ": " + name);
		}

		// Close input stream
		inStream.close();

		// Concatenate data
		byte[] data = new byte[(int)byteData.getLength()];
		byteData.getData(data);

		// Return data
		return data;
	}

	//------------------------------------------------------------------

	/**
	 * Reads the resource with the specified name and returns it as a string that is decoded from the contents of the
	 * resource with the specified character encoding.
	 *
	 * @param  cls
	 *           the class with which the resource is associated.  If this is {@code null}, the system class loader will
	 *           be used to open the resource.
	 * @param  name
	 *           the name of the required resource.
	 * @return encoding
	 *           the character encoding that will be used to decode the contents of the resource.
	 * @throws IOException
	 *           if an error occurred when reading the resource.
	 */

	public static String readTextResource(Class<?> cls,
										  String   name,
										  Charset  encoding)
		throws IOException
	{
		return new String(readResource(cls, name), encoding);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------

/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.agent.web.resources;

import de.betterform.agent.web.resources.stream.DefaultResourceStreamer;
import de.betterform.agent.web.resources.stream.ResourceStreamer;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * ResourceServlet is responsible for streaming resources like css, script, images and etc to the client. Streaming is done via
 * ResourceStreamers and resources are forced to be cached indefinitely using convenient response headers.
 */
public class ResourceServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(ResourceServlet.class);
	private static Map<String, String> mimeTypes;
	private List<ResourceStreamer> resourceStreamers;
	private boolean caching;
	private boolean exploded = false;
	/**
	 * RESOURCE_FOLDER refers to the location in the classpath where resources are found.
	 */
	public final static String RESOURCE_FOLDER = "/META-INF/resources";

	/**
	 * RESOURCE_PATTERN is the string used in URLs requesting resources. This value is hardcoded for now - meaning that all
	 * requests to internal betterFORM resources like CSS, images, scripts and XSLTs have to use 'bfResources'.
	 * 
	 * Example: http://somehost.com/betterform/bfResources/images/image.gif" will try to load an image 'image.gif' from
	 * /META-INF/resources/images/image.gif
	 * 
	 */
	public final static String RESOURCE_PATTERN = "bfResources";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if ("false".equals(config.getInitParameter("caching"))) {
			caching = false;
			if (LOG.isTraceEnabled()) {
				LOG.trace("Caching of Resources is disabled");
			}
		} else {
			caching = true;
			if (LOG.isTraceEnabled()) {
				LOG.trace("Caching of Resources is enabled - resources are loaded from classpath");
			}
		}

		if (new File(config.getServletContext().getRealPath("WEB-INF/classes/META-INF/resources")).exists()) {
			exploded = true;
		}

		initMimeTypes();
		initResourceStreamers();
	}

	private void initMimeTypes() {
		mimeTypes = new HashMap<String, String>();
		mimeTypes.put("css", "text/css");
		mimeTypes.put("js", "text/javascript");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("jpeg", "image/jpeg");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("swf", "application/x-shockwave-flash");
	}

	private void initResourceStreamers() {
		resourceStreamers = new ArrayList<ResourceStreamer>();
		resourceStreamers.add(new DefaultResourceStreamer());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestUri = req.getRequestURI();
		String resourcePath = RESOURCE_FOLDER + getResourcePath(requestUri);
		URL url = ResourceServlet.class.getResource(resourcePath);
		if (LOG.isTraceEnabled()) {
			LOG.trace("Request URI: " + requestUri);
			LOG.trace("resource fpath: " + resourcePath);
		}

		if (url == null) {
			boolean error = true;

			if (requestUri.endsWith(".js")) {
				// try optimized version first
				if (requestUri.contains("scripts/betterform/betterform-")) {
					if (ResourceServlet.class.getResource(resourcePath) == null) {
						resourcePath = resourcePath.replace("betterform-", "BfRequired");
						if (ResourceServlet.class.getResource(resourcePath) != null) {
							error = false;
						}
					}
				}
			}

			if (error) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(MessageFormat.format("Resource \"{0}\" not found - ", resourcePath));
				}
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(MessageFormat.format("Streaming resource \"{0}\"", resourcePath));
		}

		InputStream inputStream = null;

		try {
			//final URL resource = ResourceServlet.class.getResource(resourcePath);
			final URLConnection urlConnection = url.openConnection();
			// This already opens the inputstream afaik
			final long lastModified = urlConnection.getLastModified();

			final long ifModifiedSince = req.getDateHeader("If-Modified-Since");

			// if(exploded){

			String path = url.getPath();
			inputStream = urlConnection.getInputStream();// new FileInputStream(new File(path));
			if (LOG.isTraceEnabled()) {
				LOG.trace("loading reources form file: " + path + " Last-Modified: " + lastModified + " if-modified-since" );
			}
			if (lastModified <= ifModifiedSince) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("   - not modified");
				}
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			// }else{
			// inputStream = ResourceServlet.class.getResourceAsStream(resourcePath);
			// }
			String mimeType = getResourceContentType(resourcePath);

			if (mimeType == null) {
				if (LOG.isTraceEnabled()) {
					LOG.trace(MessageFormat.format("MimeType for \"{0}\" not found. Sending 'not found' response - ",
							resourcePath));
				}
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			resp.setContentType(mimeType);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLength(urlConnection.getContentLength());
			setCaching(req, resp, lastModified);
			streamResource(req, resp, mimeType, inputStream);

			if (LOG.isTraceEnabled()) {
				LOG.trace(MessageFormat.format("Resource \"{0}\" streamed succesfully - ", resourcePath));
			}
		} catch (Exception exception) {
			LOG.error(MessageFormat.format("Error in streaming resource \"{0}\". Exception is \"{1}\" - ", new Object[] {
					resourcePath, exception.getMessage() }));
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}

			resp.getOutputStream().flush();
			resp.getOutputStream().close();
		}
	}

	private void streamResource(HttpServletRequest req, HttpServletResponse resp, String mimeType, InputStream inputStream)
			throws IOException {
		for (ResourceStreamer streamer : resourceStreamers) {
			if (streamer.isAppropriateStreamer(mimeType))
				streamer.stream(req, resp, inputStream);
		}
	}

	/**
	 * set the caching headers for the resource response. Caching can be disabled by adding and init-param of 'caching' with
	 * value 'false' to web.xml
	 * 
	 * @param request
	 *            the http servlet request
	 * @param response
	 *            the http servlet response
	 */
	protected void setCaching(HttpServletRequest request, HttpServletResponse response, long lastModified) {
		long now = System.currentTimeMillis();
		long oneYear = 31363200000L;

		if (caching) {
			response.setHeader("Cache-Control", "Public");
			response.setDateHeader("Expires", now + oneYear);
			response.setDateHeader("Last-Modified", lastModified);
		} else {
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		}
	}

	protected String getResourcePath(String requestURI) {
		int jsessionidIndex = requestURI.toLowerCase().indexOf(";jsessionid");
		if (jsessionidIndex != -1) {
			requestURI = requestURI.substring(0, jsessionidIndex);
		}

		int patternIndex = requestURI.indexOf(RESOURCE_PATTERN);
		return requestURI.substring(patternIndex + RESOURCE_PATTERN.length(), requestURI.length());

	}

	protected String getResourceContentType(String resourcePath) {
		String resourceFileExtension = getResourceFileExtension(resourcePath);

		return mimeTypes.get(resourceFileExtension);
	}

	protected String getResourceFileExtension(String resourcePath) {
		String parsed[] = resourcePath.split("\\.");

		return parsed[parsed.length - 1];
	}
}

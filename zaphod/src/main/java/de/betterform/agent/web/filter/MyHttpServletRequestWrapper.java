package de.betterform.agent.web.filter;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;


public class MyHttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

	HttpServletRequest request;

	public MyHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		this.request = request;

		Enumeration en = request.getHeaderNames();
		while(en.hasMoreElements() ) {
			String name = (String)en.nextElement();
			System.out.println(name + "=" + request.getHeader(name));
		}
	}

	public String getMethod() {
		if (request.getMethod().equalsIgnoreCase("POST") ) {
			return "GET";
		}
		return request.getMethod();
	}

}

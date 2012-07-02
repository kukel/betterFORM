/*
 * Copyright (c) 2011. betterForm Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */
package de.betterform.xml.xforms.model.constraints;

import de.betterform.xml.xforms.BetterFormTestCase;
import de.betterform.xml.xforms.exception.XFormsException;
import de.betterform.xml.xforms.model.Model;
import de.betterform.xml.xforms.xpath.saxon.function.XPathFunctionContext;

/**
 * Unit tests for custom mips.
 *
 * @author Ronald van Kuijk

 * @version $Id:  $
 */
public class CustomMIPTest extends BetterFormTestCase {
//    static {
//        org.apache.log4j.xml.DOMConfigurator.configure("/Users/uli/Development/IdeaProjects/betterform-sandbox/build/log4j.xml");
//    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDiff() throws XFormsException {
        Model model = getDefaultModel();
        
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/deliveryDate")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n");
        
        model.getDefaultInstance().setNodeValue(evaluateInDefaultContextAsNode("/data/deliveryDate"), "2012-12-12");
        model.recalculate();
        model.revalidate();
        model.refresh();
        assertEquals("true", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/deliveryDate")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));
        
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n");
        
        
        
        // Still different, no event?
        model.getDefaultInstance().setNodeValue(evaluateInDefaultContextAsNode("/data/deliveryDate"), "2012-12-13");
        model.recalculate();
        model.revalidate();
        model.refresh();
        assertEquals("true", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/deliveryDate")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));

        
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n");
        
        // Identical again, so event
        model.getDefaultInstance().setNodeValue(evaluateInDefaultContextAsNode("/data/deliveryDate"), "2012-12-24");
        model.recalculate();
        model.revalidate();
        model.refresh();
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/deliveryDate")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n");
        
    }
    
    /**
     * Tests constraints with a predicate.
     *
     * @throws XFormsException if any error occurred during the test.
     */
    public void testDiffWithPredicate() throws XFormsException {
        Model model = getDefaultModel();
        model.refresh();
        
        // both are identical, so diff equals false
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[1]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));

        assertEquals("alert", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[1]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfStatus"));
        assertEquals("warning", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfStatus"));

        
        // setting one of them to a different value so it becomes different and thus 'diff' becomes true
        model.getDefaultInstance().setNodeValue(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount"), "789");
        
        model.recalculate();
        model.revalidate();
        
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[1]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));
        assertEquals("true", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));

        assertEquals("alert", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[1]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfStatus"));
        assertEquals("ok", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfStatus"));

        
        // Making it identical again
        model.getDefaultInstance().setNodeValue(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount"), "456");
        
        model.recalculate();
        model.revalidate();
        
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[1]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));
        assertEquals("false", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfDiff"));

        assertEquals("alert", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[1]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfStatus"));
        assertEquals("warning", model.getDefaultInstance().getModelItem(evaluateInDefaultContextAsNode("/data/repeated/item[2]/amount")).getLocalUpdateView().getCustomMIPValues().get("bfStatus"));

        
    }

    protected String getTestCaseURI() {
        return "CustomMIPTest.xhtml";
    }

    protected XPathFunctionContext getDefaultFunctionContext() {
        return null;
    }
}

/* Copyright 2008 - Joern Turner, Lars Windauer */

package de.betterform.xml.xforms.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.betterform.connector.ConnectorFactory;
import de.betterform.connector.URIResolver;
import de.betterform.xml.dom.DOMUtil;
import de.betterform.xml.events.BetterFormEventNames;
import de.betterform.xml.events.XFormsEventNames;
import de.betterform.xml.ns.NamespaceConstants;
import de.betterform.xml.ns.NamespaceResolver;
import de.betterform.xml.xforms.Initializer;
import de.betterform.xml.xforms.XFormsConstants;
import de.betterform.xml.xforms.XFormsProcessor;
import de.betterform.xml.xforms.exception.XFormsException;
import de.betterform.xml.xforms.model.Instance;
import de.betterform.xml.xforms.model.Model;
import de.betterform.xml.xforms.model.submission.AttributeOrValueChild;
import de.betterform.xml.xpath.impl.saxon.XPathUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.util.HashMap;


/**
 * Implements the action as defined in <code>10.1.8 The load Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;, Nick van den Bleeken, Joern Turner, Lars Windauer
 * @version $Id: LoadAction.java 3477 2008-08-19 09:26:47Z joern $
 */
public class LoadAction extends AbstractBoundAction {
    private static final Log LOGGER = LogFactory.getLog(LoadAction.class);
    private AttributeOrValueChild resource;
    private String showAttribute = null;
    private String targetAttribute = null;

    /**
     * Creates a load action implementation.
     *
     * @param element the element.
     * @param model the context model.
     */
    public LoadAction(Element element, Model model) {
        super(element, model);
    }

    // lifecycle methods

    /**
     * Performs element init.
     */
    public void init() throws XFormsException {
        super.init();

        this.resource= new AttributeOrValueChild(this.element, this.model, RESOURCE_ATTRIBUTE);
        this.resource.init();

        this.showAttribute = getXFormsAttribute(SHOW_ATTRIBUTE);
        if (this.showAttribute == null) {
            // default
            this.showAttribute = "replace";
        }

        this.targetAttribute = getXFormsAttribute(TARGETID_ATTRIBUTE);

    }

    // implementation of 'de.betterform.xml.xforms.action.XFormsAction'

    /**
     * Performs the <code>load</code> action.
     *
     * @throws XFormsException if an error occurred during <code>load</code>
     * processing.
     */
    public void perform() throws XFormsException {
        String bindAttribute = getXFormsAttribute(BIND_ATTRIBUTE);
        String refAttribute = getXFormsAttribute(REF_ATTRIBUTE);
        if (this.resource.isAvailable() && (bindAttribute != null || refAttribute != null)) {
            // issue warning
            getLogger().warn(this + " perform: since both binding and linking attributes are present this action has no effect");
            return;
        }

        // obtain relative URI
        String relativeURI;
        String absoluteURI = null;
        try {
            //todo: needs refactoring
            updateXPathContext();
            if("none".equals(this.showAttribute)){
              //todo: dirty dirty dirty
                String evaluatedTarget =evalAttributeValueTemplates(this.targetAttribute,this.element);
                Element targetElem = this.container.getElementById(evaluatedTarget);
                destroyembeddedModels(targetElem);
                DOMUtil.removeAllChildren(targetElem);
                HashMap map = new HashMap();
              
                map.put("show", this.showAttribute);
                map.put("xlinkTarget", evaluatedTarget);

                this.container.dispatch(this.target, BetterFormEventNames.LOAD_URI, map);
                return;
            }


            if (this.resource.isAvailable()) {
                relativeURI = this.resource.getValue();
            }
            else {

                if (this.nodeset ==null || this.nodeset.size() == 0) {
                    getLogger().warn(this + " perform: nodeset '" + getLocationPath() + "' is empty");
                    return;
                }

                final Instance instance = this.model.getInstance(getInstanceId());
                relativeURI = instance.getNodeValue(XPathUtil.getAsNode(this.nodeset, 1));
            }

            // resolve uri
            absoluteURI = this.container.getConnectorFactory().getAbsoluteURI(relativeURI, this.element).toString();

            HashMap map = new HashMap();
            handleEmbedding(absoluteURI, map);

        } catch (XFormsException e) {
            LOGGER.error("Error performing xf:load action at: " + DOMUtil.getCanonicalPath(this.getElement()));
            throw new XFormsException("Error performing xf:load action at: " + DOMUtil.getCanonicalPath(this.getElement()), e);
        }

        // backwards compat
//        storeInContext(absoluteURI, this.showAttribute);
    }

    private void handleEmbedding(String absoluteURI, HashMap map) throws XFormsException {

//        if(this.targetAttribute == null) return;
        //todo: handle multiple params
        if(absoluteURI.indexOf("?") > 0){
            String name = absoluteURI.substring(absoluteURI.indexOf("?")+1);
            String value = name.substring(name.indexOf("=")+1,name.length());
            name = name.substring(0,name.indexOf("="));
            this.container.getProcessor().setContextParam(name,value);
        }
        if(this.targetAttribute == null){
            map.put("uri", absoluteURI);
            map.put("show", this.showAttribute);

            // dispatch internal betterform event
            this.container.dispatch(this.target, BetterFormEventNames.LOAD_URI, map);
            return;
        }


        if(!("embed".equals(this.showAttribute))){
            throw new XFormsException("@show must be 'embed' if @target is given but was: '" + this.showAttribute + "' on Element "  + DOMUtil.getCanonicalPath(this.element));
        }
        //todo: dirty dirty dirty
        String evaluatedTarget =evalAttributeValueTemplates(this.targetAttribute,this.element);

        //load resource from absoluteURI
        ConnectorFactory factory = ConnectorFactory.getFactory();
        factory.setContext(this.container.getProcessor().getContext());
        URIResolver uriResolver = factory.createURIResolver(absoluteURI,this.element);

        Node embed = (Node) uriResolver.resolve();
        if(embed instanceof Document){
            embed = ((Document)embed).getDocumentElement();
        }

        if(embed == null){
            //todo: review: context info params containing a '-' fail during access in javascript!
            //todo: the following property should have been 'resource-uri' according to spec
            map.put("resourceUri",absoluteURI);
            this.container.dispatch(this.target, XFormsEventNames.LINK_EXCEPTION, map);
            return;
        }
        
        Node embeddedNode;
        Element targetElem;
        // ##### try to interpret the targetAttribute value as an idref ##### -->
        targetElem = this.container.getElementById(evaluatedTarget);

        // destroy existing embedded form within targetNode
        if(targetElem.hasChildNodes()){
            destroyembeddedModels(targetElem);
            Initializer.disposeUIElements(targetElem);
        }

        // import referenced embedded form into host document
        embeddedNode = this.container.getDocument().importNode(embed,true);

        //import namespaces
        NamespaceResolver.applyNamespaces(targetElem.getOwnerDocument().getDocumentElement(), (Element) embeddedNode);

        // keep original targetElem id within hostdoc
        ((Element)embeddedNode).setAttributeNS(null,"id", targetElem.getAttributeNS(null,"id"));
        targetElem.getParentNode().replaceChild(embeddedNode,targetElem);


        map.put("uri", absoluteURI);
        map.put("show", this.showAttribute);
        map.put("targetElement", embeddedNode);
        map.put("xlinkTarget", evaluatedTarget);

        this.container.dispatch((EventTarget) embeddedNode, BetterFormEventNames.EMBED, map);
        //create model for it
        this.container.createEmbeddedForm((Element) embeddedNode);

        // dispatch internal betterform event
        this.container.dispatch((EventTarget) embeddedNode, BetterFormEventNames.EMBED_DONE, map);


        this.container.dispatch(this.target, BetterFormEventNames.LOAD_URI, map);
//        storeInContext(absoluteURI, this.showAttribute);
    }

    private void destroyembeddedModels(Element targetElem) throws XFormsException {
        NodeList childNodes = targetElem.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elementImpl = (Element) node;

                String name = elementImpl.getLocalName();
                String uri = elementImpl.getNamespaceURI();

                if (NamespaceConstants.XFORMS_NS.equals(uri) && name.equals(XFormsConstants.MODEL)) {
                    Model model = (Model) elementImpl.getUserData("");
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug("dispatch 'model-destruct' event to embedded model: " + model.getId());;
                    }

                    // do not dispatch model-destruct to avoid problems in lifecycle
                    // TODO: review: this.container.dispatch(model.getTarget(), XFormsEventNames.MODEL_DESTRUCT, null);
                    model.dispose();
                    this.container.removeModel(model);
                    model = null;
                }
                else {
                    destroyembeddedModels(elementImpl);
                }
            }
        }
    }


    public boolean isBound() {
        return (getBindingExpression() != null) || resource.isAvailable();
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Log getLogger() {
        return LOGGER;
    }

    /**
     * @deprecated backwards compat
     */
    private void storeInContext(String uri, String show) {
        // store in context
        this.container.getProcessor().getContext().put(XFormsProcessor.LOAD_URI, uri);
        this.container.getProcessor().getContext().put(XFormsProcessor.LOAD_TARGET, show);
    }
}

// end of class

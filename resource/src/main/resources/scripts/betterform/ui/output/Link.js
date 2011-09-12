/*
 * Copyright (c) 2011. betterForm Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

if(!dojo._hasResource["betterform.ui.output.Link"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["betterform.ui.output.Link"] = true;
dojo.provide("betterform.ui.output.Link");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");



dojo.declare(
        "betterform.ui.output.Link",
        [betterform.ui.ControlValue],
    {
	// properties and methods
		// settings
        href: "",
        label:null,
		
        templateString:"<span><a href=\"javascript:void(0)\" value=\"${href}\"  dojoAttachPoint=\"containerNode\"></a></span>",

        postMixInProperties:function() {
            this.inherited(arguments);
            this.applyProperties(dijit.byId(this.xfControlId), this.srcNodeRef);
        },

        postCreate:function() {
            this.label = dojo.byId(this.xfControl.id+ "-label");
            if(this.label != undefined){
                this.containerNode.innerHTML = this.label.innerHTML;
                this.containerNode.onclick = dojo.hitch(this,this._onClick);
                this.label.innerHTML = '';
            }

        },

        _onFocus:function() {
            this.inherited(arguments);
            this.handleOnFocus();
        },

        _onBlur:function(){
            this.inherited(arguments);
            this.handleOnBlur();
        },

        _onClick:function(){
            if(!this.xfControl.isReadonly()){
                window.open(dojo.attr(this.containerNode,"value"), '_blank');
            }
        },

        getControlValue:function(){
            return this.containerNode.innerHTML;
        },

        applyState:function(){
            /* overwritten with no content because outputs are allways readonly */
        },
        
        _handleSetControlValue:function(value) {
            //console.debug("Link._handleSetControlValue: Value: ", value);
            dojo.attr(this.containerNode,"href", value);
            if(value.indexOf("/") > 0){
                value = value.substring(value.lastIndexOf("/")+1,value.length);
            }
            if(value.indexOf(".") > 0){
                value = value.substring(0,value.lastIndexOf("."));
            }
            this.containerNode.innerHTML = value


        },

        _setLabel:function(value) {
            if(this.containerNode.innerHTML == ""){
                var textNode = document.createTextNode(value);
                dojo.place(textNode, this.containerNode);
            }
            else {
                this.containerNode.innerHTML = value;
            }
            // console.debug("this.containerNode", this.containerNode);

        }
	}
);
}

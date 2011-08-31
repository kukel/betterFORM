/*
 * Copyright (c) 2010. betterForm Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

dojo.provide("betterform.ui.select1.RadioGroup");

dojo.require("dijit._Widget");


/**
	All Rights Reserved.
	@author Joern Turner
	@author Lars Windauer

    RadioGroup "Value Dijit" represents a Select1 with appearance="full"
        Controller class for RadioButtons (betterform.util.select1.RadioButton) within Select1 Control

**/

dojo.declare(
        "betterform.ui.select1.RadioGroup",
         betterform.ui.ControlValue,
{
    
    buildRendering:function() {
        this.domNode = this.srcNodeRef;
    },

    postMixInProperties:function() {
        this.inherited(arguments);
        this.applyProperties(dijit.byId(this.xfControlId), this.srcNodeRef);
        if(dojo.attr(this.srcNodeRef, "incremental") == undefined || dojo.attr(this.srcNodeRef, "incremental") == "" || dojo.attr(this.srcNodeRef, "incremental") == "true"){
            this.incremental = true;
        }else {
            this.incremental = false;
        }
        
    },


    postCreate:function() {
        var name = this.id;
        dojo.query("*[controltype='radioButtonEntry']",this.domNode).forEach(
                function(xfNode){
                    // dojo.attr(xfNode,"name", name);
                    var optionId = dojo.attr(xfNode,"id");
                    var optionvalue = [0];
                    dojo.query(".xfRadioValue",xfNode).attr("name",name);
                    // console.dirxml(xfNode);
                    var option = new betterform.ui.Control({id:optionId,
                                                       value:dojo.attr(xfNode,"value")
                                                       },xfNode);
                    option.startup();

                }
        );
        this.inherited(arguments);
        this.setCurrentValue();
    },


    _onFocus:function() {
        this.inherited(arguments);
        this.handleOnFocus();
    },

    focus:function() {
        var itemIsFocused = dojo.query(".dijitRadioChecked",this.domNode);
        if(itemIsFocused.length > 0){
            dojo.query(".xfRadioLabel", itemIsFocused[0].parentNode)[0].focus();
        }else {
            dojo.query(".xfEnabled .xfRadioLabel",this.domNode)[0].focus();
        }
    },

    _onBlur:function() {
        this.inherited(arguments);
            this.handleOnBlur();
    },

    _setRadioGroupValue:function() {
        var option = dojo.query(".dijitRadioChecked .dijitCheckBoxInput", this.domNode)[0];
        var itemId
        if(option != undefined) {
            var tmpId = dojo.attr(option,"id");
            itemId = tmpId.substring(0,tmpId.length-6);
        }else {
            itemId = "";
        }

        //console.debug("RadioGroup._setRadioGroupValue this.incremental:" + this.incremental, " selectedItem: ", option, " itemId: ",itemId);
        fluxProcessor.dispatchEventType(this.xfControl.id, "DOMActivate", itemId);

        if(this.incremental){
            this.setControlValue();
        }
    },

    getControlValue:function(){
        // console.debug("RadioGroup.getControlValue");
        var option = dojo.query(".dijitRadioChecked .dijitCheckBoxInput", this.domNode)[0];
        if(option != undefined){
            return dijit.byId(dojo.attr(option,"id")).getControlValue();
        }else {
            return "";
        }
    },


    /* update UI control value after value change by processor */
    _handleSetControlValue:function(value) {
        // console.debug("RadioGroup._handleSetControlValue value:",value, " this.id: ", this.id);
        var radioItems = dojo.query(".xfRadioValue", this.domNode);
        for(i=0;i<radioItems.length;i++){
            var optionDijitId = dojo.attr(radioItems[i],"widgetId");
            var optionDijit = dijit.byId(optionDijitId);
            var optionValue = optionDijit.getControlValue();
            // console.debug("RadioItems optionDijit: ",optionDijit, " optionValue:",optionValue);
            if(optionValue==value){
                // console.debug("found correct dijit: optionDijit: ",optionDijit);
                optionDijit.attr("checked", true);
            }else{
                optionDijit.attr("checked", false);                
            }            
        }
    },
    /* update UI MIP after value change by processor */
    applyState:function(){
        // console.debug("RadioGroup.applyState [id:"+this.id+" / object:",this,"] this.xfControl: ",this.xfControl);
        var isReadOnly = this.xfControl.isReadonly();
        dojo.query(".xfRadioValue", this.domNode).forEach(
            function(xfNode){
                var radioDijit = dijit.byNode(xfNode);
                // console.debug("radioDijit: ",radioDijit, " isReadOnly: ",isReadOnly);
                radioDijit.set("readOnly",isReadOnly);
            }
        );
    }
});



// Register this class autoloading
dojo.provide('betterform.ui.common._Growl');

// Include dependencies
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

// Create class declaration extending templating system
dojo.declare('betterform.ui.common._Growl', [dijit._Widget, dijit._Templated], {

    // Convention defining HTML template resource
    templatePath: dojo.moduleUrl('betterform.ui.common.templates', '_Growl.html'),

    // Custom template variable
    message: null,

    // Custom template variable
    controlLabel: null,

    control: null,

    position: "top",
    stack: "btm",

    // Length of duration in milliseconds
    timeout: 8000,

    _initialHeight: undefined,
    _initialBorderColor: undefined,
    _messageNode: undefined,

    // Private DOM object to wrap all notifications
    _container: null,

    // Timer that we'll reset if the user interacts with the notification
    _timer: null,
    
    _showing: false,

    // Pin notification container to top-right of window.
    // These aren't in CSS as the _container object is internal as well.
    containerStylesTopRight: {
        'position': 'fixed',
        'top': 0,
        'right': 0,
        'zIndex': 1000,
        'cursor': 'pointer'
    },

    containerStylesBottomRight: {
        'position': 'fixed',
        'bottom': 0,
        'right': 0,
        'zIndex': 1000,
        'cursor': 'pointer'
    },

    // Before Dojo begins templatizing the HTML, we setup the container
    constructor: function(params) {
        // Combine passed parameters with default params before creating template
        dojo.mixin(this, params);
        
        this.createWrapper();
	console.debug("Creating growl for " + params); 
    },

    // If container is not already setup, we create one and add it to the DOM.
    createWrapper: function() {
        if (dojo.byId('GrowlContainer')) {
            this.container = dojo.byId('GrowlContainer');
        } else {
            this.container = dojo.doc.createElement('div');
            
            dojo.attr(this.container, 'id', 'GrowlContainer');
	    if (this.position == "btm") {
                dojo.style(this.container, this.containerStylesBottomRight);
	    } else {
                dojo.style(this.container, this.containerStylesTopRight);
	    }
            
            dojo.place(this.container, dojo.body());
        }
    },

    // 'postCreate' is called after Dojo instantiates the template as 'domNode'. We're
    // responsible for inserting it into the DOM.
    postCreate: function() {
    },

    show: function() {
        // Fade in the notification nicely
        if (this._showing == false) { // if already being displayed, ignore (prevent 'jumping'
    	// Computed style is per left/right/top-bottom for each of the border parts (color, width etc)

            
            msgNode = dojo.query(".growlMessage", this.domNode)[0];
            var closeNode = dojo.query(".close", msgNode)[0];
            this._showing = true;
        
            if (this.stack == "btm") {
                dojo.place(this.domNode, this.container);
            } else {
                dojo.place(this.domNode, this.container, "first");
            }
            if (this.initialHeight == undefined) {
                this.initialBorderColor = dojo.style(msgNode, 'borderTopColor');
                this.initialHeight = dojo.style(this.domNode, 'height');
            }
            dojo.style(this.domNode, 'display', 'block');
            dojo.style(msgNode, {"borderColor": "red"});
            dojo.style(this.domNode, 'height', 0);
            dojo.anim(this.domNode, { height: this.initialHeight}, 250);
            dojo.anim(this.domNode, { opacity: 1}, 250);
	    if (closeNode.offsetTop == '26') { // IE7 Hack to move the close buttom 21 px up... 
                // console.debug("closeNode: "+ closeNode.offsetTop);
		dojo.style(closeNode, 'top', '-21px');
            }

            this.setTimeout();

            dojo.anim(msgNode, { borderColor: this.initialBorderColor}, 1000, null,dojo.hitch(this, 'allowHover'));
	}
    },

    // Call 'close' method after specified amount of time
    setTimeout: function() {
        this.timer = setTimeout(dojo.hitch(this, 'close'), this.timeout);
    },

    // "hover" template hook to stop the timer and allow styling
    _hoverOver: function() {
        clearInterval(this.timer);
        
        dojo.addClass(msgNode, 'hover');
    },

    // "hover out" template hook to start the timer and remove "hover" styling
    _hoverOut: function() {
        this.setTimeout();
        
        dojo.removeClass(msgNode, 'hover');
    },

    _focusControl: function() {
        if(this.control != undefined && this.control.controlValue != undefined) {
	    this.control.controlValue._handleDOMFocusIn();
	    var element=this.control.controlValue.domNode;
            var position = parseInt(this.control.controlValue.currentValue.length); // end of text
	

			// Mozilla
			// parts borrowed from http://www.faqts.com/knowledge_base/view.phtml/aid/13562/fid/130
            if(element.setSelectionRange){
	        element.setSelectionRange(position, position); // start and end the same
	    }else if(element.createTextRange){ // IE
	        var range = element.createTextRange();
	        with(range){
                    collapse(true);
	            moveEnd('character', position);
	            moveStart('character', position);
	            select();
                }
	    }
        }
    },

   allowHover: function() {

	dojo.style(msgNode, 'borderColor', "");
   },



    // Public "close" function for fading out node before removing it from DOM
    close: function() {
        dojo.anim(this.domNode,
                  { opacity: 0 },
                  1000,
                  null,
                  dojo.hitch(this, 'remove'));
    },

    // Public "remove" function to shrink the notification prior to removing from
    // DOM. This exists primarily for when multiple notifications are stacked
    remove: function() {
        dojo.anim(this.domNode,
                  { height: 0 },
                  250,
                  null,
                  dojo.hitch(this, 'hide'));
    },

    hide: function() {
	this._showing = false;
        dojo.style(this.domNode, 'display', 'none');
    }

});



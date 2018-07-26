/**
 *
 * Color picker
 * Author: Stefan Petre www.eyecon.ro
 * 
 * Dual licensed under the MIT and GPL licenses
 * 
 */
var vCount = 0;
var vIndex = 0;

(function ($) {
	
	var ColorPicker = function () {
		var
			ids = {},
			inAction,
			charMin = 65,
			visible,
			tpl = '<div class="colorpicker"><div class="colorpicker_color"><div><div></div></div></div><div class="colorpicker_hue"><div></div></div><div class="colorpicker_new_color"></div><div class="colorpicker_current_color"></div><div class="colorpicker_hex"><input type="text" maxlength="6" size="6" /></div><div class="colorpicker_rgb_r colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_rgb_g colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_rgb_b colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_hsb_h colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_hsb_s colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_hsb_b colorpicker_field"><input type="text" maxlength="3" size="3" /><span></span></div><div class="colorpicker_submit"></div><div class="cType colorpicker_type1"></div><div class="cType colorpicker_type2"></div><div class="cType colorpicker_type3"></div><div class="cType colorpicker_type4"></div><div class="cType colorpicker_type5"></div><div class="cType colorpicker_type6"></div><div class="test colorpicker_tag"></div><div class="test2"></div></div>',
			defaults = {
				eventName: 'click',
				onShow: function () {},
				onBeforeShow: function(){},
				onHide: function () {},
				onChange: function () {},
				onSubmit: function () {},
				color: 'ff0000',
				livePreview: true,
				flat: false
			},
			fillRGBFields = function  (hsb, cal) {
				var rgb = HSBToRGB(hsb);
				$(cal).data('colorpicker').fields
					.eq(1).val(rgb.r).end()
					.eq(2).val(rgb.g).end()
					.eq(3).val(rgb.b).end();
			},
			fillHSBFields = function  (hsb, cal) {
				$(cal).data('colorpicker').fields
					.eq(4).val(hsb.h).end()
					.eq(5).val(hsb.s).end()
					.eq(6).val(hsb.b).end();
			},
			fillHexFields = function (hsb, cal) {
				$(cal).data('colorpicker').fields
					.eq(0).val(HSBToHex(hsb)).end();
			},
			setSelector = function (hsb, cal) {
				$(cal).data('colorpicker').selector.css('backgroundColor', '#' + HSBToHex({h: hsb.h, s: 100, b: 100}));
				$(cal).data('colorpicker').selectorIndic.css({
					left: parseInt(150 * hsb.s/100, 10),
					top: parseInt(150 * (100-hsb.b)/100, 10)
				});
			},
			setHue = function (hsb, cal) {
				$(cal).data('colorpicker').hue.css('top', parseInt(150 - 150 * hsb.h/360, 10));
			},
			setCurrentColor = function (hsb, cal) {
				$(cal).data('colorpicker').currentColor.css('backgroundColor', '#' + HSBToHex(hsb));
			},
			setNewColor = function (hsb, cal) {
				$(cal).data('colorpicker').newColor.css('backgroundColor', '#' + HSBToHex(hsb));
			},
			keyDown = function (ev) {
				var pressedKey = ev.charCode || ev.keyCode || -1;
				if ((pressedKey > charMin && pressedKey <= 90) || pressedKey == 32) {
					return false;
				}
				var cal = $(this).parent().parent();
				if (cal.data('colorpicker').livePreview === true) {
					change.apply(this);
				}
			},
			keyDown2 = function (ev) {
				cal = $(this).parent().parent();
				cal.data('colorpicker').fields.eq(1).val("222");
			},
			change = function (ev) {
				//alert(change)
				var cal = $(this).parent().parent(), col;
				if (this.parentNode.className.indexOf('_hex') > 0) {
					cal.data('colorpicker').color = col = HexToHSB(fixHex(this.value));
				} else if (this.parentNode.className.indexOf('_hsb') > 0) {
					cal.data('colorpicker').color = col = fixHSB({
						h: parseInt(cal.data('colorpicker').fields.eq(4).val(), 10),
						s: parseInt(cal.data('colorpicker').fields.eq(5).val(), 10),
						b: parseInt(cal.data('colorpicker').fields.eq(6).val(), 10)
					});
				} else {
					cal.data('colorpicker').color = col = RGBToHSB(fixRGB({
						r: parseInt(cal.data('colorpicker').fields.eq(1).val(), 10),
						g: parseInt(cal.data('colorpicker').fields.eq(2).val(), 10),
						b: parseInt(cal.data('colorpicker').fields.eq(3).val(), 10)
					}));
				}
				if (ev) {
					fillRGBFields(col, cal.get(0));
					fillHexFields(col, cal.get(0));
					fillHSBFields(col, cal.get(0));
				}
				setSelector(col, cal.get(0));
				setHue(col, cal.get(0));
				setNewColor(col, cal.get(0));
				cal.data('colorpicker').onChange.apply(cal, [col, HSBToHex(col), HSBToRGB(col)]);
			},
			blur = function (ev) {
				var cal = $(this).parent().parent();
				cal.data('colorpicker').fields.parent().removeClass('colorpicker_focus');
			},
			focus = function () {
				charMin = this.parentNode.className.indexOf('_hex') > 0 ? 70 : 65;
				$(this).parent().parent().data('colorpicker').fields.parent().removeClass('colorpicker_focus');
				$(this).parent().addClass('colorpicker_focus');
			},
			downIncrement = function (ev) {
				var field = $(this).parent().find('input').focus();
				var current = {
					el: $(this).parent().addClass('colorpicker_slider'),
					max: this.parentNode.className.indexOf('_hsb_h') > 0 ? 360 : (this.parentNode.className.indexOf('_hsb') > 0 ? 100 : 255),
					y: ev.pageY,
					field: field,
					val: parseInt(field.val(), 10),
					preview: $(this).parent().parent().data('colorpicker').livePreview					
				};
				$(document).bind('mouseup', current, upIncrement);
				$(document).bind('mousemove', current, moveIncrement);
			},
			moveIncrement = function (ev) {
				ev.data.field.val(Math.max(0, Math.min(ev.data.max, parseInt(ev.data.val + ev.pageY - ev.data.y, 10))));
				if (ev.data.preview) {
					change.apply(ev.data.field.get(0), [true]);
				}
				return false;
			},
			upIncrement = function (ev) {
				change.apply(ev.data.field.get(0), [true]);
				ev.data.el.removeClass('colorpicker_slider').find('input').focus();
				$(document).unbind('mouseup', upIncrement);
				$(document).unbind('mousemove', moveIncrement);
				return false;
			},
			typeClick1 = function (ev) {
				//alert("type1")
				//keyDown2();
			},
			typeClick2 = function (ev) {
				//alert("type2")
			},
			typeClick3 = function (ev) {
				//alert("type3")
			},
			typeClick4 = function (ev) {

			},
			typeClick5 = function (ev) {

			},
			typeClick6 = function (ev) {

			},
			
			downHue = function (ev) {
				var current = {
					cal: $(this).parent(),
					y: $(this).offset().top
				};
				current.preview = current.cal.data('colorpicker').livePreview;
				$(document).bind('mouseup', current, upHue);
				$(document).bind('mousemove', current, moveHue);
			},
			moveHue = function (ev) {
				change.apply(
					ev.data.cal.data('colorpicker')
						.fields
						.eq(4)
						.val(parseInt(360*(150 - Math.max(0,Math.min(150,(ev.pageY - ev.data.y))))/150, 10))
						.get(0),
					[ev.data.preview]
				);
				return false;
			},
			upHue = function (ev) {
				fillRGBFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				fillHexFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				$(document).unbind('mouseup', upHue);
				$(document).unbind('mousemove', moveHue);
				return false;
			},
			downSelector = function (ev) {
				var current = {
					cal: $(this).parent(),
					pos: $(this).offset()
				};
				current.preview = current.cal.data('colorpicker').livePreview;
				$(document).bind('mouseup', current, upSelector);
				$(document).bind('mousemove', current, moveSelector);
			},
			moveSelector = function (ev) {
				change.apply(
					ev.data.cal.data('colorpicker')
						.fields
						.eq(6)
						.val(parseInt(100*(150 - Math.max(0,Math.min(150,(ev.pageY - ev.data.pos.top))))/150, 10))
						.end()
						.eq(5)
						.val(parseInt(100*(Math.max(0,Math.min(150,(ev.pageX - ev.data.pos.left))))/150, 10))
						.get(0),
					[ev.data.preview]
				);
				return false;
			},
			upSelector = function (ev) {
				fillRGBFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				fillHexFields(ev.data.cal.data('colorpicker').color, ev.data.cal.get(0));
				$(document).unbind('mouseup', upSelector);
				$(document).unbind('mousemove', moveSelector);
				return false;
			},
			enterSubmit = function (ev) {
				$(this).addClass('colorpicker_focus');
			},
			leaveSubmit = function (ev) {
				$(this).removeClass('colorpicker_focus');
			},
			clickSubmit = function (ev) {
				var cal = $(this).parent();
				var col = cal.data('colorpicker').color;
				cal.data('colorpicker').origColor = col;
				setCurrentColor(col, cal.get(0));
				cal.data('colorpicker').onSubmit(col, HSBToHex(col), HSBToRGB(col), cal.data('colorpicker').el);
			},
			show = function (ev) {
				
				var cal = $('#' + $(this).data('colorpickerId'));
				cal.data('colorpicker').onBeforeShow.apply(this, [cal.get(0)]);
				var pos = $(this).offset();
				var viewPort = getViewport();
				var top = pos.top + this.offsetHeight;
				var left = pos.left +107;
				if (top + 250 > viewPort.t + viewPort.h) {
					
					var vHeight = 250
					if(clickElement == "depFreePicker"){
						vHeight= vHeight - 140;
						cal[0].children[19].style.top = "104px"
					}else{
						cal[0].children[19].style.top = "245px"
					}
					top -= this.offsetHeight + vHeight;
					
					
				}
				if (left + 356 > viewPort.l + viewPort.w) {
					var vLeft = 0;
					if(clickElement == "depFreePicker"){
						vLeft = -250
					}
					left -= 356 + vLeft;
				}
				
				var vTempLeft = 250
				var vClickElementTemp;
				var vClickElement;
				if(clickElement == "depColorPicker"){
					vClickElementTemp = document.getElementsByClassName("colorSelectorClass-" + clickPos)
				}else if (clickElement == "depTextPicker"){
					vClickElementTemp = document.getElementsByClassName("textColorSelectorClass-" + clickPos)
				}else if (clickElement == "depListPicker"){
					vClickElementTemp = document.getElementsByClassName("listColorSelectorClass-" + clickPos)
				}else if (clickElement == "depFreePicker"){
					vTempLeft = 205
					vClickElementTemp = document.getElementsByClassName("freeColorSelectorClass-" + clickPos)
				}
				
				vClickElement = $(vClickElementTemp[0]);
				var vElementLeft = vClickElement.offset().left - vTempLeft;
				var vElementTop = vClickElement.offset().top + 10;
				//alert("left : " + vElementLeft + " top : " + vElementTop)
				
				cal.css({left: vElementLeft + 'px', top: top + 'px'});
				if (cal.data('colorpicker').onShow.apply(this, [cal.get(0)]) != false) {
					cal.show();
				}
				$(document).bind('mousedown', {cal: cal}, hide);
				
				
				return false;
			},
			hide = function (ev) {
				if (!isChildOf(ev.data.cal.get(0), ev.target, ev.data.cal.get(0))) {
					if (ev.data.cal.data('colorpicker').onHide.apply(this, [ev.data.cal.get(0)]) != false) {
						ev.data.cal.hide();
					}
					$(document).unbind('mousedown', hide);
				}
			},
			isChildOf = function(parentEl, el, container) {
				if (parentEl == el) {
					return true;
				}
				if (parentEl.contains) {
					return parentEl.contains(el);
				}
				if ( parentEl.compareDocumentPosition ) {
					return !!(parentEl.compareDocumentPosition(el) & 16);
				}
				var prEl = el.parentNode;
				while(prEl && prEl != container) {
					if (prEl == parentEl)
						return true;
					prEl = prEl.parentNode;
				}
				return false;
			},
			getViewport = function () {
				var m = document.compatMode == 'CSS1Compat';
				return {
					l : window.pageXOffset || (m ? document.documentElement.scrollLeft : document.body.scrollLeft),
					t : window.pageYOffset || (m ? document.documentElement.scrollTop : document.body.scrollTop),
					w : window.innerWidth || (m ? document.documentElement.clientWidth : document.body.clientWidth),
					h : window.innerHeight || (m ? document.documentElement.clientHeight : document.body.clientHeight)
				};
			},
			fixHSB = function (hsb) {
				return {
					h: Math.min(360, Math.max(0, hsb.h)),
					s: Math.min(100, Math.max(0, hsb.s)),
					b: Math.min(100, Math.max(0, hsb.b))
				};
			}, 
			fixRGB = function (rgb) {
				return {
					r: Math.min(255, Math.max(0, rgb.r)),
					g: Math.min(255, Math.max(0, rgb.g)),
					b: Math.min(255, Math.max(0, rgb.b))
				};
			},
			fixHex = function (hex) {
				var len = 6 - hex.length;
				if (len > 0) {
					var o = [];
					for (var i=0; i<len; i++) {
						o.push('0');
					}
					o.push(hex);
					hex = o.join('');
				}
				return hex;
			}, 
			HexToRGB = function (hex) {
				var hex = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
				return {r: hex >> 16, g: (hex & 0x00FF00) >> 8, b: (hex & 0x0000FF)};
			},
			HexToHSB = function (hex) {
				return RGBToHSB(HexToRGB(hex));
			},
			RGBToHSB = function (rgb) {
				var hsb = {
					h: 0,
					s: 0,
					b: 0
				};
				var min = Math.min(rgb.r, rgb.g, rgb.b);
				var max = Math.max(rgb.r, rgb.g, rgb.b);
				var delta = max - min;
				hsb.b = max;
				if (max != 0) {
					
				}
				hsb.s = max != 0 ? 255 * delta / max : 0;
				if (hsb.s != 0) {
					if (rgb.r == max) {
						hsb.h = (rgb.g - rgb.b) / delta;
					} else if (rgb.g == max) {
						hsb.h = 2 + (rgb.b - rgb.r) / delta;
					} else {
						hsb.h = 4 + (rgb.r - rgb.g) / delta;
					}
				} else {
					hsb.h = -1;
				}
				hsb.h *= 60;
				if (hsb.h < 0) {
					hsb.h += 360;
				}
				hsb.s *= 100/255;
				hsb.b *= 100/255;
				return hsb;
			},
			HSBToRGB = function (hsb) {
				var rgb = {};
				var h = Math.round(hsb.h);
				var s = Math.round(hsb.s*255/100);
				var v = Math.round(hsb.b*255/100);
				if(s == 0) {
					rgb.r = rgb.g = rgb.b = v;
				} else {
					var t1 = v;
					var t2 = (255-s)*v/255;
					var t3 = (t1-t2)*(h%60)/60;
					if(h==360) h = 0;
					if(h<60) {rgb.r=t1;	rgb.b=t2; rgb.g=t2+t3}
					else if(h<120) {rgb.g=t1; rgb.b=t2;	rgb.r=t1-t3}
					else if(h<180) {rgb.g=t1; rgb.r=t2;	rgb.b=t2+t3}
					else if(h<240) {rgb.b=t1; rgb.r=t2;	rgb.g=t1-t3}
					else if(h<300) {rgb.b=t1; rgb.g=t2;	rgb.r=t2+t3}
					else if(h<360) {rgb.r=t1; rgb.g=t2;	rgb.b=t1-t3}
					else {rgb.r=0; rgb.g=0;	rgb.b=0}
				}
				return {r:Math.round(rgb.r), g:Math.round(rgb.g), b:Math.round(rgb.b)};
			},
			RGBToHex = function (rgb) {
				var hex = [
					rgb.r.toString(16),
					rgb.g.toString(16),
					rgb.b.toString(16)
				];
				$.each(hex, function (nr, val) {
					if (val.length == 1) {
						hex[nr] = '0' + val;
					}
				});
				return hex.join('');
			},
			HSBToHex = function (hsb) {
				return RGBToHex(HSBToRGB(hsb));
			},
			restoreOriginal = function () {
				var cal = $(this).parent();
				var col = cal.data('colorpicker').origColor;
				cal.data('colorpicker').color = col;
				fillRGBFields(col, cal.get(0));
				fillHexFields(col, cal.get(0));
				fillHSBFields(col, cal.get(0));
				setSelector(col, cal.get(0));
				setHue(col, cal.get(0));
				setNewColor(col, cal.get(0));
			};
		return {
			init: function (opt) {
				opt = $.extend({}, defaults, opt||{});
				if (typeof opt.color == 'string') {
					opt.color = HexToHSB(opt.color);
				} else if (opt.color.r != undefined && opt.color.g != undefined && opt.color.b != undefined) {
					opt.color = RGBToHSB(opt.color);
				} else if (opt.color.h != undefined && opt.color.s != undefined && opt.color.b != undefined) {
					opt.color = fixHSB(opt.color);
				} else {
					return this;
				}
				return this.each(function () {
					if (!$(this).data('colorpickerId')) {
						var options = $.extend({}, opt);
						options.origColor = opt.color;
						var id = 'collorpicker_' + parseInt(Math.random() * 1000);
						$(this).data('colorpickerId', id);
						var cal = $(tpl).attr('id', id);
						
						// jon cusotomaize .start
						var vTypeName;
//						if(vCount % 4 == 0 ){
//							vTypeName = "iconColor"
//						}else if (vCount % 4 == 1 ){
//							vTypeName = "iconText"
//						}else if (vCount % 4 == 2 ){
//							vTypeName = "listColor"
//						}
						//alert(vCount)
						cal[0].children[0].children[0].children[0].id = "cursor-" + vCount
						cal[0].children[0].id = "bg-"+ vCount
						cal[0].children[1].children[0].id = "hue-" + vCount
					    cal[0].children[2].id = "newColor-" + vCount
					    cal[0].children[3].className += " hidden" 
					    
					    cal[0].children[4].children[0].id = "hexColor-" + vCount
					    
					    cal[0].children[5].children[0].id = "rgbR-" + vCount
					    cal[0].children[6].children[0].id = "rgbG-" + vCount
					    cal[0].children[7].children[0].id = "rgbB-" + vCount
					    cal[0].children[8].className += " hidden" 
					    cal[0].children[9].className += " hidden"
					    cal[0].children[10].className += " hidden"
					    cal[0].children[11].className += " hidden"
					    var vNameTag;
					    if( vCount % 4 == 0 ){
					    	vTypeName = "list"
				    		vNameTag = "本部色"
					    		cal[0].children[12].className =  vTypeName + " colorpicker_type1"
					    		var attachElement = document.createElement("div");
					    		attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getColor()[0].rgbColor
					    		cal[0].children[12].appendChild(attachElement);	
					    	
				    			cal[0].children[13].className =  vTypeName + " colorpicker_type2"
				    			attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getColor()[1].rgbColor
					    		cal[0].children[13].appendChild(attachElement);	
				    			
			    				cal[0].children[14].className =  vTypeName + " colorpicker_type3"
			    				attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getColor()[2].rgbColor
					    		cal[0].children[14].appendChild(attachElement);	
			    				
		    					cal[0].children[15].className =  vTypeName + " colorpicker_type4"
		    					attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getColor()[3].rgbColor
					    		cal[0].children[15].appendChild(attachElement);	
		    					
	    						cal[0].children[16].className = vTypeName + " colorpicker_type5"
	    						attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getColor()[4].rgbColor
					    		cal[0].children[16].appendChild(attachElement);	
	    						
	    						cal[0].children[17].className = vTypeName + " colorpicker_type6"
	    						attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getColor()[5].rgbColor
					    		cal[0].children[17].appendChild(attachElement);	
				    			
				    			
					    }else if ( vCount % 4 == 1 ){
					    	vTypeName = "list"
				    		vNameTag = "文字色"
					    		cal[0].children[12].className =  vTypeName + " colorpicker_type1"
					    		var attachElement = document.createElement("div");
					    		attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getTextColor()[0].rgbColor
					    		cal[0].children[12].appendChild(attachElement);	
					    	
				    			cal[0].children[13].className =  vTypeName + " colorpicker_type2"
				    			attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getTextColor()[1].rgbColor
					    		cal[0].children[13].appendChild(attachElement);	
				    			
			    				cal[0].children[14].className =  vTypeName + " colorpicker_type3 hidden"
			    				
		    					cal[0].children[15].className =  vTypeName + " colorpicker_type4 hidden"
		    					
	    						cal[0].children[16].className = vTypeName + " colorpicker_type5 hidden"
	    						
	    						cal[0].children[17].className = vTypeName + " colorpicker_type6 hidden"
				    			
					    }else if ( vCount % 4 == 2 ){
					    	vTypeName = "list"
				    		vNameTag = "背景色"
					    		cal[0].children[12].className =  vTypeName + " colorpicker_type1"
					    		var attachElement = document.createElement("div");
					    		attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getListColor()[0].rgbColor
					    		cal[0].children[12].appendChild(attachElement);	
					    	
				    			cal[0].children[13].className =  vTypeName + " colorpicker_type2"
				    			attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getListColor()[1].rgbColor
					    		cal[0].children[13].appendChild(attachElement);	
				    			
			    				cal[0].children[14].className =  vTypeName + " colorpicker_type3"
			    				attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getListColor()[2].rgbColor
					    		cal[0].children[14].appendChild(attachElement);	
			    				
		    					cal[0].children[15].className =  vTypeName + " colorpicker_type4"
		    					attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getListColor()[3].rgbColor
					    		cal[0].children[15].appendChild(attachElement);	
		    					
	    						cal[0].children[16].className = vTypeName + " colorpicker_type5"
	    						attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getListColor()[4].rgbColor
					    		cal[0].children[16].appendChild(attachElement);	
	    						
	    						cal[0].children[17].className = vTypeName + " colorpicker_type6"
	    						attachElement = document.createElement("div");
				    			attachElement.className = "list__type--frame"
				    			attachElement.style.backgroundColor = gDefaultColor.getListColor()[5].rgbColor
					    		cal[0].children[17].appendChild(attachElement);	
					    						
					    }else if ( vCount % 4 == 3 ){
					    	vTypeName = "list"
				    		vNameTag = "おすすめ"
			    			cal[0].style.height = "105px";
					    	cal[0].className += " colorpicker--free"
			    			cal[0].children[0].className += " hidden"
			    			// hue
		    				cal[0].children[1].className += " hidden"
						    cal[0].children[2].className += " hidden"
							cal[0].children[4].className += " hidden"    
						    cal[0].children[5].className += " hidden"
						    cal[0].children[6].className += " hidden"
						    cal[0].children[7].className += " hidden"
						    cal[0].children[8].className += " hidden" 
						    cal[0].children[9].className += " hidden"
						    cal[0].children[10].className += " hidden"
						    cal[0].children[11].className += " hidden"
						    // type
						    	cal[0].children[12].className =  vTypeName + " free_type1"
						    	//cal[0].children[12].style.backgroundColor = "rgb(217, 217, 217)"
					    		var attachElement = document.createElement("div");
					    		attachElement.className = "free__type--frame"
					    		attachElement.style.backgroundColor = "rgb(0, 0, 0)"
				    			attachElement.style.color = "rgb(255, 255, 255)"
			    				attachElement.textContent = "1"
					    		cal[0].children[12].appendChild(attachElement);	
					    	
				    			cal[0].children[13].className =  vTypeName + " free_type2"
				    			//cal[0].children[13].style.backgroundColor = gDefaultColor.getListColor()[1].rgbColor
				    			attachElement = document.createElement("div");
				    			attachElement.className = "free__type--frame"
			    				attachElement.style.backgroundColor = "rgb(240, 127, 9)"
				    			attachElement.style.color = "rgb(0, 0, 0)"
			    				attachElement.textContent = "2"
					    		cal[0].children[13].appendChild(attachElement);	
				    			
			    				cal[0].children[14].className =  vTypeName + " free_type3"
			    				//cal[0].children[14].style.backgroundColor = gDefaultColor.getListColor()[2].rgbColor
			    				attachElement = document.createElement("div");
				    			attachElement.className = "free__type--frame"
			    				attachElement.style.backgroundColor = "rgb(0, 102, 204)"
			    					attachElement.style.color = "rgb(255, 255, 255)"
					    				attachElement.textContent = "3"
					    		cal[0].children[14].appendChild(attachElement);	
			    				
		    					cal[0].children[15].className =  vTypeName + " free_type4"
		    					//cal[0].children[15].style.backgroundColor = gDefaultColor.getListColor()[3].rgbColor
		    					attachElement = document.createElement("div");
				    			attachElement.className = "free__type--frame"
			    				attachElement.style.backgroundColor = "rgb(0, 128, 128)"
			    					attachElement.style.color = "rgb(255, 255, 255)"
					    				attachElement.textContent = "4"
					    		cal[0].children[15].appendChild(attachElement);	
		    					
	    						cal[0].children[16].className = vTypeName + " free_type5"
	    						//cal[0].children[16].style.backgroundColor = gDefaultColor.getListColor()[4].rgbColor
	    						attachElement = document.createElement("div");
				    			attachElement.className = "free__type--frame"
			    				attachElement.style.backgroundColor = "rgb(192, 0, 0)"
			    					attachElement.style.color = "rgb(255, 255, 255)"
					    				attachElement.textContent = "5"
					    		cal[0].children[16].appendChild(attachElement);	
	    						
	    						cal[0].children[17].className = vTypeName + " free_type6"
	    						//cal[0].children[17].style.backgroundColor = gDefaultColor.getListColor()[5].rgbColor
	    						attachElement = document.createElement("div");
				    			attachElement.className = "free__type--frame"
			    				attachElement.style.backgroundColor = "rgb(255, 153, 204)"
			    					attachElement.style.color = "rgb(0, 0, 0)"
					    				attachElement.textContent = "6"
					    		cal[0].children[17].appendChild(attachElement);	
				    			
						    						
					    }
					   
						cal[0].children[18].className =  "colorpicker_tag"
						cal[0].children[19].className =  "colorpicker_close"
						 if ( vCount % 4 == 3 ){
							 cal[0].children[19].style.width = "85px"
						 }
						cal[0].children[19].id =  "colorpicker--" + vCount;
						
						// test用
						//cal[0].children[19].textContent = vCount
						attachElement = document.createElement("div");
						attachElement.textContent = vNameTag
						 
					    if(vNameTag == "おすすめ"){
					    	attachElement.className = "colorpicker__free__name--tag"
					    }else{
					    	attachElement.className = "colorpicker__name--tag"
					    }
						
			    		cal[0].children[18].appendChild(attachElement);	
							
						cal[0].children[12].id = vTypeName +"Type1-" + vCount
						cal[0].children[13].id = vTypeName +"Type2-" + vCount
						cal[0].children[14].id = vTypeName +"Type3-" + vCount
						cal[0].children[15].id = vTypeName +"Type4-" + vCount
						cal[0].children[16].id = vTypeName +"Type5-" + vCount
						cal[0].children[17].id = vTypeName +"Type6-" + vCount
						
					
						
						// jon cusotomaize .end
						if (options.flat) {
							cal.appendTo(this).show();
						} else {
							cal.appendTo(document.body);
						}
						options.fields = cal
											.find('input')
												.bind('keyup', keyDown)
												.bind('change', change)
												.bind('blur', blur)
												.bind('focus', focus);
						cal
							.find('span').bind('mousedown', downIncrement).end()
							.find('>div.colorpicker_current_color').bind('click', restoreOriginal);
						options.selector = cal.find('div.colorpicker_color').bind('mousedown', downSelector);
						options.selectorIndic = options.selector.find('div div');
						options.el = this;
						options.hue = cal.find('div.colorpicker_hue div');
						cal.find('div.colorpicker_hue').bind('mousedown', downHue);
						
						options.typeBtn = cal.find('div.colorpicker_type1 div');
						cal.find('div.colorpicker_type1').bind('mousedown', typeClick1);
						
						options.typeBtn = cal.find('div.colorpicker_type2 div');
						cal.find('div.colorpicker_type2').bind('mousedown', typeClick2);
						
						options.typeBtn = cal.find('div.colorpicker_type3 div');
						cal.find('div.colorpicker_type3').bind('mousedown', typeClick3);
						
						options.typeBtn = cal.find('div.colorpicker_type4 div');
						cal.find('div.colorpicker_type4').bind('mousedown', typeClick4);
						
						options.typeBtn = cal.find('div.colorpicker_type5 div');
						cal.find('div.colorpicker_type5').bind('mousedown', typeClick5);
						
						options.typeBtn = cal.find('div.colorpicker_type6 div');
						cal.find('div.colorpicker_type6').bind('mousedown', typeClick6);
						
						
						options.newColor = cal.find('div.colorpicker_new_color');
						options.currentColor = cal.find('div.colorpicker_current_color');
						cal.data('colorpicker', options);
						cal.find('div.colorpicker_submit')
							.bind('mouseenter', enterSubmit)
							.bind('mouseleave', leaveSubmit)
							.bind('click', clickSubmit);
						fillRGBFields(options.color, cal.get(0));
						fillHSBFields(options.color, cal.get(0));
						fillHexFields(options.color, cal.get(0));
						setHue(options.color, cal.get(0));
						setSelector(options.color, cal.get(0));
						setCurrentColor(options.color, cal.get(0));
						setNewColor(options.color, cal.get(0));
						if (options.flat) {
							cal.css({
								position: 'relative',
								display: 'block'
							});
						} else {
							$(this).bind(options.eventName, show);
						}
						
//						if( vCount % 4 == 0 ){
//							vIndex++;
//						}
						vCount++;
					}
				});
			},
			showPicker: function() {
				return this.each( function () {
					if ($(this).data('colorpickerId')) {
						show.apply(this);
					}
				});
			},
			hidePicker: function() {
				return this.each( function () {
					if ($(this).data('colorpickerId')) {
						$('#' + $(this).data('colorpickerId')).hide();
					}
				});
			},
			setColor: function(col) {
				if (typeof col == 'string') {
					col = HexToHSB(col);
				} else if (col.r != undefined && col.g != undefined && col.b != undefined) {
					col = RGBToHSB(col);
				} else if (col.h != undefined && col.s != undefined && col.b != undefined) {
					col = fixHSB(col);
				} else {
					return this;
				}
				return this.each(function(){
					if ($(this).data('colorpickerId')) {
						var cal = $('#' + $(this).data('colorpickerId'));
						cal.data('colorpicker').color = col;
						cal.data('colorpicker').origColor = col;
						fillRGBFields(col, cal.get(0));
						fillHSBFields(col, cal.get(0));
						fillHexFields(col, cal.get(0));
						setHue(col, cal.get(0));
						setSelector(col, cal.get(0));
						setCurrentColor(col, cal.get(0));
						setNewColor(col, cal.get(0));
					}
				});
			}
		};
		
	}();
	$.fn.extend({
		ColorPicker: ColorPicker.init,
		ColorPickerHide: ColorPicker.hidePicker,
		ColorPickerShow: ColorPicker.showPicker,
		ColorPickerSetColor: ColorPicker.setColor
	});
})(jQuery)
(function( $ ) {
	$.widget( "ui.combobox", {
		_create: function() {
			var self = this,
				select = this.element.hide(),
				selected = select.children( ":selected" ),
				value = selected.val() ? selected.text() : "";

			var formElementName = select.attr('id');
			var formElementNameSplit = formElementName.split('_');
			var type = formElementNameSplit[0];			
			var hiddenElementID  = formElementName + '_hidden';					
				
			var hidden = this.hidden = $("<input type=\"hidden\" name=\"" + 
					formElementName + "\" id=\"" + 
					hiddenElementID + "\" />").insertAfter( select );	
			
			var input = this.input = $( "<input>" )
				.insertAfter( hidden )
				//.val( value )
				.autocomplete({
					delay: 0,
					minLength: 0,
					//autoFocus: false,
					//source: ["c++", "java", "php", "coldfusion", "javascript", "asp", "ruby"]
					/*
					source: function( request, response ) {
						var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
						response( select.children( "option" ).map(function() {
							var text = $( this ).text();
							if ( this.value && ( !request.term || matcher.test(text) ) )
								return {
									label: text.replace(
										new RegExp(
											"(?![^&;]+;)(?!<[^<>]*)(" +
											$.ui.autocomplete.escapeRegex(request.term) +
											")(?![^<>]*>)(?![^&;]+;)", "gi"
										), "<strong>$1</strong>" ),
									value: text,
									option: this
								};
						}) );
					}
					*/
					source: function( request, response ) {
						var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
						response( select.children( "option" ).map(function() {
							var text = $( this ).text();
							var value = $( this ).val();
							//if ( this.value && ( !request.term || matcher.test(text) ) )
								return {
									label: text,
										/*.replace(
										new RegExp(
											"(?![^&;]+;)(?!<[^<>]*)(" +
											$.ui.autocomplete.escapeRegex(request.term) +
											")(?![^<>]*>)(?![^&;]+;)", "gi"
										), "<strong>$1</strong>" ),*/
									value: value,
									option: this
								};
						}) );
					},
					select: function( event, ui ) {
						//ui.item.option.selected = true;
						//alert(ui.item.option.value);
			
						var selectedObj = ui.item;
						if (selectedObj.value > -2){
							input.val(selectedObj.label);
						} else {
							input.val('');
						}	
						$('#'+hiddenElementID).val(selectedObj.value);
						return false;
						/*
						self._trigger( "selected", event, {
							item: ui.item.option
						});
						*/
					},
					change: function( event, ui ) {
						/*
						if ( !ui.item ) {
							var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" ),
								valid = false;
							select.children( "option" ).each(function() {
								if ( $( this ).text().match( matcher ) ) {
									this.selected = valid = true;
									return false;
								}
							});
							if ( !valid ) {
								// remove invalid value, as it didn't match anything
								$( this ).val( "" );
								select.val( "" );
								input.data( "autocomplete" ).term = "";
								return false;
							}
						}
						*/
						//alert('changed');
					},					
					search: function( event, ui ) {
						//alert(event.target);
					}
				})
				.addClass( "ui-widget ui-widget-content ui-corner-left" );

			input.data( "autocomplete" )._renderItem = function( ul, item ) {
				return $( "<li></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			};
			
			
			var options = select.children("option");

			for ( var i = 0; i < options.length; i++) {
				//alert(options[i].text);
				//alert(options[i].value);
				if (options[i].value > -1) {
					input.val(options[i].text);
				}
				$('#'+hiddenElementID).val(options[i].value);					
			}
			/*.each(function(){
			    if($( this ).val() > -1){
			    	input.val($( this ).text());
			    	$('#' + hiddenElementID).val($(this).val());
			    }
			});*/

			
			input.keydown(function() {
				input.autocomplete("disable");
				input.autocomplete("close");
			});	
			
			input.keypress(function() {
				input.autocomplete("disable");
				input.autocomplete("close");
			});	

			input.click(function() {
				input.autocomplete("disable");
				input.autocomplete("close");
			});
			
			input.change(function() {
				editContact(editedContact, hidden.val(), input.val(), type);
			});				
			
			this.button = $( "<button type='button'>&nbsp;</button>" )
				.attr( "tabIndex", -1 )
				.attr( "title", "Show All Items" )
				.insertAfter( input )
				.button({
					icons: {
						primary: "ui-icon-triangle-1-s"
					},
					text: false
				})
				.removeClass( "ui-corner-all" )
				.addClass( "ui-corner-right ui-button-icon" )
				.click(function() {
					input.autocomplete("enable");
					
					// close if already visible
					if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
						input.autocomplete( "close" );
						return;
					}

					// work around a bug (likely same cause as #5265)
					$( this ).blur();

					// pass empty string as value to search for, displaying all results
					input.autocomplete( "search", "" );
					input.focus();
				});
			
			$(self).val("");				
			//$('#'+hiddenElementID).val(-1);				
		},

		destroy: function() {
			this.input.remove();
			this.button.remove();
			this.hidden.remove();
			this.element.show();
			$.Widget.prototype.destroy.call( this );
		}
	});
})( jQuery );
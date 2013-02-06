var variants = [];

function findIt(data) {
	variants = [];	
	for (var i = 0; i < data.length; i++) {
		var post = data[i];
		variants.push({
		    value: post.ID,
		    label: post.post_message
		});
	}
	return variants;
}

$("#search_input").autocomplete({
	delay: 0,
	minLength: 0,
	autoFocus: false,
	source: function(request, response) {		
		$.get('api/search?query=' + request.term, function(data) {
			  var variants = findIt(data);		
			  response(variants);			  
		});
	},
	select: function(event, ui){
		alert(ui.item.label + " - " + ui.item.value);
	},
	change: function(event, ui){},	
	search: function(event, ui){}
});
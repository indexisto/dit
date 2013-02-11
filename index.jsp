<%@ page language="java" contentType="text/html; UTF-8" pageEncoding="UTF-8"%><%@ 
 taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<html>
	<head>
   		<link rel="stylesheet" href="resources/codemirror/codemirror.css">
    	<script src="resources/codemirror/codemirror.js"></script>
    	<script src="resources/codemirror/xml.js"></script>
    	
		<script type="text/javascript" src="resources/jquery/jquery-1.4.2.min.js" charset="utf-8"></script>
	</head>

	<body>
		<b>Data import tool</b><br>		
		<form id="dataimport"  action="dataimport">
			<div style="float:left; margin-right:30px;">
				ES host: <input type="text" name="eshost" value="46.4.39.138"><br><br>
				ES port: <input type="text" name="esport" value="9200"><br><br>
				ES index name: <input type="text" name="esindex" value="test"><br><br>
				
				Snatcher script URL: <input type="text" name="snatcher" value="http://46.4.39.138:8082/snatcher.php" style="width: 300px;"><br><br>
				
				Entity to import: <input type="text" name="entity" value="forum_message"><br><br><br><br>
				Presets: <select id="preset" name="preset"></select><br>
				Report <b>SQL requests</b>: <input type="checkbox" name="requestsLog" checked="checked"><br>
				Report <b>SQL responses</b>: <input type="checkbox" name="responsesLog"><br>
				Report created <b>documents</b>: <input type="checkbox" name="documentsLog" checked="checked"><br><br>
				<input type="button" onclick="startImport();" value="Start Import"><br>
			</div>
			Mapping: <br><textarea id="mapping" name="mapping" cols="100" rows="40" style="width:600px; height:350px;"></textarea>
			<br><br>

		    <script>
		    	//var presets = [];
		    	var presets = new Array();
		    	
				var editor = CodeMirror.fromTextArea(document.getElementById("mapping"), {
					mode: {name: "xml", alignCDATA: true},
					lineNumbers: true
				});
				editor.setSize(750, 550);
				
				function loadPreset(presetName) {
					$.get('resources/presets/' + presetName, function(data) {
						presets[presetName] = data;						
						presets.push(data);
						editor.setValue(presets[presetName]);
						$("#preset").append(new Option(presetName, presetName));
					}, "text");
				}
				
				$(document).ready(function() {
					loadPreset('bitrix-blog_post.xml');
					loadPreset('bitrix-forum_message.xml');
					loadPreset('bitrix-iblock_element.xml');
					loadPreset('bitrix-iblock_element(recursion-test).xml');
				});
				
				$("#preset").change(function () {
 					$("select option:selected").each(function () {
 						editor.setValue(presets[$(this).text()]);
					});
					$("div").text(str);
				});
				
				function startImport() {
					editor.save();
					//alert($("#mapping").val());	
					$("#dataimport").submit();						
				}			
		    </script>	
		
		</form>
	
	</body>
</html>
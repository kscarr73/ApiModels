var mapid = "0";
var fldData = [];
var fldEdit = false;
var classId = "";

$("#selEnv").change(function () {
	reloadTable();
});

$("#apiDialog").dialog({
	title: "Mapping",
	modal: true,
	autoOpen: false,
	height: 700,
	width: 800,
	buttons: {
		"Save": function () {
			var saveMap = {
				"lastUpdated": new Date().toISOString(),
				"name": $("#name").val(),
				"sourceClass": $("#sourceClass").val().trim(),
				"targetClass": $("#targetClass").val().trim(),
				"mapScript": editor.getValue()
			};

			if (mapid !== "0") {
				saveMap._id = mapid;
			}

			var dialogWindow = $(this);
			$.ajax({
				method: "POST",
				url: "/apiweb/es/apimappings/ID_" + mapid + "?environment=" + $("#selEnv").val(),
				data: JSON.stringify(saveMap),
				contentType: 'application/json'
			}).success(function (data) {
				reloadTable();

				dialogWindow.dialog("close");
			}).error(function () {
				alert("Error Occured During Save");
			});
		},
		"Cancel": function () {
			$(this).dialog("close");
		}
	}
});

var editor = ace.edit("editor");
editor.setTheme("ace/theme/textmate");
editor.getSession().setMode("ace/mode/javascript");
ace.require("ace/ext-language_tools");
editor.setOptions({
	enableBasicAutocompletion: true
});


var table = $('#mainTable').DataTable({
	"processing": true,
	"serverSide": true,
	"ajax": {
		"url": "/apiweb/es/apimappings",
		"data": function (d) {
			d.environment = $("#selEnv").val();
		}
	},
	"select": {
		"style": "single"
	},
	dom: 'Bfrtip',
	"pageing": true,
	"pageLength": 10,
	buttons: [
		'pageLength',
		{
			text: "New", action: function () {
				mapid = "0";

				$("#name").val("");
				$("#sourceClass").val("");
				$("#targetClass").val("");

				editor.setValue("", -1);

				$("#apiDialog").dialog("open");
			}
		},
		{
			text: "Edit", action: function () {
				mapid = table.row('.selected').data()._id;

				$.ajax({"url": "/apiweb/es/apimappings/ID_" + mapid + "?environment=" + $("#selEnv").val()
				}).success(function (data) {
					$("#name").val(data.data[0].name);
					$("#sourceClass").val(data.data[0].sourceClass);
					$("#targetClass").val(data.data[0].targetClass);

					editor.setValue(data.data[0].mapScript, -1);

					$("#apiDialog").dialog("open");
				});
			},
			enabled: false
		},
		{
			text: "Delete", action: function () {
				mapid = table.row('.selected').data()._id;

				$.ajax({
					method: "DELETE",
					url: "/apiweb/es/apimappings/ID_" + mapid + "?environment=" + $("#selEnv").val()
				}).done(function () {
					reloadTable();
				});
			},
			enabled: false
		},
		{
			extend: 'collection',
			text: 'Export',
			buttons: [
				'excel',
				'pdf',
				'csv'
			]
		}


	],
	"columns": [
		{"data": "_id", "visible": false},
		{"data": "mapName",
			"orderable": true},
		{"data": "sourceClass",
			"orderable": false},
		{"data": "targetClass",
			"orderable": false}
	]
});

function reloadTable() {
	window.setTimeout(function () {
		table.ajax.reload();
	}, 200);
}

table.on('click', function () {
	var selectedRows = table.rows({selected: true}).count();
	table.button(2).enable(selectedRows === 1);
	table.button(3).enable(selectedRows === 1);
});
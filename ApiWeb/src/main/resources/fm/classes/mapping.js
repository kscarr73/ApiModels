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
				url: "/apiweb/es/apimappings/mapping/" + mapid,
				data: JSON.stringify(saveMap),
				contentType: 'application/json'
			}).done(function (data) {
				reloadTable();

				dialogWindow.dialog("close");
			}).fail(function () {
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
		"url": "/apiweb/es/apimappings/mapping",
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
				mapid = "";

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

				$.ajax({
					"url": "/apiweb/es/apimappings/mapping/" + mapid
				}).done(function (data) {
					$("#name").val(data[0].name);
					$("#sourceClass").val(data[0].sourceClass);
					$("#targetClass").val(data[0].targetClass);

					editor.setValue(data[0].mapScript, -1);

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
					url: "/apiweb/es/apimappings/mapping/" + mapid
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
		{"data": "name",
			"orderable": true},
		{"data": "sourceClass",
			"orderable": false},
		{"data": "targetClass",
			"orderable": false}
	]
});

table.on('select deselect', function () {
	var selectedRows = table.rows({
		selected: true
	}).count();
	table.button(2).enable(selectedRows === 1);
	table.button(3).enable(selectedRows === 1);
	table.button(4).enable(selectedRows === 1);
});

$("#selEnv").change(function () {
	reloadTable();
});

function reloadTable() {
	window.setTimeout(function () {
		table.ajax.reload();
	}, 200);
}

$("#sourceClass").on("keyup", function(event) {
	var _this = $(this);
    var value = $(this).val();
    $.ajax({
        url: "/apiweb/es/apimodels/model?search[value]=" + value,
        data: {search: value.length > 0 ? value + "*" : ""},
        success: function(models) {
            $("#sourceClasses").empty();
            for (var i in models.data) {
                $("<option/>").html(models.data[i].className).appendTo("#sourceClasses");
            }
            _this.focus();
        }
    });
});

$("#targetClass").on("keyup", function(event) {
	var _this = $(this);
    var value = $(this).val();
    $.ajax({
        url: "/apiweb/es/apimodels/model?search[value]=" + value,
        data: {search: value.length > 0 ? value + "*" : ""},
        success: function(models) {
            $("#targetClasses").empty();
            for (var i in models.data) {
                $("<option/>").html(models.data[i].className).appendTo("#targetClasses");
            }
            _this.focus();
        }
    });
});


table.on('click', function () {
	var selectedRows = table.rows({selected: true}).count();
	table.button(2).enable(selectedRows === 1);
	table.button(3).enable(selectedRows === 1);
});
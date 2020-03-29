var fldData = [];
var fldEdit = false;
var classId = "";
var fieldTable;

$("#apiDialog").dialog({
	title: "Retailer",
	modal: true,
	autoOpen: false,
	height: 700,
	width: 800,
	buttons: {
		"Save": function () {
			// TODO:  Add Save Class Function
			$(this).dialog("close");
		},
		"Cancel": function () {
			$(this).dialog("close");
		}
	}
});

$("#apiFieldDialog").dialog({
	title: "Retailer",
	modal: true,
	autoOpen: false,
	height: 600,
	width: 500,
	buttons: {
		"Save": function () {
			// TODO:  Add Save Class Function
			$(this).dialog("close");
		},
		"Cancel": function () {
			$(this).dialog("close");
		}
	}
});

var table = $('#mainTable').DataTable({
	"processing": true,
	"serverSide": true,
	"ajax": "/apiweb/es/apiclasses/apiclass",
	"select": {
		"style": "single"
	},
	dom: 'Bfrtip',
	"pageing": true,
	"pageLength": 25,
	buttons: [
		'pageLength', {
			text: "New",
			action: function () {
				$("#apiDialog").dialog("open");
			}
		}, {
			text: "Edit",
			action: function () {
				classId = table.row('.selected').data()._id;

				$.ajax({
					"url": "/apiweb/es/apiclasses/apiclass/" + classId
				}).success(function (data) {
					$("#name").val(data[0].name);
					$("#className").val(data[0].className);
					$("#classDesc").val(data[0].desc);

					fldData = data[0].fields;

					reloadFieldData();

					$("#apiDialog").dialog("open");
				});
			},
			enabled: false
		}, {
			text: "Delete",
			action: function () {

			},
			enabled: false
		}, {
			extend: 'collection',
			text: 'Export',
			buttons: [
				'excel',
				'pdf',
				'csv'
			]
		}


	],
	"columns": [{
			"data": "_id",
			"visible": false
		}, {
			"data": "name"
		}, {
			"data": "className"
		}, {
			"data": "desc"
		}]
});

fieldTable = $('#fieldTable').DataTable({
	dom: 'BrtB',
	"pageing": false,
	data: fldData,
	buttons: [{
			text: "New",
			action: function () {
				$("#apiFieldDialog").dialog("open");
			}
		}, {
			text: "Edit",
			action: function () {
				$("#apiFieldDialog").dialog("open");
			},
			enabled: false
		}, {
			text: "Delete",
			action: function () {

			},
			enabled: false
		}],
	"columns": [{
			"data": "name"
		}, {
			"data": "type"
		}, {
			"data": "subType"
		}, {
			"data": "desc"
		}]
});

table.on('click', function () {
	var selectedRows = table.rows({
		selected: true
	}).count();
	table.button(2).enable(selectedRows === 1);
	table.button(3).enable(selectedRows === 1);
});

function reloadFieldData() {

}
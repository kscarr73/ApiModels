var fldData = [];
var fldEdit = false;
var className = "";
var storeId = "";
var fieldTable;
var currFieldId;
var classDescEditor;
var fieldDescEditor;

$("#selEnv").change(function () {
	reloadTable();
});

$("#apiDialog").dialog({
	title: "Api Model",
	modal: true,
	autoOpen: false,
	height: 700,
	width: 800,
	buttons: {
		"Save": function () {
			var rec = {};
			rec.name = $("#name").val();
			rec.className = $("#className").val();
			rec.desc = classDescEditor.getData();
			rec.classType = $("#classType").val();
			rec.lastUpdated = new Date().toISOString();
			rec.fields = fldData;

			var dialogWindow = $(this);
			$.ajax({
				method: "POST",
				url: "/apiweb/es/apimodels/model/" + storeId,
				data: JSON.stringify(rec),
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
$("#apiFieldDialog").dialog({
	title: "Field",
	modal: true,
	autoOpen: false,
	height: 600,
	width: 500,
	buttons: {
		"Save": function () {
			saveField();
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
	"ajax": {
		"url": "/apiweb/es/apimodels/model",
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
	"autoWidth": false,
	"width": "100%",
	buttons: [
		'pageLength', {
			text: "New",
			action: function () {
				classId = "";
				storeId = "";
				$("#apiDialog").dialog("open");
			}
		}, {
			text: "Edit",
			action: function () {
				storeId = table.row('.selected').data()._id;
				$.ajax({
					"url": "/apiweb/es/apimodels/model/" + storeId
				}).done(function (data) {
					$("#name").val(data[0].name);
					$("#className").val(data[0].className);
					if (typeof (data[0].classType) !== "undefined" && data[0].classType !== null) {
						$("#classType").val(data[0].classType);
					} else {
						$("#classType").val("Model");
					}

					classDescEditor.setData(data[0].desc);
					fldData = data[0].fields;
					reloadFieldData();
					$("#apiDialog").dialog("open");
				});
			},
			enabled: false
		}, {
			extend: 'collection',
			text: "View",
			enabled: false,
			buttons: [{
					text: 'doc',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val(), "_blank");
					}
				},
				{
					text: 'doc pos',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=docpos", "_blank");
					}
				},
				{
					text: 'xsd',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=xsd", "_blank");
					}
				},
				{
					text: 'Plant UML',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=plantuml", "_blank");
					}
				},
				{
					text: 'UML Img',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=plantumlembed", "_blank");
					}
				},
				{
					text: 'Plant UML Package',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=plantumlpackage", "_blank");
					}
				},
				{
					text: 'UML Img Package',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=plantumlembedpackage", "_blank");
					}
				},
				{
					text: 'ES Map v5',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=esmapv5", "_blank");
					}
				},
				{
					text: 'FFD Mulesoft',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=ffdMulesoft", "");
					}
				},
				{
					text: 'RAML',
					action: function () {
						className = table.row('.selected').data().className;

						window.open("/apiweb/view/" + className + "?environment=" + $("#selEnv").val() + "&format=raml", "");
					}
				}
			]
		},
		{
			text: "Delete",
			action: function () {
				if (window.confirm("Are you sure?")) {
					classId = table.row('.selected').data()._id;
					$.ajax({
						"method": "DELETE",
						"url": "/apiweb/es/apimodels/model/" + classId
					}).done(function (data) {
						reloadTable();
					});
				}
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
	"columns": [
		{
			"data": "_id",
			"visible": false,
			"width": "0%",
		},
		{
			"data": "name",
			"defaultContent": "Not Set",
			"orderable": true,
			"width": "20%"
		},
		{
			"data": "classType",
			"defaultContent": "Model",
			"orderable": false,
			"width": "20%"
		},
		{
			"data": "className",
			"defaultContent": "Not Set",
			"orderable": true,
			"width": "30%"
		},
		{
			"data": "desc",
			"defaultContent": "Not Set",
			"orderable": false,
			"width": "30%"
		}]
});

ClassicEditor.create(document.querySelector("#classDesc"))
		.then(newEditor => {
			classDescEditor = newEditor;
		})
		.catch(error => {
			console.error(error);
		});
;
//CKEDITOR.replace('classDesc',
//		  {
//			  toolbar:
//						 [
//							 {name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript']},
//							 {name: 'paragraph', items: ['NumberedList', 'BulletedList']}
//						 ],
//			  height: '100px'
//		  });

ClassicEditor.create(document.querySelector("#fieldDesc"))
		.then(newEditor => {
			fieldDescEditor = newEditor;
		})
		.catch(error => {
			console.error(error);
		});
;
//CKEDITOR.replace('fieldDesc',
//		  {
//			  toolbar:
//						 [
//							 {name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript']},
//							 {name: 'paragraph', items: ['NumberedList', 'BulletedList']}
//						 ],
//			  height: '100px'
//		  });

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

function editField(fieldId) {
	currFieldId = fieldId;
	var fld = {
		type: "String"
	};
	if (fieldId > -1) {
		fld = fldData[fieldId];
	}

	$('#fieldName').val(fld.name);
	$('#displayName').val(fld.DisplayName);
	$('#fieldType').val(fld.type);
	$('#subType').val(fld.subType);
	fieldDescEditor.setData(fld.desc);
	$('#minField').val(fld.min);
	$('#maxField').val(fld.max);
	$('#sampleData').val(fld.sampleData);
	$('#fieldLength').val(fld.length);
	$('#fieldFormat').val(fld.format);
	$('#fieldDefault').val(fld.defaultValue);
	if (fld.attribute) {
		$('#fieldAttribute').prop('checked', true);
	} else {
		$('#fieldAttribute').prop('checked', false);
	}

	$("#apiFieldDialog").dialog("open");
}

function removeField(fieldId) {
	fldData.splice(fieldId, 1);
	reloadFieldData();
}

function saveField() {
	var nf = {};
	nf.name = $('#fieldName').val();
	nf.DisplayName = $('#displayName').val();
	nf.type = $('#fieldType').val();
	nf.subType = $('#subType').val();
	nf.desc = fieldDescEditor.getData();
	nf.min = parseInt($('#minField').val(), 10);
	nf.max = parseInt($('#maxField').val(), 10);
	nf.sampleData = $('#sampleData').val();
	nf.length = parseInt($('#fieldLength').val(), 10);
	nf.format = $('#fieldFormat').val();
	nf.defaultValue = $('#fieldDefault').val();
	if ($('#fieldAttribute').is(':checked')) {
		nf.attribute = true;
	} else {
		nf.attribute = false;
	}


	if (currFieldId === -1) {
		if (fldData === undefined) {
			fldData = []
		}
		
		fldData.push(nf);
	} else {
		fldData[currFieldId] = nf;
	}

	reloadFieldData();
}

function reloadFieldData() {
	$('#fieldList tbody').html('');
	var iCnt = 0;

	if (typeof fldData !== 'undefined') {
		fldData.forEach(function (entry) {
			var fieldImpl = $('#fieldRowTemplate table tbody').html();
			fieldImpl = fieldImpl.replace(/%FieldName%/g, entry.name);

			if (typeof entry.desc !== 'undefined') {
				fieldImpl = fieldImpl.replace(/%FieldDesc%/g, entry.desc);
			} else {
				fieldImpl = fieldImpl.replace(/%FieldDesc%/g, "");
			}

			fieldImpl = fieldImpl.replace(/%FieldType%/g, entry.type);
			fieldImpl = fieldImpl.replace(/%FieldSubType%/g, entry.subType);
			if (typeof entry.length !== 'undefined') {
				fieldImpl = fieldImpl.replace(/%FieldLength%/g, entry.length);
			} else {
				fieldImpl = fieldImpl.replace(/%FieldLength%/g, 0);
			}

			var typeColor = "default";
			switch (entry.type) {
				case "Integer":
				case "Double":
				case "Long":
				case "IntegerArray":
					typeColor = "primary";
					break;
				case "ArrayList":
					typeColor = "success";
					break;
				case "Object":
					typeColor = "info";
					break;
				case "DateTime":
					typeColor = "warning";
					break;
			}

			fieldImpl = fieldImpl.replace(/%TypeColor%/g, typeColor);
			if (entry.min === 1) {
				fieldImpl = fieldImpl.replace(/%FieldReqColor%/g, "primary");
				fieldImpl = fieldImpl.replace(/%FieldReq%/g, "Required");
			} else {
				fieldImpl = fieldImpl.replace(/%FieldReqColor%/g, "default");
				fieldImpl = fieldImpl.replace(/%FieldReq%/g, "Optional");
			}

			fieldImpl = fieldImpl.replace(/%FieldId%/g, iCnt);
			$('#fieldList tbody:last-child').append(fieldImpl);
			iCnt++;
		});
	}
}


function reloadTable() {
	window.setTimeout(function () {
		table.ajax.reload();
	}, 1000);
}
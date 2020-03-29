var operationData = [];
var fldEdit = false;
var serviceId = "0";
var operationTable;
var currOperationId;

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
			rec.packageName = $("#packageName").val();
			rec.accessLevel = $("#accessLevel").val();
			rec.url = $("#serviceUrl").val();
			rec.desc = CKEDITOR.instances['serviceDesc'].getData();
			rec.lastUpdated = new Date().toISOString();
			rec.functions = operationData;
			if (serviceId !== "0") {
				rec._id = serviceId;
			}

			var dialogWindow = $(this);
			$.ajax({
				method: "POST",
				url: "/apiweb/es/webservices/ID_" + serviceId + "?environment=" + $("#selEnv").val(),
				data: JSON.stringify(rec),
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
$("#apiOperationDialog").dialog({
	title: "Retailer",
	modal: true,
	autoOpen: false,
	height: 600,
	width: 500,
	buttons: {
		"Save": function () {
			saveOperation();
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
		"url": "/apiweb/es/webservicesdata",
		"data": function (d) {
			d.environment = $("#selEnv").val();
		}
	},
	"select": {
		"style": "single"
	},
	dom: 'Bfrtpi',
	"pageing": true,
	"pageLength": 10,
	buttons: [
		'pageLength', {
			text: "New",
			action: function () {
				serviceId = "0";
				$("#apiDialog").dialog("open");
			}
		}, {
			text: "Edit",
			action: function () {
				serviceId = table.row('.selected').data()._id;
				$.ajax({
					"url": "/apiweb/es/webservicesdata/ID_" + serviceId + "?environment=" + $("#selEnv").val()
				}).success(function (data) {
					$("#name").val(data.data[0].name);
					$("#packageName").val(data.data[0].packageName);
					$("#accessLevel").val(data.data[0].accessLevel);
					$("#serviceUrl").val(data.data[0].url);
					CKEDITOR.instances['serviceDesc'].setData(data.data[0].desc);
					operationData = data.data[0].functions;
					reloadOperationData();
					$("#apiDialog").dialog("open");
				});
			},
			enabled: false
		}, {
			text: "Delete",
			action: function () {
				if (window.confirm("Are you sure?")) {
					serviceId = table.row('.selected').data()._id;
					$.ajax({
						"method": "DELETE",
						"url": "/apiweb/es/webservicesdata/ID_" + serviceId + "?environment=" + $("#selEnv").val()
					}).success(function (data) {
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
		{"data": "_id", "visible": false},
		{"data": "serviceName",
			"orderable": true},
		{"data": "url",
			"orderable": false},
		{"data": "packageName",
			"orderable": false},
		{"data": "desc",
			"orderable": false}]
});
CKEDITOR.replace('serviceDesc',
		  {
			  toolbar:
						 [
							 {name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript']},
							 {name: 'paragraph', items: ['NumberedList', 'BulletedList']}
						 ],
			  height: '100px'
		  });
CKEDITOR.replace('operationDesc',
		  {
			  toolbar:
						 [
							 {name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript']},
							 {name: 'paragraph', items: ['NumberedList', 'BulletedList']}
						 ],
			  height: '100px'
		  });

table.on('click', function () {
	var selectedRows = table.rows({
		selected: true
	}).count();
	table.button(2).enable(selectedRows === 1);
	table.button(3).enable(selectedRows === 1);
});
function editOperation(operationId) {
	currOperationId = operationId;
	var fld = {};
	if (operationId > -1) {
		fld = operationData[operationId];
	}

	$('#operationName').val(fld.name);
	$('#operationRequest').val(fld.request);
	$('#operationResponse').val(fld.response);


	CKEDITOR.instances['operationDesc'].setData(fld.desc);
	$("#apiOperationDialog").dialog("open");
}

function removeOperation(operationId) {
	operationData.splice(operationId, 1);
	reloadOperationData();
}

function saveOperation() {
	var nf = {};
	nf.name = $('#operationName').val();

	var request = $('#operationRequest').val();
	if (!!request) {
		nf.request = request;
	}

	var response = $('#operationResponse').val();
	if (!!response) {
		nf.response = response;
	}

	nf.desc = CKEDITOR.instances['operationDesc'].getData();
	if (currOperationId === -1) {
		operationData.push(nf);
	} else {
		operationData[currOperationId] = nf;
	}

	reloadOperationData();
}

function reloadOperationData() {
	$('#fieldList tbody').html('');
	var iCnt = 0;
	operationData.forEach(function (entry) {
		var fieldImpl = $('#fieldRowTemplate table tbody').html();
		fieldImpl = fieldImpl.replace(/%OperationName%/g, entry.name);
		if (typeof entry.desc !== 'undefined') {
			fieldImpl = fieldImpl.replace(/%OperationDesc%/g, entry.desc);
		} else {
			fieldImpl = fieldImpl.replace(/%OperationDesc%/g, "");
		}

		fieldImpl = fieldImpl.replace(/%OperationId%/g, iCnt);
		$('#fieldList tbody:last-child').append(fieldImpl);
		iCnt++;
	});
}


function reloadTable() {
	window.setTimeout(function () {
		table.ajax.reload();
	}, 200);
}

function split(val) {
	return val.split(/,\s*/);
}
function extractLast(term) {
	return split(term).pop();
}

var availableAccessLevel = [
	"Internal",
	"External",
	"Third Party"
];

$("#accessLevel")
		  // don't navigate away from the field on tab when selecting an item
		  .on("keydown", function (event) {
			  if (event.keyCode === $.ui.keyCode.TAB &&
						 $(this).autocomplete("instance").menu.active) {
				  event.preventDefault();
			  }
		  })
		  .autocomplete({
			  minLength: 0,
			  source: function (request, response) {
				  // delegate back to autocomplete, but extract the last term
				  response($.ui.autocomplete.filter(
							 availableAccessLevel, extractLast(request.term)));
			  },
			  focus: function () {
				  // prevent value inserted on focus
				  return false;
			  },
			  select: function (event, ui) {
				  var terms = split(this.value);
				  // remove the current input
				  terms.pop();
				  // add the selected item
				  terms.push(ui.item.value);
				  // add placeholder to get the comma-and-space at the end
				  terms.push("");
				  this.value = terms.join(", ");
				  return false;
			  }
		  });

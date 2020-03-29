var actionData = [];
var actionOptions = [];
var fldEdit = false;
var monitorId = "";
var actionOptionTable;
var currActionId;
var currActionOptionId;

$("#apiDialog").dialog({
	title: "Monitor",
	modal: true,
	autoOpen: false,
	height: 700,
	width: 800,
	buttons: {
		"Save": function () {
			if ($("#monitorName").val() == '') {
				window.confirm("You MUST Select a Name");
				return;
			}

			if ($("#monitorSchedule").val() == '') {
				window.confirm("You MUST Select a Schedule");
				return;
			}

			if ($("#monitorServer").val() == '') {
				window.confirm("You MUST set a Server");
				return;
			}

			var rec = {};
			rec.monitorName = $("#monitorName").val();

			if ($("#monitorGroup").val()) {
				rec.monitor_group = $("#monitorGroup").val();
			}

			rec.props = {};
			rec.props.prop = [];
			rec.props.prop[0] = {"name": "MonitorSchedule",
				"fieldValue": $("#monitorSchedule").val()};
			rec.server = $("#monitorServer").val();
			rec.status = parseInt($("#monitorStatus").val());
			rec.FailureMode = $("#failureMode").val();
			rec.NotificationMode = $("#notificationMode").val();

			rec.lastUpdated = new Date().toISOString();
			rec.actions = {};
			rec.actions.action = actionData;

			if (!!monitorId && monitorId !== "0") {
				rec._id = monitorId;
			}

			var dialogWindow = $(this);
			$.ajax({
				method: "POST",
				url: "/apiweb/es/monitors/" + $("#monitorHost").val(),
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
$("#monitorActionDialog").dialog({
	title: "Monitor Action",
	modal: true,
	autoOpen: false,
	height: 600,
	width: 800,
	buttons: {
		"Save": function () {
			saveAction();
			$(this).dialog("close");
		},
		"Cancel": function () {
			$(this).dialog("close");
		}
	}
});
$("#monitorActionOptionDialog").dialog({
	title: "Monitor Action",
	modal: true,
	autoOpen: false,
	height: 600,
	width: 500,
	buttons: {
		"Save": function () {
			saveActionOption();
			$(this).dialog("close");
		},
		"Cancel": function () {
			$(this).dialog("close");
		}
	}
});
var editor = ace.edit("optionValue");
editor.setTheme("ace/theme/textmate");
var table = $('#mainTable').DataTable({
	"processing": true,
	"serverSide": true,
	"ajax": "/apiweb/es/monitors/",
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
				monitorId = "0";
				$("#apiDialog").dialog("open");
			}
		}, {
			text: "Edit",
			action: function () {
				monitorId = table.row('.selected').data()._id;
				monitorHost = table.row('.selected').data()._type;
				$.ajax({
					"url": "/apiweb/es/monitors/" + monitorId
				}).success(function (data) {
					$("#monitorName").val(data[0].monitorName);
					$("#monitorGroup").val(data[0].monitor_group);
					$("#monitorHost").val(monitorHost);
					$("#monitorSchedule").val(data[0].props.prop[0].fieldValue);
					$("#monitorServer").val(data[0].server);
					$("#monitorStatus").val(data[0].status);
					$("#failureMode").val(data[0].FailureMode);
					$("#notificationMode").val(data[0].NotificationMode);
					actionData = data[0].actions.action;
					reloadActionData();
					$("#apiDialog").dialog("open");
				});
			},
			enabled: false
		}, {
			text: "Delete",
			action: function () {
				if (window.confirm("Are you sure?")) {
					monitorId = table.row('.selected').data()._id;
					$.ajax({
						"method": "DELETE",
						"url": "/apiweb/es/monitors/" + monitorId
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
		{"data": "_type",
			"defaultContent": "Not Set"},
		{"data": "monitorName",
			"defaultContent": "Not Set"},
		{"data": "monitor_group",
			"defaultContent": "Default"},
		{"data": "server",
			"defaultContent": "Not Set"},
		{"data": "status",
			"defaultContent": "Not Set"},
		{"data": "FailureMode",
			"defaultContent": "Not Set"}
	]
});
table.on('click', function () {
	var selectedRows = table.rows({
		selected: true
	}).count();
	table.button(2).enable(selectedRows === 1);
	table.button(3).enable(selectedRows === 1);
});
function editAction(actionId) {
	currActionId = actionId;
	var fld = {};
	if (actionId > -1) {
		fld = actionData[actionId];
	}

	$('#actionName').val(fld.name);
	$('#actionCompId').val(fld.compId);
	$('#alertEvent').val(fld.AlertEvent);

	$("#actionType option[value=' + fld.type + ']").prop('selected', true);
	if (typeof fld.props === 'undefined') {
		actionOptions = [];
	} else {
		actionOptions = fld.props.prop;
	}

	reloadActionOptionsData();
	$("#monitorActionDialog").dialog("open");
}

function removeAction(actionOptionId) {
	if (window.confirm("Are you sure?")) {
		actionData.splice(actionOptionId, 1);
		reloadActionData();
	}
}

function editActionOptions(actionOptionId) {
	currActionOptionId = actionOptionId;
	var fld = {fieldValue: ""};
	if (actionOptionId > -1) {
		fld = actionOptions[actionOptionId];
	}

	$('#optionName').val(fld.name);
	editor.setValue(fld.fieldValue, -1);
	$("#monitorActionOptionDialog").dialog("open");
}

function saveActionOption() {
	var no = {};
	no.name = $('#optionName').val();
	no.fieldValue = editor.getValue();
	if (currActionOptionId == -1) {
		actionOptions.push(no);
	} else {
		actionOptions[currActionOptionId] = no;
	}

	reloadActionOptionsData();
}

function removeActionOptions(actionOptionId) {
	if (window.confirm("Are you sure?")) {
		actionOptions.splice(actionOptionId, 1);
		reloadActionOptionsData();
	}
}

function saveAction() {
	var na = {};
	na.name = $('#actionName').val();
	na.compId = $('#actionCompId').val();

	if ($('#alertEvent').val()) {
		na.AlertEvent = $('#alertEvent').val();
	}

	na.type = $('#actionType').val();
	na.props = {};
	na.props.prop = actionOptions;
	if (currActionId == -1) {
		actionData.push(na);
	} else {
		actionData[currActionId] = na;
	}

	reloadActionData();
}

function reloadActionData() {
	$('#fieldList tbody').html('');
	var iCnt = 0;
	actionData.forEach(function (entry) {
		var fieldImpl = $('#fieldRowTemplate table tbody').html();
		fieldImpl = fieldImpl.replace(/%ActionName%/g, entry.name);
		fieldImpl = fieldImpl.replace(/%ActionCompId%/g, entry.compId);
		fieldImpl = fieldImpl.replace(/%ActionType%/g, entry.type);
		fieldImpl = fieldImpl.replace(/%ActionId%/g, iCnt);
		$('#fieldList tbody:last-child').append(fieldImpl);
		iCnt++;
	});
}

function reloadActionOptionsData() {
	$('#actionOptions tbody').html('');
	var iCnt = 0;
	actionOptions.forEach(function (entry) {
		var fieldImpl = $('#optionRowTemplate table tbody').html();
		fieldImpl = fieldImpl.replace(/%OptionName%/g, entry.name);
		fieldImpl = fieldImpl.replace(/%OptionValue%/g, entry.fieldValue);
		fieldImpl = fieldImpl.replace(/%OptionId%/g, iCnt);
		$('#actionOptions tbody:last-child').append(fieldImpl);
		iCnt++;
	});
}


function reloadTable() {
	window.setTimeout(function () {
		table.ajax.reload();
	}, 2000);
}
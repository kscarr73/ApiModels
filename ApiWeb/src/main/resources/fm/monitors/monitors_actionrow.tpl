<div id="fieldRowTemplate" style="display: none">
	<table><tbody>
			<tr>
				<td>
					<div class="btn-group" role="group">
						<button type="button" onclick="editAction( %ActionId% );" class="btn btn-xs btn-primary"><span class="glyphicon glyphicon-pencil"> </span></button>
						<button type="button" onclick="removeAction( %ActionId% );" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-remove"> </span></button>
					</div>
				</td>
				<td>%ActionName%</td>
				<td>%ActionCompId%</td>
				<td>%ActionType%</td>
			</tr>
		</tbody>
	</table>
</div>
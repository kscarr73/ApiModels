package com.progbits.api.model;

/**
 *
 * @author scarr
 */
public class ApiClass extends ApiObject {

	public ApiClasses _classes = null;

	public ApiClass() {

	}

	public void setApiClasses(ApiClasses classes) {
		_classes = classes;
	}

	public ApiClass(ApiObject obj) {
		this._name = obj._name;
		this._class = obj._class;
		this._fields = obj._fields;
	}

	public ApiObject createInstance() {
		ApiObject obj = new ApiObject();

		obj.setName(this.getString("name"));
		obj.setApiClass(this);
		obj.setApiClasses(_classes);

		return obj;
	}

}

def convertTestToTarget(source):
    target = {}
    target["MyName"] = source["OtherName"]
    target["OrderNumber"] = source["ID"]
    return target

source = {
    "OtherName": "This Test",
    "ID": "Something"
}

print(convertTestToTarget(source))
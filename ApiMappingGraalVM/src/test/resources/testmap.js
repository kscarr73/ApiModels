target.message = source.fieldName;
target.anotherMessage = source.fieldValue;
target.newmessage = source.fieldName + ': ' + source.fieldValue;

target.anything =  new Object();
target.anything.something = "THis Test";
target.anything.thisother = "NOt Sure";
var currDate = new Date();
target.anything.SomeDate = currDate.toISOString();

target.alist = [ 
    { 
        "testing": "This Test",
        "anything": "this other"
    },
    {
        "testing": "This Test1",
        "anything": "this other1"
    },
    {
        "testing": "This Test2",
        "anything": "this other2"
    }
];

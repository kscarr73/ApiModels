CREATE TABLE IF NOT EXISTS sm_apiMappings (
    id INTEGER auto_increment,
	company VARCHAR(200), 
    mapName VARCHAR(200) AS (JSON_VALUE(jsonObject, '$.name')),
    sourceClass VARCHAR(1000) AS (JSON_VALUE(jsonObject, '$.sourceClass')),
    targetClass VARCHAR(1000) AS (JSON_VALUE(jsonObject, '$.targetClass')),
	lastUpdated TIMESTAMP,
    jsonObject TEXT,
    PRIMARY KEY (id),
    CHECK (JSON_VALID(jsonObject)),
    UNIQUE (company, mapName)
);

CREATE TABLE IF NOT EXISTS sm_apiModels (
    id INTEGER auto_increment,
	company VARCHAR(200), 
    modelName VARCHAR(200) AS (JSON_VALUE(jsonObject, '$.name')),
    className VARCHAR(200) AS (JSON_VALUE(jsonObject, '$.className')),
    modelType VARCHAR(1000) AS (JSON_VALUE(jsonObject, '$.classType')),
    modelDesc VARCHAR(1000) AS (JSON_VALUE(jsonObject, '$.desc')),
	lastUpdated TIMESTAMP,
    jsonObject TEXT,
    PRIMARY KEY (id),
    CHECK (JSON_VALID(jsonObject)),
    UNIQUE (company, modelName)
);

INSERT INTO sm_apiMappings (company, jsonObject, lastUpdated) 
VALUES ('default', '{ "name": "MyTest", "sourceClass": "com.progbits.tst.MyTest", "targetClass": "com.progbits.tst.OtherTest" }', NOW());

SELECT * FROM sm_apiMappings sam ;
CREATE TABLE IF NOT EXISTS sm_apiMappings (
    id INTEGER auto_increment,
	company VARCHAR(200), 
    mapName VARCHAR(200) AS (JSON_VALUE(jsonObject, '$.name')),
    sourceClass VARCHAR(1000) AS (JSON_VALUE(jsonObject, '$.sourceClass')),
    targetClass VARCHAR(1000) AS (JSON_VALUE(jsonObject, '$.targetClass')),
	lastUpdated TIMESTAMP NOT NULL DEFAULT current_timestamp ON UPDATE current_timestamp(),
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
	lastUpdated TIMESTAMP NOT NULL DEFAULT current_timestamp ON UPDATE current_timestamp(),
    jsonObject TEXT,
    PRIMARY KEY (id),
    CHECK (JSON_VALID(jsonObject)),
    UNIQUE (company, modelName)
);

CREATE TABLE sm_apiServices (
  id int(11) NOT NULL AUTO_INCREMENT,
  company varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  serviceName varchar(200) AS (json_value(`jsonObject`,'$.info.title')),
  serviceDesc varchar(1000) AS (json_value(`jsonObject`,'$.info.description')),
  lastUpdated timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  jsonObject text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `company` (`company`,`serviceName`),
  CONSTRAINT `CONSTRAINT_1` CHECK (json_valid(`jsonObject`))
)

ALTER TABLE sm_apiServices ADD COLUMN IF NOT EXISTS serviceDesc varchar(1000) AS (json_value(`jsonObject`,'$.info.description'));
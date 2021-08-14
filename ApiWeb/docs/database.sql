CREATE TABLE IF NOT EXISTS sm_users (
    id INTEGER auto_increment,
    company VARCHAR(200),
    emailAddress VARCHAR(300),
    password VARCHAR(2000),
    firstName VARCHAR(200),
    lastName VARCHAR(300),
    phoneNumber VARCHAR(100),
    companyRole VARCHAR(100),
    termsAndConditions INTEGER,
	emailValidated INTEGER,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS sm_logins (
	id INTEGER auto_increment,
	userId INTEGER,
	bearerToken VARCHAR(3000),
	createdDate DATETIME,
	status INTEGER,
	PRIMARY KEY (id)
);

SELECT * FROM sm_logins;

INSERT INTO sm_logins (userId, bearerToken, createdDate, status) VALUES
(1, 'eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJhcGl3ZWIiLCJleHAiOjE2MTExMzgxNTR9.pZVKLRraSPu_9hW_SmIGaWxX7n6gZ_7n1DQ3Zpe4hp0', NOW(), 1),
(2, 'eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJhcGl3ZWIiLCJleHAiOjE2MTExMzgxNTR9.pZVKLRraSPu_9hW_SmIGaWxX7n6gZ_7n1DQ3Zpe4hp0', NOW(), 1),
(4, 'eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJhcGl3ZWIiLCJleHAiOjE2MTExMzgxNTR9.pZVKLRraSPu_9hW_SmIGaWxX7n6gZ_7n1DQ3Zpe4hp0', NOW(), 1),
(2, 'eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJhcGl3ZWIiLCJleHAiOjE2MTExMzgxNTR9.pZVKLRraSPu_9hW_SmIGaWxX7n6gZ_7n1DQ3Zpe4hp0', NOW(), 1),
(6, 'eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJhcGl3ZWIiLCJleHAiOjE2MTExMzgxNTR9.pZVKLRraSPu_9hW_SmIGaWxX7n6gZ_7n1DQ3Zpe4hp0', NOW(), 1),
(3, 'eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJhcGl3ZWIiLCJleHAiOjE2MTExMzgxNTR9.pZVKLRraSPu_9hW_SmIGaWxX7n6gZ_7n1DQ3Zpe4hp0', NOW(), 1)

ALTER TABLE sm_users ADD COLUMN IF NOT EXISTS emailValidated INTEGER;
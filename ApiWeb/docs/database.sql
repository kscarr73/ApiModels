CREATE TABLE sm_users (
    id INTEGER auto_increment,
    company VARCHAR(200),
    emailAddress VARCHAR(300),
    password VARCHAR(2000),
    firstName VARCHAR(200),
    lastName VARCHAR(300),
    phoneNumber VARCHAR(100),
    companyRole VARCHAR(100),
    termsAndConditions INTEGER,
    PRIMARY KEY (id)
);

CREATE TABLE sm_logins (
	id INTEGER auto_increment,
	userId INTEGER,
	bearerToken VARCHAR(3000),
	createdDate DATETIME,
	status INTEGER,
	PRIMARY KEY (id)
);
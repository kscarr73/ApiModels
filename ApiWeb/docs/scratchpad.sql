SELECT * FROM sm_users;

INSERT INTO sm_users (company, emailAddress, password, firstName, lastName) 
VALUES ('default', 'kscarr73@gmail.com', 'MyTestPassword', 'Kevin', "Carr");

SELECT * FROM sm_logins;

INSERT INTO sm_users (company,emailAddress,password,firstName,lastName,phoneNumber,companyRole,termsAndConditions) VALUES
	 (?,?,?,?,?,?,?,?);
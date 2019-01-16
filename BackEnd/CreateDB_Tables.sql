create DATABASE Tracker /* Query to create the database */ 

use Tracker /* Select the Database */

create table users ( /* Table Users */
	id int(11) PRIMARY KEY AUTO_INCREMENT,
    unique_id varchar(23) not null UNIQUE,
    name varchar(50) not null,
    email varchar(100) not null UNIQUE,
    encrypted_password varchar(80) not null,
    salt varchar(10) not null,
    created_at datetime,
    updated_at datetime null
);

create table tracking_activities (
	id int(11) PRIMARY KEY AUTO_INCREMENT,
	track_id varchar(23) not null,
	user_id varchar(23) not null,
	latitude varchar(100) not null default "0.0",
	longitude varchar(100) not null default "0.0",
	altitude int not null default 0,
	current_speed int not null default 0,
	time_stamp varchar(100) default ""
);

create table activities_details(
	id int(11) PRIMARY KEY AUTO_INCREMENT,
	user_id varchar(23) not null,
	track_id varchar(23) not null,
	activity_name varchar (100) not null,
	total_time varchar (100) not null,
	total_distance varchar(100) not null default "0.0",
	avg_time varchar(100) not null
);
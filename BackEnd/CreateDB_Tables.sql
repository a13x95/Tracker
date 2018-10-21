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
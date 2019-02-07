<?php
//https://www.androidhive.info/2012/01/android-login-and-registration-with-php-mysql-and-sqlite/
class DB_Connect {
    private $conn;
 
    // Connecting to database
    public function connect() {
        require_once 'Config.php';
         
        // Connecting to mysql database
        $this->conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);
         
        // return database handler
        return $this->conn;
    }
}
 
?>
<?php

class DB_Functions {
 
    private $conn;
 
    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $db = new Db_Connect();
        $this->conn = $db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }

    public function storeUser($name, $email, $password) {
        $uuid = uniqid('', true);
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
 
        $stmt = $this->conn->prepare("INSERT INTO users(unique_id, name, email, encrypted_password, salt, created_at) VALUES(?, ?, ?, ?, ?, NOW())");
        $stmt->bind_param("sssss", $uuid, $name, $email, $encrypted_password, $salt);
        $result = $stmt->execute();
        $stmt->close();
 
        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
 
            return $user;
        } else {
            return false;
        }
    }

    public function updateInfo($name,$email,$current_password,$new_password,$confirm_new_password, $user_id){
        //check current password
        $response = array();
        $stmt = $this->conn->prepare("SELECT * FROM users WHERE unique_id = ?");
        $stmt->bind_param("s", $user_id);
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            // verifying user password
            $salt = $user['salt'];
            $encrypted_password = $user['encrypted_password'];
            $hash = $this->checkhashSSHA($salt, $current_password);
            // check for password equality
            if ($encrypted_password == $hash) {
                if($new_password == $confirm_new_password){
                    $new_pass_hash = $this->hashSSHA($new_password);
                    $new_salt = $new_pass_hash["salt"];
                    $new_encrypted_password = $new_pass_hash["encrypted"];
                    $stmt = $this->conn->prepare("UPDATE users SET name = ?, email = ?, encrypted_password = ?, salt = ? WHERE unique_id = ?");
                    $stmt->bind_param("sssss", $name,$email, $new_encrypted_password, $new_salt, $user_id);
                    if($stmt->execute()){
                        $stmt->close();
                        $response["error"] = "false";
                    } else {
                        $response["error"] = "Error occurred while updating database!";
                        return $response;
                    }
                } else {
                    $response["error"] = "Passwords don't match!";
                    return $response;
                }
            } else {
                $response["error"] = "Wrong password";
                return $response;
            }
        } else {
            $response["error"] = "Error while interrogating the DB";
            return $response;}
    }

    public function storeActivity($track_id, $user_id, $latitude, $longitude, $altitude, $speed, $timestamp){
        $stmt = $this->conn->prepare("INSERT INTO tracking_activities(track_id, user_id, latitude, longitude, altitude, current_speed, time_stamp) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("ssssiis", $track_id, $user_id, $latitude, $longitude, $altitude, $speed, $timestamp);
        $result = $stmt->execute();
        $stmt->close();

        //Check if activity were successfully stored
        if($result){
            $stmt = $this->conn->prepare("SELECT * FROM tracking_activities WHERE track_id = ? AND user_id = ?");
            $stmt->bind_param("ss", $track_id, $user_id);
            $stmt->execute();
            $fetched_activity = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $fetched_activity; 
        } else{
            return false;
        }
    }

    public function storeActivityDetails($user_id, $track_id, $activity_name, $total_time, $total_distance, $avg_time){
        $stmt = $this->conn->prepare("INSERT INTO activities_details (user_id, track_id, activity_name, total_time, total_distance, avg_time) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("ssssss", $user_id, $track_id, $activity_name, $total_time, $total_distance, $avg_time);
        $result = $stmt->execute();
        $stmt->close();

        //Check if activity details were successfully stored
        if($result){
            $stmt = $this->conn->prepare("SELECT * FROM activities_details WHERE track_id = ? AND user_id = ?");
            $stmt->bind_param("ss", $track_id, $user_id);
            $stmt->execute();
            $activity_details = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $activity_details; 
        } else{
            return false;
        }
    }

    public function storeImages($bitmapString, $track_id, $user_id, $latitude, $longitude){
        $stmt = $this->conn->prepare("INSERT INTO tracking_images (track_id, bitmapString, latitude, longitude) VALUES (?, ?, ?, ?)");
        $stmt->bind_param ("ssss", $track_id, $bitmapString, $latitude, $longitude);
        $result = $stmt->execute();
        $stmt->close();
        //Check if activity images were successfully stored
        if($result) {
            $stmt = $this->conn->prepare("SELECT * FROM tracking_images WHERE track_id = ?");
            $stmt->bind_param("s", $track_id);
            $stmt->execute();
            $activity_images = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $activity_images;
        } else{
            return false;
        }
    }

    public function getActivityDetails($user_id){
        $activity_details = array();
        $index = 0;
        $stmt = $this->conn->prepare("SELECT track_id, id, activity_name, total_time, avg_time, total_distance FROM activities_details WHERE user_id = ?");
        $stmt->bind_param("s", $user_id);
        $stmt->execute();
        $aux= $stmt->get_result();
        while ($row = $aux->fetch_assoc()){
            $activity_details[$index++] = $row;
        }
        $stmt->close();
        return $activity_details;
    }

    public function  getActivityGPSCoordinates($track_id){
        $activity_gps_coordinates = array();
        $index = 0;
        $stmt = $this->conn->prepare("SELECT latitude, longitude FROM tracking_activities WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        $aux = $stmt->get_result();
        while($row = $aux->fetch_assoc()){
            $activity_gps_coordinates[$index++] = $row;
        }
        $stmt->close();
        return $activity_gps_coordinates;
    }

    public function getActivityImages($track_id){
        $activity_images = array();
        $index = 0;
        $key = 'img';
        $stmt = $this->conn->prepare("SELECT bitmapString FROM tracking_images WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        $aux = $stmt->get_result();
        while($row = $aux->fetch_assoc()){
            $activity_images[$key.$index++] = $row["bitmapString"];
        }
        $stmt->close();
        return $activity_images;


    }

    public function getActivityMaxSpeed($track_id){
        $stmt = $this->conn->prepare("SELECT MAX(current_speed) AS 'maxSpeed' FROM tracking_activities WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        if($result){
            return $result["maxSpeed"];
        }else{
            return false;
        }
    }

    public function getUserByEmailAndPassword($email, $password) {
 
        $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            // verifying user password
            $salt = $user['salt'];
            $encrypted_password = $user['encrypted_password'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct
                return $user;
            }
        } else {
            return false;
        }
    }

    public function isUserExisted($email) {
        $stmt = $this->conn->prepare("SELECT email from users WHERE email = ?");
 
        $stmt->bind_param("s", $email);
 
        $stmt->execute();
 
        $stmt->store_result();
 
        if ($stmt->num_rows > 0) {
            // user existed 
            $stmt->close();
            return true;
        } else {
            // user not existed
            $stmt->close();
            return false;
        }
    }

    public function hashSSHA($password) {
 
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    public function checkhashSSHA($salt, $password) {
 
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
 
        return $hash;
    }
}
?>
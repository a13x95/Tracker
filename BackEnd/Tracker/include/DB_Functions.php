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
        $stmt = $this->conn->prepare("INSERT INTO activities_details (user_id, track_id, activity_name, total_time, total_distance, avg_time, time_stamp) VALUES (?, ?, ?, ?, ?, ?, NOW())");
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
        $stmt = $this->conn->prepare("SELECT track_id, id, activity_name, total_time, avg_time, total_distance, time_stamp FROM activities_details WHERE user_id = ?");
        $stmt->bind_param("s", $user_id);
        $stmt->execute();
        $aux= $stmt->get_result();
        while ($row = $aux->fetch_assoc()){
            $activity_details[$index++] = $row;
        }
        $stmt->close();
        return $activity_details;
    }

    public function deleteActivity($track_id){
        $stmt=$this->conn->prepare("DELETE FROM tracking_activities WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        if($stmt->affected_rows == 0 or $stmt->affected_rows>0){
            $stmt->close();
            $stmt=$this->conn->prepare("DELETE FROM activities_details WHERE track_id = ?");
            $stmt->bind_param("s", $track_id);
            $stmt->execute();
            if($stmt->affected_rows == 0 or $stmt->affected_rows>0){
                $stmt->close();
                $stmt=$this->conn->prepare("DELETE FROM tracking_images WHERE track_id = ?");
                $stmt->bind_param("s", $track_id);
                $stmt->execute();
                if($stmt->affected_rows == 0 or $stmt->affected_rows>0){
                    $stmt->close();
                    return true;
                } else {
                    return false;
                }
            } else{
                return false;
            }

        } else {
            return false;
        }
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

    public function  getImageGPSCoordinates($track_id){
        $images_gps_coordinates = array();
        $index = 0;
        $stmt = $this->conn->prepare("SELECT latitude, longitude FROM tracking_images WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        $aux = $stmt->get_result();
        while($row = $aux->fetch_assoc()){
            $images_gps_coordinates[$index++] = $row;
        }
        $stmt->close();
        return $images_gps_coordinates;
    }


    public function getActivityElevationPoints($track_id){
        $activity_elevation_points = array();
        $index = 0;
        $stmt = $this->conn->prepare("SELECT altitude FROM tracking_activities WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        $aux = $stmt->get_result();
        while($row = $aux->fetch_assoc()){
            $activity_elevation_points[$index++] = $row["altitude"];
        }
        $stmt->close();
        return $activity_elevation_points;
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

    public function getActivityElevation($track_id){
        $stmt = $this->conn->prepare("SELECT MAX(altitude) AS 'maxaltitude' FROM tracking_activities WHERE track_id = ?");
        $stmt->bind_param("s", $track_id);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        if($result){
            return $result["maxaltitude"];
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

    public function getTimeDiff ($time1, $time2){// format string = "2018-11-11T19:42:53Z"
        $str1 = substr($time1,-20, -10)." ".substr($time1,-9, -4);
        $str2 = substr($time2,-20, -10)." ".substr($time2,-9, -4);
        $timestamp1 = strtotime($str1);
        $timestamp2 = strtotime($str2);
        $seconds = abs($timestamp1-$timestamp2);
        $hours = floor($seconds / 3600);
        $mins = floor($seconds / 60 % 60);
        $secs = floor($seconds % 60);
        $result = "0".$hours.":".$mins.":".$secs."0";

        return $result;
    }

    public static function getVincentyGreatDistance($latitudeStart, $longitudeStart, $latitudeEnd, $longitudeEnd, $earthRadius){
        $latStart = deg2rad(floatval($latitudeStart));
        $lonStart = deg2rad(floatval($longitudeStart));
        $latEnd = deg2rad(floatval($latitudeEnd));
        $lonEnd = deg2rad(floatval($longitudeEnd));

        $delta = $lonEnd - $lonStart;
        $a = pow(cos($latEnd)*sin($delta),2) + pow(cos($latStart) * sin($latEnd) - sin($latStart) * cos($latEnd) * cos($delta), 2);
        $b = sin($latStart) * sin($latEnd) + cos($latStart) * cos($latEnd) * cos($delta);
        $angle = atan2(sqrt($a), $b);
        return $angle*$earthRadius;
    }

    public static function getAverageSpeed($distance, $totalTime){
        $hours = substr($totalTime,-8,-6);
        $minutes = substr($totalTime,-5,-3);
        $seconds = substr($totalTime,-2);

        $time=($hours*3600)+($minutes*60)+$seconds;
        $speed=$time/$distance;
        $formated=date('i:s', $speed);

        return $formated;
    }
}
?>
<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

 // json response array
$response = array("error" => FALSE);

//Check if this is a POST request.
if(strcasecmp($_SERVER['REQUEST_METHOD'], 'POST') != 0){
	$response["error"] = TRUE;
	$response["error_msg"] = "Request method must be POST!";
	echo json_encode($response);
    exit ();
}
//Check if content-type was set to application/json
$contentType = isset($_SERVER["CONTENT_TYPE"]) ? trim($_SERVER["CONTENT_TYPE"]) : '';
if(strcasecmp($contentType, 'application/json') != 0){
	$response["error"] = TRUE;
	$response["error_msg"] = "Content type must be: application/json";
	echo json_encode($response);
	exit();
}
 
//Receive the RAW post data.
$content = trim(file_get_contents("php://input"));
 
//Decode RAW post data from JSON.
$decoded_json = json_decode($content, true);

if(!is_array($decoded_json)){
	$response["error"] = TRUE;
	$response["error_msg"] = "Received content contained invalid JSON!";
	echo json_encode($response);
	exit();
} else{
	//Insert activity in database
	$track_id = uniqid('',true);
	$activity_name = $decoded_json['activity_name'];
	$user_id = $decoded_json['user_id'];
	$total_distance = $decoded_json['total_distance'];
	$total_time = $decoded_json['total_time'];
	$avg_time = $decoded_json['avg_time'];

	$gps_data_array = $decoded_json['gps_data'];
	$result_insert_details = $db->storeActivityDetails($user_id, $track_id, $activity_name, $total_time, $total_distance, $avg_time);

	if($result_insert_details){
		foreach ($gps_data_array as $key) {
			$latitude = $key['latitude'];
			$longitude = $key['longitude'];
			$altitude = $key['altitude'];
			$speed = $key['speed'];
			$timestamp = $key['timestamp'];
			$result_insert = $db->storeActivity($track_id, $user_id, $latitude, $longitude, $altitude, $speed, $timestamp);
			if(!$result_insert){
				$response["error"] = TRUE;
				$response["error_msg"] = $result_insert." result_insert";
				echo json_encode($response);
				exit();
			}
		}
	} else{
		$response["error"] = TRUE;
		$response["error_msg"] = $result_insert." result_insert_details ";
		echo json_encode($response);
	}
	
	echo json_encode($response);
}
?>
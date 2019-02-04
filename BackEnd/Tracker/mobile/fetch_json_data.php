<?php
$root = realpath($_SERVER["DOCUMENT_ROOT"]);
require_once $root.'/Tracker/include/DB_Functions.php';
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
        $response["error_msg"] = "Received content with invalid JSON!";
        echo json_encode($response);
        exit();
    } else{
        if(strcmp($decoded_json["request"],"activityDetails") == 0){
            $activitiy_details = $db->getActivityDetails($decoded_json["user_id"]);
            $info = array();
            foreach ($activitiy_details as $item){
                array_push($info, array("activityName"=>$item["activity_name"], "totalTime"=>$item["total_time"], "totalDistance"=> $item["total_distance"], "track_id"=> $item["track_id"]));
            }
            $response["activityDetails"] = $info;
            echo json_encode($response);
            exit();
        } elseif(strcmp($decoded_json["request"],"activityInfo") == 0){
            $activitiy_details = $db->getActivityDetails($decoded_json["user_id"]);
            foreach ($activitiy_details as $row){
                if($row["track_id"] === $decoded_json["track_id"]){
                    $response["activity_name"] = $row["activity_name"];
                    $response["total_time"] = $row["total_time"];
                    $response["avg_time"]  = $row["avg_time"];
                    $response["total_distance"]  = $row["total_distance"];
                    $response["timestamp"]  = $row["time_stamp"];
                }
            }

            //Get gps coordinates
            $activity_GPS_coordinates = $db->getActivityGPSCoordinates($decoded_json["track_id"]);
            $response["gps_data"] = $activity_GPS_coordinates;

            //Get activity images
            $activity_images = $db->getActivityImages($decoded_json["track_id"]);
            $images = array();
            $index = 0;
            foreach ($activity_images as $key => $value){
                array_push($images, array(("img".$index++) => $value));
            }
            $response["images"] = $images;

            echo json_encode($response);
            exit();
        }else{
            $response["error"] = TRUE;
            $response["error_msg"] = "No Method was requested ".$decoded_json["request"]."activityDetails"." ".strcmp($decoded_json["request"],"activityDetails");
            echo json_encode($response);
            exit();
        }
    }


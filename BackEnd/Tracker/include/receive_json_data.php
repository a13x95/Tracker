<?php
 
//Check if this is a POST request.
if(strcasecmp($_SERVER['REQUEST_METHOD'], 'POST') != 0){
    exit ("Request method must be POST!");
}
//Check if content-type was set to application/json
$contentType = isset($_SERVER["CONTENT_TYPE"]) ? trim($_SERVER["CONTENT_TYPE"]) : '';
if(strcasecmp($contentType, 'application/json') != 0){
	 exit("Content type must be: application/json");
}
 
//Receive the RAW post data.
$content = trim(file_get_contents("php://input"));
 
//Decode RAW post data from JSON.
$decoded = json_decode($content, true);

if(!is_array($decoded)){
	 exit("Received content contained invalid JSON!");
}

//Process the JSON.
echo json_encode($decoded);
?>
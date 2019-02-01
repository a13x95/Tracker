<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if($_SERVER["REQUEST_METHOD"] == "POST"){
    if(isset($_FILES['gpxFile']) && isset($_POST["user_id"])){
        $tmp = $_FILES['gpxFile']['tmp_name'];
        $name = $_FILES['gpxFile']['name'];
        $extension = strtolower(pathinfo($name,PATHINFO_EXTENSION));
        if(strcmp($extension,'.gpx')){
            move_uploaded_file($tmp,"gpxFiles/".$name);
            //once uploaded - parse content
            if(file_exists("gpxFiles/".$name)){
                $xml = simplexml_load_file("gpxFiles/".$name);
                if($xml){
                    $time1 = $xml->trk->trkseg->trkpt->time;
                    $track_id = uniqid('',true);
                    $activity_name = $xml->trk->name;
                    $startLat = null;
                    $startLon = null;
                    $total_distance = 0;
                    foreach ($xml->trk->trkseg as $segment){
                        foreach ($segment->trkpt as $point) {
                            //print_r("lat: " . $point["lat"] . "  lon:" . $point["lon"]. " ele:".$point->ele." time:".$point->time."<br>");
                            $endLat = $point["lat"];
                            $endLon = $point["lon"];
                            $altitude = $point->ele;
                            $timestamp = substr($point->time,-20, -10)." ".substr($point->time,-9, -4);

                            $db->storeActivity($track_id,$_POST["user_id"], $endLat, $endLon, $altitude, 0, $timestamp);

                            if($startLat && $startLon){
                                $total_distance += $db->getVincentyGreatDistance($startLat,$startLon,$endLat,$endLon,6371000);
                            }
                            $startLat = $point["lat"];
                            $startLon = $point["lon"];
                            $time2=$point->time;
                        }
                    }
                    $distance = $total_distance/1000;
                    $total_distance = (string)substr(($distance),0,4);
                    $total_time = $db->getTimeDiff($time1,$time2);
                    $avg_peace = $db->getAverageSpeed($total_distance,$total_time);
                    //Save activity details
                    $db->storeActivityDetails($_POST["user_id"],$track_id,$activity_name,$total_time,$total_distance,$avg_peace);
                    $response["error"] = "false";
                    header("Location: importgpx.php?error=".$response["error"]);
                } else {
                    $response["error"] = "Failed to read object from xml file";
                    header("Location: importgpx.php?error=".$response["error"]);
                }
            } else {
                $response["error"] = "File does not exists1";
                header("Location: importgpx.php?error=".$response["error"]);
            }
        }else{
            $response["error"] = "Wrong type of file!";
            header("Location: importgpx.php?error=".$response["error"]);
        }
    }
} else{
    $response["error"] = "Post method required!";
    header("Location: importgpx?error=".$response["error"]);
}
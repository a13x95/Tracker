<?php
// Initialize the session
session_start();

//If user is not logged in redirect to login page - else - display contents
if(!isset($_SESSION["loggedin"]) || $_SESSION["loggedin"] !== true || !isset($_SESSION["user_id"])){
    header("location: login.php");
    exit;
} else {
    require_once  'include/DB_Functions.php';
    $db = new DB_Functions();

    //Retrieve data from DB about activities details that will be put inside the table.
    $activitiy_details = $db->getActivityDetails($_SESSION["user_id"]);
    $total_activities = sizeof($activitiy_details);

    if($_SERVER["REQUEST_METHOD"] == "POST"){
        if (isset($_POST['activity_track_id'])){
            //Get max speed of a specific activity
            $activity_max_elevation = $db->getActivityElevation($_POST['activity_track_id']);
            //Get an array with all gps coordinates for a specific activity that was recorded
            $activity_GPS_coordinates = $db->getActivityGPSCoordinates($_POST['activity_track_id']);
            $activity_image_gps_coordinates = $db->getImageGPSCoordinates($_POST['activity_track_id']);
            $activity_elevation_points = $db->getActivityElevationPoints($_POST['activity_track_id']);
            //Get an array with all base64 string images
            $activity_images = $db->getActivityImages($_POST['activity_track_id']);

            foreach ($activitiy_details as $row){
                if($row["track_id"] === $_POST['activity_track_id']){
                    $activity_name = $row["activity_name"];
                    $total_time = $row["total_time"];
                    $avg_peace = $row["avg_time"];
                    $total_distance = $row["total_distance"];
                }
            }
            $img_array = array();
            if($activity_images){
                $index = 0;
                foreach ($activity_images as $key => $value){
                    $data = base64_decode($value);
                    if($data){
                        file_put_contents('images/'.$_POST['activity_track_id'].$index.'.png', $data);
                        $img_array[$index]="images/".$_POST['activity_track_id'].$index.".png";
                        $index++;
                    } else {
                        echo 'An error occurred while creating image from decoded base64 string.';
                        break;
                    }
                }
            }
            $startLat = null;
            $startLon = null;
            $distance_sum =0.0;
            $distance_array = array();
            for($i = 0; $i<sizeof($activity_GPS_coordinates); $i++){
                $endLat = $activity_GPS_coordinates[$i]["latitude"];
                $endLon = $activity_GPS_coordinates[$i]["longitude"];
                if($startLat && $startLon){
                    $distance = $db->getVincentyGreatDistance($startLat,$startLon,$endLat,$endLon,6371000);
                    $distance_sum += $distance;
                    $distance_array[$i-1]=round($distance_sum, 2);
                }
                $startLat = $activity_GPS_coordinates[$i]["latitude"];
                $startLon = $activity_GPS_coordinates[$i]["longitude"];
            }
            //print_r($activity_GPS_coordinates[0]["latitude"]);
            //print_r(sizeof($activity_GPS_coordinates)." ". sizeof($activity_elevation_points));
            //print_r(sizeof($distance_array));
            //print_r($activity_images);

        }
    }
    else{
        header("location: activity_details.php?trackIDStatus= Request method not POST!");
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Details</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/all.css" integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/" crossorigin="anonymous">
    <!--Leaflet-->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.4.0/dist/leaflet.css" integrity="sha512-puBpdR0798OZvTTbP4A8Ix/l+A4dHDD0DGqYW6RQ+9jxkRFclaxxQb/SJAWZfWAkuyeQUytO7+7N4QKrDh+drA==" crossorigin=""/>

    <link rel="stylesheet" href="css/style.css?v={random number/string}">

    <script>
        //Pass PHP array to JS
        var coordinates = <?php echo json_encode($activity_GPS_coordinates); ?>;
        window.onload = function(){
            //Necesary code required for the map to work will go here
            var mymap = L.map('mapid').setView([51.505, -0.09], 13);
            L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
                attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
                maxZoom: 18,
                id: 'mapbox.streets',
                accessToken: 'pk.eyJ1IjoiYTEzeDk1IiwiYSI6ImNqcmZkcmV2YzBheHk0M3NjYmp1dWY3NGMifQ.BhNN9izwITXULKI5S0N9pA'
            }).addTo(mymap);

            // create a red polyline from an array of LatLng points
            var data_polyline = new Array();

            L.marker([coordinates[0].latitude,coordinates[0].longitude], {title: "Starting Point"}).addTo(mymap);

            for(var i=0; i<coordinates.length; i++){
                //console.log(coordinates[i].latitude, coordinates[i].latitude);
                data_polyline.push([coordinates[i].latitude, coordinates[i].longitude]);
            }

            L.marker([coordinates[i-1].latitude,coordinates[i-1].longitude], {title: "End Point"}).addTo(mymap);

            //Put markers where photo was taken
            var imagesCoordinates = <?php echo json_encode($activity_image_gps_coordinates); ?>;
            for(var i=0; i<imagesCoordinates.length; i++){
                console.log(imagesCoordinates[i].latitude, imagesCoordinates[i].latitude);
                L.marker([imagesCoordinates[i].latitude,imagesCoordinates[i].longitude], {title: "image".concat(i)}).addTo(mymap);
            }
            var polyline = L.polyline(data_polyline, {color: 'red'}).addTo(mymap);
            // zoom the map to the polyline
            mymap.fitBounds(polyline.getBounds());

            //Elevation chart
            var distance = <?php echo json_encode($distance_array); ?>;
            var elevation_points = <?php echo json_encode($activity_elevation_points); ?>;
            var ctx = document.getElementById('elevationChart').getContext('2d');
            var chart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: distance,
                    datasets: [{
                        label: "Elevation (m)",
                        backgroundColor: 'rgb(220, 53, 69)',
                        borderColor: 'rgb(0, 0, 0)',
                        data: elevation_points,
                        pointBackgroundColor: 'rgb(255, 255, 255)'
                    }]
                },
                options: {
                    maintainAspectRatio: false
                }

            });
        }

    </script>

</head>
<body>

<!--Navbar -->
<nav class="mb-1 navbar navbar-expand-lg  navbar-dark bg-dark">
    <a class="navbar-brand" href="index.php">Tracker</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarSupportedContent">
        <ul class="navbar-nav ml-auto">
            <li class="nav-item active">
                <a class="nav-link" href="index.php"> Activities  <span class="badge bg-danger align-text-top"><?php echo $total_activities;?></span></a>
            </li>
            <li class="nav-item ">
                <a class="nav-link" href="contact.php"> <i class="far fa-envelope"></i> Contact </a>
            </li>
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" id="navbarDropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"> <i class="fas fa-user"></i> Profile </a>
                <div class="dropdown-menu dropdown-menu-right dropdown-info" aria-labelledby="navbarDropdownMenuLink">
                    <a class="dropdown-item" href="settings.php">Settings</a>
                    <a class="dropdown-item" href="importgpx.php">Import Activity</a>
                    <a class="dropdown-item" href="logout.php">Log out</a>
                </div>
            </li>
        </ul>
    </div>
</nav>
<!--Navbar -->
<!-- MAP-->
<div class="container">
    <div class="row content panel-title">
        <div class="col-sm-12 text-center">
            <div class="table-responsive-sm">
                <table class="table table-dark">
                    <thead>
                    <tr>
                        <th scope="col"><?php echo ucwords($activity_name);?></th>
                    </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class = "row">
        <div class = "col-sm-12 border border-primary">
            <div id="mapid"></div>
        </div>
    </div>

    <div class="row content panel-title">
        <div class="col-sm-3 text-center">
            <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">Total Time</th>
                    </tr>
                    </thead>
                    <tbody>
                        <tr><td scope="row"><?php echo $total_time." s"; ?></td></tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-sm-3 text-center">
            <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">Total distance</th>
                    </tr>
                    </thead>
                    <tbody>
                        <tr><td scope="row"><?php echo $total_distance." KM"; ?></td></tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="col-sm-3 text-center">
             <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">Avg Peace</th>
                    </tr>
                    </thead>
                    <tbody>
                        <tr><td scope="row"><?php echo $avg_peace." min/km"; ?></td></tr>
                    </tbody>
                </table>
             </div>
        </div>

        <div class="col-sm-3 text-center">
            <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">Max Elevation</th>
                    </tr>
                    </thead>
                    <tbody>
                        <tr><td scope="row"><?php echo $activity_max_elevation." m"; ?></td></tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="elevation_card">
            <canvas id="elevationChart"></canvas>
        </div>
    </div>
    <?php if($img_array){?>
        <div class="carousel slide carousel_container" data-ride="carousel" id="imgCarousel" data-interval="3000">
            <ol class="carousel-indicators">
                <?php
                for($i = 0; $i<sizeof($img_array); $i++){
                    echo "<li data-target=\"#imgCarousel\" data-slide-to=\"".$i."\"";
                    if($i==0) {
                        echo " class=\"active\"";
                    }
                    echo "></li>";
                }
                ?>
            </ol>

            <div class="carousel-inner" role="listbox">
                <?php
                for($i = 0; $i<sizeof($img_array); $i++){
                    echo "<div class=\"carousel-item";
                    if($i==0) {
                        echo " active\"";
                    }
                    echo "\"><img class=\"d-block\" src=\"".$img_array[$i]."\" alt=\"Image ".$i."\">  </div>";
                }
                ?>
            </div>

            <a class="carousel-control-prev" role="button" href="#imgCarousel" data-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="sr-only">Previous</span>
            </a>
            <a class="carousel-control-next" role="button"  href="#imgCarousel" data-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="sr-only">Next</span>
            </a>
        </div>
    <?php } ?>
</div>

    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <!--Chart.js-->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.4.0/Chart.min.js"></script>
    <!--Leaflet-->
    <script src="https://unpkg.com/leaflet@1.4.0/dist/leaflet.js" integrity="sha512-QVftwZFqvtRNi0ZyCtsznlKSWOStnDORoefr1enyq5mVL4tmKB3S/EnC3rRJcxCPavG10IcrVGSmPh6Qw5lwrg==" crossorigin=""></script>
</body>

<!-- Footer -->
<footer>
    <div class="text-center py-3">© 2019 Copyright:
        <a href="index.php"> Tracker </a>
    </div>
</footer>
</html>

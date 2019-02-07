<?php
//https://getbootstrap.com/docs/4.0/getting-started/introduction/
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

    $currentMonthNrDays = date('t');
    $monthArrayLabels = array();
    $monthArrayActivity = array();
    for($i = 0; $i <$currentMonthNrDays; $i++){
        $monthArrayLabels[$i]=$i+1;
        $monthArrayActivity[$i] = 0;
    }
    $activitiesThisMonth =0;
    //Check which activities were tracked in the current month
    foreach ($activitiy_details as $key){
        if(date('m', strtotime($key["time_stamp"])) === date('m')){
            $day = date('j', strtotime($key["time_stamp"]));
            $monthArrayActivity[$day-1] += 1;
            $activitiesThisMonth++;
        }
    }
    //print_r($monthArrayActivity);


}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Home</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/all.css" integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/" crossorigin="anonymous">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <link rel="stylesheet" href="css/style.css">

    <script>
        window.onload = function () {
            var monthdays = <?php echo json_encode($monthArrayLabels); ?>;
            var monthactivity = <?php echo json_encode($monthArrayActivity); ?>;
            var ctx = document.getElementById('activityChart').getContext('2d');
            var chart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: monthdays,
                    datasets: [{
                        label: "Activities",
                        backgroundColor: 'rgb(220, 53, 69)',
                        borderColor: 'rgb(0, 0, 0)',
                        data: monthactivity,
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

<div class="container-fluid text-center">

    <div class="row">
        <div class="col-sm-2 sidenav">

        </div>
        <div class="col-sm-4">
            <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col">Activities this month</th>
                    </tr>
                    </thead>
                </table>
            </div>
            <div class="activity_chart">
                <canvas id="activityChart"></canvas>
            </div>
        </div>
        <div class="col-sm-4">
            <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead-dark">
                    <tr>
                        <th scope="col"> Activities</th>
                    </tr>
                    </thead>
                </table>
            </div>
            <div class="col">
                <div class="row" >
                    <div class="table-responsive-sm" style="width: 50%;">
                        <table class="table table-light">
                            <thead class="thead">
                            <tr>
                                <th scope="col"> This Month</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr><td scope="row"><b><?php echo $activitiesThisMonth;?></b></td></tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="table-responsive-sm" style="width: 50%;">
                        <table class="table table-light">
                            <thead class="thead">
                            <tr>
                                <th scope="col"> Total Activities</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr><td scope="row"><b><?php echo $total_activities;?></b></td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-2 sidenav">

        </div>
    </div>
    <div class="row content">
        <div class="col-sm-2 sidenav">

        </div>
        <div class="col-sm-8 text-center">
            <?php  if($activitiy_details){ ?>
            <div class="table-responsive-sm">
                <table class="table table-light">
                    <thead class="thead">
                    <tr>
                        <th scope="col"> Most recent activities</th>
                    </tr>
                    </thead>
                </table>
            </div>
            <div class="table-responsive-sm">
                <table class="table table-hover table-dark">
                    <thead>
                    <tr>
                        <th scope="col"></th>
                        <th scope="col">Name</th>
                        <th scope="col">Duration</th>
                        <th scope="col">Avg Peace</th>
                        <th scope="col">KM</th>
                        <th scope="col"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php
                        $id=1;
                        $index = count($activitiy_details);
                        while ($index){
                            --$index;
                            $activity_track_id = $activitiy_details[$index]["track_id"];
                            $activity_name = $activitiy_details[$index]["activity_name"];
                            $total_time = $activitiy_details[$index]["total_time"];
                            $avg_time = $activitiy_details[$index]["avg_time"];
                            $total_distance = $activitiy_details[$index]["total_distance"];
                            echo "<tr><td scope=\"row\">".$id++."</td>"."<td>".$activity_name."</td>"."<td>".$total_time."</td>"."<td>".$avg_time."</td>"."<td>".$total_distance."</td>";
                            echo "<td><form action='activity_details.php' method='post'> <button class='btn btn-sm btn-danger' type='submit' name='activity_track_id' value=$activity_track_id>Details</button> </form></td>"."</tr>";
                        }
                    ?>
                    </tbody>
                </table>
            </div>

            <?php } else {?>
                <div class="table-responsive-sm">
                    <table class="table table-light">
                        <thead class="thead">
                        <tr>
                            <th scope="col"> You didn't tracked any activities yet.</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            <?php }?>

        </div>
        <div class="col-sm-2 sidenav">

        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<!--Chart.js-->
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.4.0/Chart.min.js"></script>
</body>
<!-- Footer -->
<footer>
    <div class="text-center py-3">© 2019 Copyright:
        <a href="index.php"> Tracker </a>
    </div>
</footer>
</html>
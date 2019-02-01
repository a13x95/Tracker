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
    //print_r($activitiy_details);
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Home</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/all.css" integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/" crossorigin="anonymous">
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css" integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS" crossorigin="anonymous">
    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js" integrity="sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/js/bootstrap.min.js" integrity="sha384-B0UglyR+jN6CkvvICOB2joaf5I4l3gm9GU6Hc1og6Ls7i6U/mkkaduKaBhlAXv9k" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <link rel="stylesheet" href="css/style.css">

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
    <div class="row content">
        <div class="col-sm-1 sidenav">

        </div>
        <div class="col-sm-10 text-left">
            <?php  if($activitiy_details){ ?>
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
                        foreach ($activitiy_details as $row){
                            $activity_track_id = $row["track_id"];
                            $activity_name = $row["activity_name"];
                            $total_time = $row["total_time"];
                            $avg_time = $row["avg_time"];
                            $total_distance = $row["total_distance"];
                            echo "<tr><td scope=\"row\">".$id++."</td>"."<td>".$activity_name."</td>"."<td>".$total_time."</td>"."<td>".$avg_time."</td>"."<td>".$total_distance."</td>";
                            echo "<td><form action='activity_details.php' method='post'> <button class='btn btn-sm btn-danger' type='submit' name='activity_track_id' value=$activity_track_id>Details</button> </form></td>"."</tr>";
                        }
                    ?>
                    </tbody>
                </table>
            </div>

            <?php } else {echo "User didn't tracked any activities";}?>

        </div>
        <div class="col-sm-1 sidenav">

        </div>
    </div>
</div>
</body>
<!-- Footer -->
<footer>
    <div class="text-center py-3">© 2019 Copyright:
        <a href="index.php"> Tracker </a>
    </div>
</footer>
</html>
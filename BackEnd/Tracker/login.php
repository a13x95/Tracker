<?php
//Initialize session
session_start();

//If user is already logged in -> redirect to homepage
if(isset($_SESSION["loggedin"]) && $_SESSION["loggedin"] === true){
header("location: index.php");
exit();
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>LogIn</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="fontawesome563/css/all.css">
    <link rel="stylesheet" href="bootstrap421/css/bootstrap.min.css">
    <link rel="stylesheet" href="bootstrap421/css/bootstrap.min.js">
    <link rel="stylesheet" href="css/login.css">
</head>
<body>
<div class="login-form">
    <form action="login_user.php" method="post">
        <h2 class="text-center">Log In </h2>
        <hr>
        <div class="form-group row">
            <div class="col-sm-1">
                <i class="fas fa-user" style="padding-top: 12px"></i>
            </div>
            <div class="col-sm-11">
                <input type="email" class="form-control" name="email" placeholder="Enter Email Address" required="required">
            </div>
        </div>

        <div class="form-group row">
            <div class="col-sm-1">
                <i class="fas fa-key" style="padding-top: 12px"></i>
            </div>
            <div class="col-sm-11">
                <input type="password" class="form-control" name="password" placeholder="Enter Password" required="required">
            </div>
        </div>

        <div class="form-group row">
            <div class="col-sm-12">
                <button type="submit" class="btn btn-primary btn-block">Log In <i class="fas fa-user-check" style="padding-left: 5px"></i></button>
            </div>
        </div>
        <div class="row col-sm-12">
            <p class="text-center">
                <?php if (isset($_GET['loginStatus'])){ $message = $_GET['loginStatus']; echo $message;} ?>
            </p>
        </div>
        <div class="clearfix">
            <label class="pull-left checkbox-inline"><input type="checkbox"> Remember me</label>
            <a href="#" class="pull-right">Forgot Password?</a>
        </div>
    </form>
    <p class="text-center"><a href="register.php">Create an Account</a></p>
</div>
</body>
</html>
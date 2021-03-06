<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if($_SERVER["REQUEST_METHOD"] == "POST"){
    if (isset($_POST['email']) && isset($_POST['password'])) {

        // receiving the post params
        $email = $_POST['email'];
        $password = $_POST['password'];

        // get the user by email and password
        $user = $db->getUserByEmailAndPassword($email, $password);

        if ($user != false) {
            // user is found
            $response["error"] = FALSE;
            $response["uid"] = $user["unique_id"];
            $response["user"]["name"] = $user["name"];
            $response["user"]["email"] = $user["email"];
            $response["user"]["created_at"] = $user["created_at"];
            $response["user"]["updated_at"] = $user["updated_at"];

            //User exists - start new session and store data
            session_start();
            $_SESSION["loggedin"] = true;
            $_SESSION["email"] = $email;
            $_SESSION["name"] = $user["name"];
            $_SESSION["user_id"] = $user["unique_id"];

            //Login success - redirect user
            header("location: index.php");
        } else {
            // user is not found with the credentials
            $response["error"] = TRUE;
            $response["error_msg"] = "Login credentials are wrong. Please try again!";
            header("location: login.php?loginStatus=".$response["error_msg"]);
        }
    } else {
        // required post params is missing
        $response["error"] = TRUE;
        $response["error_msg"] = "Required parameters email or password is missing!";
        header("location: login.php?loginStatus=".$response["error_msg"]);
    }
} else{
    $response["error"] = TRUE;
    $response["error_msg"] = "Request method not POST!";
    header("location: login.php?loginStatus=".$response["error_msg"]);
}
?>
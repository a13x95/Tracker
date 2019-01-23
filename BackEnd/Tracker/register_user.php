<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
 
// json response array
$response = array("error" => FALSE);
if($_SERVER["REQUEST_METHOD"] == "POST"){
    if (isset($_POST['name']) && isset($_POST['email']) && isset($_POST['password'])) {

        // receiving the post params
        $name = $_POST['name'];
        $email = $_POST['email'];
        $password = $_POST['password'];

        // check if user is already existed with the same email
        if ($db->isUserExisted($email)) {
            // user already existed
            $response["error"] = TRUE;
            $response["error_msg"] = "User already exists with email address " . $email;
            header("location: register.php?registerStatus=".$response["error_msg"]);
        } else {
            // create a new user
            $user = $db->storeUser($name, $email, $password);
            if ($user) {
                // user stored successfully
                $response["error"] = FALSE;
                $response["uid"] = $user["unique_id"];
                $response["user"]["name"] = $user["name"];
                $response["user"]["email"] = $user["email"];
                $response["user"]["created_at"] = $user["created_at"];
                $response["user"]["updated_at"] = $user["updated_at"];
                header("location: login.php");
            } else {
                // user failed to store
                $response["error"] = TRUE;
                $response["error_msg"] = "Unknown error occurred in registration!";
                header("location: register.php?registerStatus=".$response["error_msg"]);
            }
        }
    } else {
        $response["error"] = TRUE;
        $response["error_msg"] = "Required parameters (name, email or password) are missing!";
        header("location: register.php?registerStatus=".$response["error_msg"]);
    }
} else{
    $response["error"] = TRUE;
    $response["error_msg"] = "Request method not POST!";
    header("location: register.php?registerStatus=".$response["error_msg"]);
}
?>
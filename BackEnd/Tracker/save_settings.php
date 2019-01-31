<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);

if($_SERVER["REQUEST_METHOD"] == "POST"){
    if (isset($_POST['name'])&& isset($_POST['email']) && isset($_POST['current_password']) && isset($_POST['new_password'])&& isset($_POST['confirm_new_password']) && isset($_POST['user_id'])) {

        // receiving the post params
        $name = $_POST['name'];
        $email = $_POST['email'];
        $current_password = $_POST['current_password'];
        $new_password = $_POST['new_password'];
        $confirm_new_password = $_POST['confirm_new_password'];
        $user_id = $_POST['user_id'];
        $update_result = $db->updateInfo($name,$email,$current_password,$new_password,$confirm_new_password, $user_id);
        if(strcmp($update_result["error"], "false")){
            // Initialize the session
            session_start();

            // Unset all of the session variables
            $_SESSION = array();

            // Destroy the session.
            session_destroy();

            // Redirect to login page
            header("location: login.php");
            exit;
        } else {
            header("location: settings.php?saveStatus=".$update_result["error"]);
        }
    }
    else {
        header("location: settings.php?=saveStatus= not all params are set");
    }
}
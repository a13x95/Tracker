package com.licenta.tracker.app;

public class AppConfig {

    public static String NGROK_ADDRESS = "https://7319f7ca.ngrok.io";
    // Server user login url
    public static String URL_LOGIN = NGROK_ADDRESS + "/Tracker/mobile/login.php";

    // Server user register url
    public static String URL_REGISTER = NGROK_ADDRESS + "/Tracker/mobile/register.php";

    // Server Upload JSON data
    public static String URL_SEND_JSON_DATA = NGROK_ADDRESS + "/Tracker/mobile/receive_json_data.php";

    // Server Fetch JSON data
    public static String URL_FETCH_DATA = NGROK_ADDRESS + "/Tracker/mobile/fetch_json_data.php";
}
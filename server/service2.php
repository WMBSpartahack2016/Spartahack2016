<?php

require_once 'vendor/autoload.php';
require_once "db.inc.php";

use transit_realtime\FeedMessage;

$pdo = pdo_connect();
$routeIdQuery = "select route_id, direction from trips where trip_id = ?";
$holder = array();
$returnArray = array();

if(!file_exists("timestamp.txt")){
    $timestampFile = fopen("timestamp.txt", "w");
    fwrite($timestampFile, 0);
    fclose($timestampFile);
}
$timestampFile = fopen("timestamp.txt", "r");
$timestamp = fgets($timestampFile);
fclose($timestampFile);


$currTime = time();

if(($currTime - $timestamp) > 30){
    $timestampFile = fopen("timestamp.txt", "w");
    fwrite($timestampFile, $currTime);
    fclose($timestampFile);
    //echo 'new time';

    $data = get_data('http://developers.cata.org/gtfsrt/vehicle/vehiclepositions.pb');

    $vehPos = fopen("vehiclepositions.txt", "wb");

    fwrite($vehPos, $data);
    fclose($vehPos);

}
else{
    //echo 'Old time';
    $data = fopen("vehiclepositions.txt", "rb");
}


$feed = new FeedMessage();
$feed->parse($data);

foreach ($feed->getEntityList() as $entity) {
    if ($entity->hasTripUpdate()) {
        error_log("trip: " . $entity->getId());
    }

    array_push($holder, $entity->getVehicle()->getTrip()->getTripId());
    $stmt = $pdo->prepare($routeIdQuery);
    $stmt->execute(array($entity->getVehicle()->getTrip()->getTripId()));
    foreach($stmt as $row) {
        //echo "<p>" . substr($row['route_id'],0,2) . "</p>";
        $routeId = substr($row['route_id'],0,2);
        array_push($holder, $routeId, $row['direction']);
    }


    array_push($holder, $entity->getVehicle()->getPosition()->getLongitude(),$entity->getVehicle()->getPosition()->getLatitude());

    array_push($returnArray, $holder);
    $holder = array();
}


    unset($pdo);

    header('Content-type: application/json');
    echo json_encode($returnArray);

/* gets the data from a URL */
function get_data($url) {
    $usrpwd = 'Contact CATA for access credentials';
    $ch = curl_init();
    $timeout = 15;

    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_USERPWD,$usrpwd);
    curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_ANY);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
    $data = curl_exec($ch);
    curl_close($ch);
    return $data;
}
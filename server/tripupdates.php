<?php

require_once 'vendor/autoload.php';

require_once "db.inc.php";
/*
$_POST['lon'] = -84.482537;
$_POST['lan'] = 42.727942;
$_POST['stops'] = array(1697,4537, 1675,1681,1674,1673);
$_POST['gps'] = 1;*/

$pdo = pdo_connect();
$nearStopsQuery = "select stop_id, stop_desc, sqt from (select stop_id, stop_desc, sqrt(POWER((?-stop_lon),2)+POWER((?-stop_lat),2)) as sqt from stops) tab1 order by sqt asc limit 3";
$getStopIdQuery = "select stop_id from stops where stop_code = ?";
$getStopIdQuery2 = "select stop_desc from stops where stop_id = ?";

$content = file_get_contents("php://input");

$content2 = json_decode($content, true);


//print_r($content2);
//var_dump($content2);
//echo '<pre>';
//echo print_r($_POST);

$holder = array();
$holder2 = array();
$returnArray = array();
$stopIdArray = array();

use transit_realtime\FeedMessage;

if(!file_exists("timestamps2.txt")){
    $timestampFile = fopen("timestamps2.txt", "w");
    fwrite($timestampFile, 0);
    fclose($timestampFile);
}
$timestampFile = fopen("timestamps2.txt", "r");
$timestamp = fgets($timestampFile);
fclose($timestampFile);


$currTime = time();

if(($currTime - $timestamp) > 30){
    $timestampFile = fopen("timestamps2.txt", "w");
    fwrite($timestampFile, $currTime);
    fclose($timestampFile);
    //echo 'new time';

    $data = get_data('http://developers.cata.org/gtfsrt/tripupdate/tripupdates.pb');

    $vehPos = fopen("tripupdates.txt", "wb");

    fwrite($vehPos, $data);
    fclose($vehPos);

}
else{
    //echo 'Old time';
    $data = fopen("tripupdates.txt", "rb");
}


$feed = new FeedMessage();
$feed->parse($data);

if($content2["lon"]){

    $stmt = $pdo->prepare($nearStopsQuery);
    $stmt->execute(array($content2["lon"], $content2["lan"]));

    foreach($stmt as $row) {
        $returnArray[$row['stop_id']] = array($row['stop_desc']);
        array_push($stopIdArray,$row['stop_id']);
        //array_push($returnArray, $holder);
        $holder = array();

    }
}

if($content2["stops"]){
    //echo '<pre>'; print_r($_POST['stops']);
    //echo print_r($content["stops"]);
    //$data = json_decode($content, true);
    //echo print_r($data);
    foreach ($content2['stops'] as $stop) {
        //echo 'asdfasdl;fasdklfjasdfkl;sda';
        //echo print_r($content);
        //echo '<pre>'; print_r($stop);
        $stmt = $pdo->prepare($getStopIdQuery2);
        $stmt->execute(array($stop));

        foreach ($stmt as $row) {
            //echo '<pre>'; print_r("sdfsdfsdfsdafasddfasdfasdfsd");
            $returnArray[$stop] = array($row['stop_desc']);
            array_push($stopIdArray, $stop);
            //array_push($returnArray, $holder);
            //$holder = array();
            // print_r($stopIdArray);
        }
    }
}


foreach ($feed->getEntityList() as $entity) {
    if ($entity->hasTripUpdate()) {
        error_log("trip: " . $entity->getId());
    }

    $scheduledStops = array();

    foreach($entity->getTripUpdate()->getStopTimeUpdate() as $entity2) {


        if ($entity->getTripUpdate()->getTrip()->getRouteId()/* === '35'*/) {

            $stopId = 0;
            $stmt2 = $pdo->prepare($getStopIdQuery);
            $stmt2->execute(array($entity2->getStopId()));


            foreach($stmt2 as $row) {
                $stopId = $row['stop_id'];
            }
            //echo '<pre>';
            //print_r($stopId);



            if (!is_null($entity2->getDeparture())) {


                if (in_array($stopId, $stopIdArray)) {

                    array_push($holder2, array($entity->getTripUpdate()->getTrip()->getRouteId(), $entity2->getDeparture()->getTime()));

                    $returnArray[$stopId][1] = $holder2;
                    $holder2 = array();

                 }
            }

        }

    }


}




unset($pdo);
//$returnArray['blah'] = 'wat';
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
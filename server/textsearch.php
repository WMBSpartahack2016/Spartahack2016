<?php

require_once 'vendor/autoload.php';

require_once "db.inc.php";

//$_POST['txtstr'] = "frandor 4537";

$pdo = pdo_connect();

$content = file_get_contents("php://input");

$content2 = json_decode($content, true);

//print_r($content2);

$textMatch = "SELECT stop_id, stop_desc FROM stops WHERE match(stop_name, stop_desc) against(? IN BOOLEAN MODE) LIMIT 5";
$stopIdMatch = "select stop_id, stop_desc from stops where ? like CONCAT('%',stop_id,'%')";
$returnArray = array();

$stmt = $pdo->prepare($stopIdMatch);
$stmt->execute(array($content2['txtstr']));

foreach($stmt as $row) {

    $returnArray[$row['stop_id']] = $row['stop_desc'];

}
$stmt2 = $pdo->prepare($textMatch);
$stmt2->execute(array($content2['txtstr']));

foreach($stmt2 as $row2) {

    $returnArray[$row2['stop_id']] = $row2['stop_desc'];

}


unset($pdo);
header('Content-type: application/json');
echo json_encode($returnArray);


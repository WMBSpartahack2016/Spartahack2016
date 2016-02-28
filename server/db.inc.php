<?php

define('DB_PREFIX', 'p2dev');

function pdo_connect() {
  try {
    // Production Server
    $dbhost="mysql:host=localhost;dbname=spartahack";
    $user="------";
    $password="---------";

      return new PDO($dbhost, $user, $password);
  } catch (PDOException $e) {
    die( "Unable to select database");
  }
}
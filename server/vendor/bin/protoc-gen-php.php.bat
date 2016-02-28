@ECHO OFF
SET BIN_TARGET=%~dp0/../centraldesktop/protobuf-php/protoc-gen-php.php
php -d display_errors=stderr -d log_errors=On -d error_log=Off "%BIN_TARGET%" %*

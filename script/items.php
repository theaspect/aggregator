<?php

//Читаем строку потока
if($stdin_line = fgets(STDIN)) {
    error_log($stdin_line);
    $arOutput = json_decode($stdin_line, TRUE);

    $class = $arOutput["class"];
    error_log("Class: " . $class);

    if($code == "TODO"){
        error_log("TODO");
        $res = json_encode(
            Array()
            )
        );
        fwrite(STDOUT, $res);
    } else {
        error_log("Unrecognized");
        exit(-1);
    }
}
?>

//<?php

/*
1. из stdin возьмет json, любой, но пусть для примера {"hello": ["world"]}
2. вставит его в другой json {"php": {"hello": ["world"]}}
3. получившийся результат напишет в stdout
*/


//Читаем строку потока
if($stdin_line = fgets(STDIN)) {
    $arOutput = json_decode($stdin_line, TRUE);

    //работа с массивом
    $arOutputNew = $arOutput;

    $arResult = Array(
        "php" => $arOutputNew,
    );


    $res = json_encode($arResult);

    //Вывод
    fwrite(STDOUT, $res);
}
//?>

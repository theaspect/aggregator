<?php

function delay($max_ms){
    usleep(rand(0,$max_ms*1000));
}

//Читаем строку потока
if($stdin_line = fgets(STDIN)) {
    error_log($stdin_line);
    $arOutput = json_decode($stdin_line, TRUE);

    $cls = $arOutput["class"];
    error_log("Class: " . $cls);

    if(preg_match('#^normal#', $cls) === 1){
        error_log("normal");
        $res = json_encode(
            Array("items" => Array(
                    Array(
                        "code" => "3311",
                        "brand"=> "ctr",
                        "name"=> "Товар1",
                        "price"=> "123",
                        "quantity"=> "30"
                    ),
                    Array(
                        "code"=> "3302",
                        "brand"=> "bermo",
                        "name"=> "Товар2",
                        "price"=> "120",
                        "quantity"=> "10"
                    ),
                    Array(
                        "code"=> "3333",
                        "brand"=> "kama",
                        "name"=> "Товар3",
                        "price"=> "200",
                        "quantity"=> "4"
                    )
                )
            )
        );
        fwrite(STDOUT, $res);
    } else {
        error_log("Unrecognized");
        exit(-1);
    }

    delay(1000);
}
?>

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

    $res = "";

    if(preg_match('#^normal#', $cls) === 1){
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
    } else if(preg_match('#^analog#', $cls) === 1) {
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
                        ),
                        "suppliers"=> Array(
                            Array(
                                "class"=> "normal-1",
                                "params"=> Array(
                                    "login"=> "log1234",
                                    "password"=> "1234"
                                ),
                                "code"=> "3310",
                                "brand"=> "ctr",
                                "analog"=> "1"
                            ),
                            Array(
                                "class"=> "normal-2",
                                "params"=> Array(
                                    "login"=> "log1234",
                                    "password"=> "1234",
                                    "domain"=> "msk"
                                ),
                                "code"=> "3310",
                                "brand"=> "ctr",
                                "analog"=> "1"
                            )
                        )
                    )
                );
    } else if(preg_match('#^empty#', $cls) === 1) {
        $res = json_encode(
            Array(
                "items" => Array(
                )
            )
        );
    } else if(preg_match('#^timeout#', $cls) === 1) {
        sleep(60);
    } else if(preg_match('#^error#', $cls) === 1) {
        exit(-1);
    } else {
        error_log("Unrecognized Item class");
        exit(-1);
    }

    fwrite(STDOUT, $res);
    delay(1000);
}
?>

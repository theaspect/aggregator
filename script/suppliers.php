<?php

function delay($max_ms){
    usleep(rand(0,$max_ms*1000));
}

//Читаем строку потока
if($stdin_line = fgets(STDIN)) {
    error_log($stdin_line);
    $arOutput = json_decode($stdin_line, TRUE);

    $code = $arOutput["params"]["code"];
    error_log("Code: " . $code);

    //  echo '{"code": "normal","brand": "foo","apikey": "bar","analog": "baz"}' | php script/suppliers.php
    if($code == "normal"){
        error_log("Normal");
        $res = json_encode(
            Array(
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
                    ),
                    Array(
                        "class"=> "normal-3",
                        "params"=> Array(
                            "apikey"=> "4d44cbtf14130d2fsdftpq024kd"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
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

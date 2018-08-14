<?php

//Читаем строку потока
if($stdin_line = fgets(STDIN)) {
    $arOutput = json_decode($stdin_line, TRUE);

    //  echo '{"code": "normal","brand": "foo","apikey": "bar","analog": "baz"}' | php script/suppliers.php
    if($arOutput["code"] == "normal"){
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
    }else{
        exit(-1);
    }
}
?>

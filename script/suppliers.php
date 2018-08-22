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

    $res = "";

    //  echo '{"code": "normal","brand": "foo","apikey": "bar","analog": "baz"}' | php script/suppliers.php
    if($code == "normal"){
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
    } else if($code == "analog"){
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
                        "class"=> "analog-3",
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
    } else if($code == "mixed"){
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
                        "class"=> "analog-2",
                        "params"=> Array(
                            "apikey"=> "4d44cbtf14130d2fsdftpq024kd"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    ),
                    Array(
                        "class"=> "error-3",
                        "params"=> Array(
                            "apikey"=> "4d44cbtf14130d2fsdftpq024kd"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    ),
                     Array(
                         "class"=> "timeout-4",
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
    } else if($code == "emptySuppliers"){
        $res = json_encode(
            Array(
                "suppliers"=> Array(
                )
            )
        );
    } else if($code == "suppliersTimeout"){
        sleep(60); // More the single script allowed
    } else if($code == "suppliersError"){
        delay(1000);
        exit(-1);
    } else if($code == "suppliersNotJson"){
        $res = "Not a JSON";
    } else if($code == "suppliersBadResponse"){
        $res = json_encode(
            Array(
                // Response should have suppliers field
                "not-a-suppliers"=> Array(
                    Array(
                        "class"=> "normal-1",
                        "params"=> Array(
                            "login"=> "log1234",
                            "password"=> "1234"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    )
                )
            )
       );
    } else if($code == "emptyItems"){
        $res = json_encode(
            Array(
                "suppliers"=> Array(
                    Array(
                        "class"=> "empty-1",
                        "params"=> Array(
                            "login"=> "log1234",
                            "password"=> "1234"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    ),
                    Array(
                        "class"=> "empty-2",
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
                        "class"=> "empty-3",
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
    } else if($code == "itemsTimeout"){
        $res = json_encode(
            Array(
                "suppliers"=> Array(
                    Array(
                        "class"=> "timeout-1",
                        "params"=> Array(
                            "login"=> "log1234",
                            "password"=> "1234"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    ),
                    Array(
                        "class"=> "timeout-2",
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
                        "class"=> "timeout-3",
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
    } else if($code == "itemsError"){
        $res = json_encode(
            Array(
                "suppliers"=> Array(
                    Array(
                        "class"=> "error-1",
                        "params"=> Array(
                            "login"=> "log1234",
                            "password"=> "1234"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    ),
                    Array(
                        "class"=> "error-2",
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
    } else if($code == "itemsBadJson"){
        $res = json_encode(
            Array(
                "suppliers"=> Array(
                    Array(
                        "class"=> "badjson-1",
                        "params"=> Array(
                            "login"=> "log1234",
                            "password"=> "1234"
                        ),
                        "code"=> "3310",
                        "brand"=> "ctr",
                        "analog"=> "1"
                    ),
                    Array(
                        "class"=> "badjson-2",
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
    } else if($code == "sessionTimeout"){
        $arr = Array(
                   Array(
                       "class"=> "normal-1",
                       "params"=> Array(
                           "login"=> "log1234",
                           "password"=> "1234"
                       ),
                       "code"=> "3310",
                       "brand"=> "ctr",
                       "analog"=> "1"
                   )
               );
        // Spam with number of suppliers >> pool size (20)
        for ($i = 1; $i <= 2000; $i++) {
            array_push($arr,
                Array(
                    "class"=> "timeout-".$i,
                    "params"=> Array(
                        "login"=> "log1234",
                        "password"=> "1234"
                    ),
                    "code"=> "3310",
                    "brand"=> "ctr",
                    "analog"=> "1"
                )
            );
        }
        $res = json_encode(
                    Array(
                        "suppliers"=>$arr
                    )
                );
    } else {
        error_log("Unrecognized Supplier class");
        exit(-1);
    }

    delay(1000);
    fwrite(STDOUT, $res);
}
?>

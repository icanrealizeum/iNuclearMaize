//run with (echo "<?php";cat ztest6.php |cpp)|php
//" this is for vim

#include "shortdef.php"
#include "debug.php"
#include "dmlL0def.php"
#include "color.php"
#include "dmlL0fun.php"


        beginprogram
        _c( $dc=new dmlL0 );
        //debug_zval_dump($dc);


        if ($dc->fFirstTime) {
                deb(dinfo,"First time run!");
        } else {
                deb(dinfo,"...using prev. defined table");
        }

        //$fp = fopen("debug.php","r");
        _t( $contents=file_get_contents("debug.php") );
        _t( $dc->OpenTransaction() );
        //do {
                //$line = fgets($fp, 1024);//or EOF,EOLN; aka a line not longer than 1024 bytes
                //echo $line;
                $line=$contents;
                $res=split("[ .,/\\\"\?\<\>&!;|\#\$\*\+\{\}=\(\)'`\n\-]",trim($line));
                //$res=split("[ \)\(]",trim($line));
                $i=2;
                foreach ($res as $val) {
                        if (evalgood($val)) {
                                _ifnot( $dc->IsNode($val) ) {
                                        _c( $dc->AddNode($val) );
                                        if ($i<6) {
                                                $i++;
                                        } else {
                                                $i=2;
                                        }
                                } else {
                                        $i=1;
                                }
                                echo setcol($i).$val." ";
                        }
                }
        //} while (evalgood($line) && !feof($fp));
        echo nocol.nl;

        _t( $dc->CloseTransaction() );
        //fclose($fp);


        _c( $res=$dc->IsNode("if") );
        //echo $res.nl;

        _t( $result=$dc->Show() );
        _t( $arr=$result->fetchAll() );
        $count=count($arr);
        deb(dnormal, "$count times.");

        _c( $res=$dc->DelNode("if") );
        //print_r(errorInfo());
        _c( $res=$dc->IsNode("if") );
        //echo $res.nl;
        //debug_zval_dump($dc);

        _t( $result=$dc->Show() );
        _t( $arr=$result->fetchAll() );
        $count=count($arr);
        deb(dnormal, "$count times.");
        /*$count=0;
        while ($row = $result->fetch()) {
                //print_r($row[dNodeName].nl);
                //echo $row['NodeName'].nl;
                $count++;
        }*/

        $dc=null;//ie. dispose()

        endprogram
?>


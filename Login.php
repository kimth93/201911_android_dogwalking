<?php
error_reporting(E_ALL); 
ini_set('display_errors',1); 

include('dbcon2.php');

//POST 값을 읽어온다.
$userid=isset($_POST['userid']) ? $_POST['userid'] : '';
$password = isset($_POST['password']) ? $_POST['password'] : '';

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if ($userid != "" ){ 

    $sql= "SELECT IF(strcmp(password,'$pw'),0,1) FROM people WHERE userid = '$id'";

    $result = mysql_query($sql);

    $stmt = $con->prepare($sql);
    $stmt->execute();

// 쿼리 결과
  if($result)
  {
    $row = mysql_fetch_array($result);
    if(is_null($row[password]))
    {
      echo "Can not find ID";
    }
    else
    {
      echo "$row[password]";   // 0이면 비밀번호 불일치, 1이면 일치
    }
  }
  else
  {
   echo mysql_errno($con);
  }

}

?>

<?php

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if (!$android){
?>

html>
   <body>
   
      <form action="<?php $_PHP_SELF ?>" method="POST">
         나라: <input type = "text" name = "userid" />
         이름: <input type = "text" name = "password" />
         <input type = "submit" />
      </form>
   
   </body>
</html>
<?php
}

   
?>
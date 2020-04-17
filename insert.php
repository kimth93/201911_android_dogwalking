<?php 

    error_reporting(E_ALL); 
    ini_set('display_errors',1); 

    include('dbcon2.php');
    

    if(isset($_SERVER['HTTP_USER_AGENT']))
        $android = strpos($_SERVER['HTTP_USER_AGENT'],'Android');
  

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

        // 안드로이드 코드의 postParameters 변수에 적어준 이름을 가지고 값을 전달 받습니다.
	if(isset($_POST['userid']))
        	$userid=$_POST['userid'];
	if(isset($_POST['password']))
        	$password=$_POST['password'];
	if(isset($_POST['Email']))
		$Email=$_POST['Email'];	

        if(empty($userid)){
            $errMSG = "아이디를 입력하세요.";
        }
        else if(empty($password)){
            $errMSG = "패스워드를 입력하세요.";
        }
        else if(empty($Email)){
	    $errMSG = "이메일을 입력하세요.";
	}

        if(!isset($errMSG)) //모두 입력이 되었다면 
        {
            try{
                // SQL문을 실행하여 데이터를 MySQL 서버의 person 테이블에 저장합니다.

                $stmt = $con->prepare('INSERT INTO people(userid,password,Email) VALUES(:userid, :password,:Email)');
                $stmt->bindParam(':userid', $userid);
                $stmt->bindParam(':password', $password);
                $stmt->bindParam(':Email', $Email);

                if($stmt->execute())
                {
                    $successMSG = "새로운 사용자를 추가했습니다.";
                }
                else
                {
                    $errMSG = "사용자 추가 에러";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage()); 
            }
        }

    }

?>


<?php 
    if (isset($errMSG)) echo $errMSG;
    if (isset($successMSG)) echo $successMSG;

	$android = strpos($_SERVER['HTTP_USER_AGENT'], 'Android');
   
    if( !$android )
    {
?>
    <html>
       <body>

            <form action="<?php $_PHP_SELF ?>" method="POST">
                userid: <input type = "text" name = "userid" />
                password: <input type = "text" name = "password" />
		email: <input type = "text" name = "Email" />
                <input type = "submit" name = "submit" />
            </form>
       
       </body>
    </html>

<?php 
    }
?>
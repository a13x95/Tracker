<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Register</title>

  <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.6.3/css/all.css" integrity="sha384-UHRtZLI+pbxtHCWp1t77Bi1L4ZtiqrqD80Kn4Z8NTSRyMA2Fd33n5dQ8lWUE00s/" crossorigin="anonymous">
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/css/bootstrap.min.css" integrity="sha384-GJzZqFGwb1QTTN6wy59ffF1BuGJpLSa9DkKMp0DgiMDm4iYMj70gZWKYbI706tWS" crossorigin="anonymous">
    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.6/umd/popper.min.js" integrity="sha384-wHAiFfRlMFy6i5SRaxvfOCifBUQy1xHdJ/yoi7FRNXMRBu5WHdZYu1hA6ZOblgut" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.2.1/js/bootstrap.min.js" integrity="sha384-B0UglyR+jN6CkvvICOB2joaf5I4l3gm9GU6Hc1og6Ls7i6U/mkkaduKaBhlAXv9k" crossorigin="anonymous"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
  <link rel="stylesheet" href="css/register.css">

  <script src="bootstrap421/js/bootstrap.min.js"></script>
</head>
<body>
<div class="signup-form">
  <form action="register_user.php" method="post">
    <h2 class="text-center">Sign Up</h2>
    <p>Please fill in this form to create an account!</p>
    <hr>
    <div class="form-group row">
      <div class="col-sm-1">
        <i class="fas fa-user" style="padding-top: 12px"></i>
      </div>
      <div class="col-sm-11">
        <input type="text" class="form-control" name="name" placeholder="Full Name" required="required">
      </div>
    </div>
    <div class="form-group row">
      <div class="col-sm-1">
        <i class="fas fa-at" style="padding-top: 12px"></i>
      </div>
      <div class="col-sm-11">
        <input type="email" class="form-control" name="email" placeholder="Email" required="required">
      </div>
    </div>
    <div class="form-group row">
      <div class="col-sm-1">
        <i class="fas fa-key" style="padding-top: 12px"></i>
      </div>
      <div class="col-sm-11">
        <input type="password" class="form-control" name="password" placeholder="Password" required="required">
      </div>
    </div>
    <!--
    <div class="form-group row">
      <div class="col-sm-1">
        <i class="fas fa-key" style="padding-top: 12px"></i>
      </div>
      <div class="col-sm-11">
        <input type="password" class="form-control" name="confirm_password" placeholder="Confirm Password" required="required">
      </div>
    </div>
    -->
    <div class="form-group row">
      <div class="col-sm-6">
        <button type="submit" class="btn btn-primary btn-lg"><i class="fas fa-plus" style="padding-right: 5px"></i> Sign Up</button>
      </div>
      <div class="col-sm-6">
        <button type="reset" class="btn btn-primary btn-lg">Reset <i class="fas fa-user-times" style="padding-left: 5px"></i> </button>
      </div>
    </div>
      <div class="row col-sm-12">
          <p class="text-center" style="color: rgba(220,18,24,0.68)">
              <?php if (isset($_GET['registerStatus'])){ $message = $_GET['registerStatus']; echo $message;} ?>
          </p>
      </div>
  </form>
  <div class="hint-text">Already have an account? <a href="login.php">Login here</a></div>
</div>
</body>
</html>
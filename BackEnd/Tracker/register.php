<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Register</title>

  <meta name="viewport" content="width=device-width, initial-scale=1">

  <link rel="stylesheet" href="fontawesome563/css/all.css">
  <link rel="stylesheet" href="bootstrap421/css/bootstrap.min.css">
  <link rel="stylesheet" href="bootstrap421/css/bootstrap.min.js">
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
  </form>
  <div class="hint-text">Already have an account? <a href="login.php">Login here</a></div>
</div>
</body>
</html>
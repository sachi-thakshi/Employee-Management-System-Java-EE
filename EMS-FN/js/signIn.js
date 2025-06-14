$('#sign-in-btn').on('click', function() {
    // console.log('Sign In button clicked');
    var email = $('#email').val();
    var password = $('#password').val();
    $.ajax({
        type: 'POST',
        url: 'http://localhost:8080/EMS_Web_exploded/signin',
        data: JSON.stringify({
            UserEmail: email,
            UserPassword: password
        }),
        contentType: 'application/json',
        success: function(response) {
            if (response.code === '200') {
                localStorage.setItem('email', email);
                // alert('Sign in successful!');
                window.location.href = '/pages/dashBoard.html';
            } else {
                alert('Sign in failed: ' + response.message);
            }
        }
   });
});
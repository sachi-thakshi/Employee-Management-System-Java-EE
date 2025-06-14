$('#sign-up-btn').on('click', function() {
    // console.log('Sign Up button clicked');
    var name = $('#name').val();
    var email = $('#email').val();
    var password = $('#password').val();
    
    $.ajax({
        type: 'POST',
        url: 'http://localhost:8080/EMS_Web_exploded/signup',
        data: JSON.stringify({
            UserName: name,
            UserPassword: email,
            UserEmail: password
        }),
        contentType: 'application/json',
        success: function(response) {
            if (response.code === '200') {
                alert('Sign up successful!');
                window.location.href = 'signIn.html';
            } else {
                alert('Sign up failed: ' + response.message);
            }
        },
    });
});
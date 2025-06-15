$(document).ready(function() {
    $('#update-employee').hide();

    // Check if user is logged in
    var email = localStorage.getItem('email');
    if (!email) {
        window.location.href = 'signin.html';
    } else {
        $('#welcome-message').text('Welcome, ' + email);

        fetchEmployees();
    }
});

$('#save-employee').on('click', function () {
    const formData = new FormData();

    formData.append('ename', $('#ename').val());
    formData.append('enumber', $('#enumber').val());
    formData.append('eaddress', $('#eaddress').val());
    formData.append('edepartment', $('#edepartment').val());
    formData.append('estatus', $('#estatus').val());

    const imageFile = $('#eimage')[0];
    if (imageFile.files.length > 0) {
        formData.append('eimage', imageFile.files[0]);
    }else{
        alert("Please select an image to upload.");
        return;
    }

    $.ajax({
        method: 'POST',
        url: 'http://localhost:8080/EMS_Web_exploded/employee',
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
            if (response.code === '200') {
                alert('Employee saved successfully!');
                window.location.reload();
            } else {
                alert('Failed to save employee: ' + response.message);
            }
        },
        error: function () {
            alert('Failed to save employee.');
        }
    });
});


$('#update-employee').on('click', function () {
    const formData = new FormData();

    const eid = $('#eid').val();
    const ename = $('#ename').val();
    const enumber = $('#enumber').val();
    const eaddress = $('#eaddress').val();
    const edepartment = $('#edepartment').val();
    const estatus = $('#estatus').val();
    const imageFile = $('#eimage')[0].files[0];

    if (!eid) {
        alert('Employee ID is required for update.');
        return;
    }

    formData.append('eid', eid);
    formData.append('ename', ename);
    formData.append('enumber', enumber);
    formData.append('eaddress', eaddress);
    formData.append('edepartment', edepartment);
    formData.append('estatus', estatus);

    if (imageFile) {
        formData.append('eimage', imageFile);
    }

    formData.append('_method', 'PUT');

    $.ajax({
        type: 'PUT',
        url: 'http://localhost:8080/EMS_Web_exploded/employee?_method=PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
            if (response.code === '200') {
                alert('Employee updated successfully!');
                window.location.reload();
            } else {
                alert('Failed to update employee: ' + response.message);
            }
        },
        error: function () {
            alert('Failed to update employee.');
        }
    });
});


function fetchEmployees() {
    $.ajax({
        method: 'GET',
        url: 'http://localhost:8080/EMS_Web_exploded/employee',
        success: function(response) {
            if (response.code === '200') {
                var employees = response.data;
                var employeeTable = $('#employee-table-body');
                employeeTable.empty(); // Clear existing rows
                employees.forEach(function(employee) {
                    employeeTable.append(
                        `<tr>
                            <td class="d-none">${employee.eid}</td>
                            <td>
                                <button class="btn btn-primary" onclick="editEmployee('${employee.eid}')">Edit</button>
                                <button class="btn btn-danger" onclick="deleteEmployee('${employee.eid}')">Delete</button>
                            </td>
                            <td>${employee.ename}</td>
                            <td>${employee.enumber}</td>
                             <td>${employee.eaddress}</td>
                            <td>${employee.edepartment}</td>
                            <td>${employee.estatus}</td>
                            <td>
                                <img src="/assets/images/${employee.eimage}" alt="Employee Image" width="60" height="60" />
                            </td>
                        </tr>`
                    );
                });
            } else {
                alert('Error fetching employees: ' + response.message);
            }
        },
        error: function() {
            alert('Failed to fetch employees.');
        }
    });
}

function editEmployee(eid) {
    $.ajax({
        method: 'GET',
        url: `http://localhost:8080/EMS_Web_exploded/employee/${eid}`,
        success: function(response) {
            if (response.code === '200') {
                var employee = response.data;

                $('#eid').val(employee.eid);
                $('#ename').val(employee.ename);
                $('#enumber').val(employee.enumber);
                $('#eaddress').val(employee.eaddress);
                $('#edepartment').val(employee.edepartment);
                $('#estatus').val(employee.estatus);

                // Set existing image path in a hidden field
                $('#current-eimage').val(employee.eimage); // hidden input field
                $('#image-preview').attr('src', `/assets/images/${employee.eimage}`).show(); // preview

                $('#update-employee').show();
                $('#save-employee').hide();
            } else {
                alert('Employee not found: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('AJAX Error:', status, error);
            alert('Failed to fetch employee details.');
        }
    });
}


function deleteEmployee(eid) {
    if (!confirm('Are you sure you want to delete this employee?')) {
        return;
    }

    $.ajax({
        type: 'DELETE',
        url: `http://localhost:8080/EMS_Web_exploded/employee/${eid}`,
        success: function(response) {
            if (response.code === '200') {
                alert('Employee deleted successfully!');
                fetchEmployees();  // refresh the list
            } else {
                alert('Failed to delete employee: ' + response.message);
            }
        },
        error: function() {
            alert('Failed to delete employee.');
        }
    });
}


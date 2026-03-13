function goToClient() {
    document.getElementById('status').innerText = 'Opening Client Dashboard... (demo)';
    // Later: window.location.href = '/client.html' or fetch API
}

function goToConsultant() {
    document.getElementById('status').innerText = 'Opening Consultant Dashboard...';
}

function goToAdmin() {
    document.getElementById('status').innerText = 'Opening Admin Panel...';
}
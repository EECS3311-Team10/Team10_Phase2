// function goToClient() {
//     document.getElementById('status').innerText = 'Opening Client Dashboard... (demo)';
//     // Later: window.location.href = '/client.html' or fetch API
// }

// function goToConsultant() {
//     document.getElementById('status').innerText = 'Opening Consultant Dashboard...';
// }

// function goToAdmin() {
//     document.getElementById('status').innerText = 'Opening Admin Panel...';
// }

async function login() {
    const userId = document.getElementById("userId").value.trim();
    const password = document.getElementById("password").value.trim(); // placeholder for now
    const status = document.getElementById("status");

    if (!userId) {
        status.innerText = "Please enter your User ID.";
        status.style.color = "red";
        return;
    }

    try {
        const response = await fetch(`/users/${userId}`);

        if (!response.ok) {
            status.innerText = "User not found.";
            status.style.color = "red";
            return;
        }

        const user = await response.json();

        // store basic info so next pages can use it
        localStorage.setItem("userId", user.userID || user.userId);
        localStorage.setItem("userName", user.name);
        localStorage.setItem("userRole", user.role);

        status.innerText = `Logging in as ${user.role}...`;
        status.style.color = "green";

        if (user.role === "Client") {
            window.location.href = "client/client.html";
        } else if (user.role === "Consultant") {
            window.location.href = "consultant/consultant.html";
        } else if (user.role === "Admin") {
            window.location.href = "admin/admin.html";
        } else {
            status.innerText = "Unknown role.";
            status.style.color = "red";
        }

    } catch (error) {
        console.error(error);
        status.innerText = "Server error. Please try again.";
        status.style.color = "red";
    }
}
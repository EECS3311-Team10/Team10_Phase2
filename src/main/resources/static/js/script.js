async function login() {
    const userIdInput = document.getElementById("userId").value.trim();
    const passwordInput = document.getElementById("password").value.trim();
    const status = document.getElementById("status");

    if (!userIdInput || !passwordInput) {
        status.innerText = "Missing User ID or Password.";
        status.style.color = "red";
        return;
    }

    try {
        const response = await fetch('/api/users/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: userIdInput,
                password: passwordInput
            })
        });

        if (response.ok) {
            const user = await response.json();
            
            // --- THE MISSING LINK: Save data for the dashboard to see ---
            localStorage.setItem("userId", user.userId || user.userID);
            localStorage.setItem("userName", user.name);
            localStorage.setItem("userRole", user.role); // e.g., "ADMIN"

            status.innerText = "Success! Entering dashboard...";
            status.style.color = "green";

            const role = user.role.toUpperCase();

            if (role === "ADMIN") {
                window.location.href = "/admin/admin.html";
            } else if (role === "CONSULTANT") {
                window.location.href = "/consultant/consultant.html";
            } else if (role === "CLIENT") {
                window.location.href = "/client/client.html";
            } else {
                status.innerText = "Role not recognized: " + user.role;
            }
        
        } else {
            status.innerText = "Invalid Login ID or Password.";
            status.style.color = "red";
        }
    } catch (error) {
        console.error("Connection Error:", error);
        status.innerText = "Cannot connect to server. Is Docker up?";
        status.style.color = "red";
    }
}



/* function goToClient() {
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
    */
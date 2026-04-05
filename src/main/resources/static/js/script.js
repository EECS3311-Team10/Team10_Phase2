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
        const response = await fetch("/api/users/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                userId: userIdInput,
                password: passwordInput
            })
        });

        if (response.ok) {
            const user = await response.json();

            localStorage.setItem("userId", user.userId || user.userID);
            localStorage.setItem("userName", user.name);
            localStorage.setItem("userRole", user.role);

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
                status.style.color = "red";
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
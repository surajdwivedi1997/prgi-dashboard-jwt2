// ✅ Redirect to login if no token
if (!window.location.pathname.includes("login.html")) {
    const token = localStorage.getItem("accessToken");
    if (!token) {
        window.location.href = "login.html";
    }
}

// ✅ Handle login form
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();

            try {
                const res = await fetch("http://localhost:8080/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email, password })
                });

                if (res.ok) {
                    const data = await res.json();

                    if (data.accessToken && data.refreshToken) {
                        // ✅ Save both tokens
                        localStorage.setItem("accessToken", data.accessToken);
                        localStorage.setItem("refreshToken", data.refreshToken);

                        // ✅ Redirect to dashboard
                        window.location.replace("index.html");
                    } else {
                        document.getElementById("loginError").style.display = "block";
                    }
                } else {
                    document.getElementById("loginError").style.display = "block";
                }
            } catch (err) {
                console.error("Login failed:", err);
                document.getElementById("loginError").style.display = "block";
            }
        });
    }

    // ✅ Logout
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            window.location.href = "login.html";
        });
    }
});

// ✅ Helper fetch with JWT
async function authFetch(url, options = {}) {
    const token = localStorage.getItem("accessToken");
    if (!options.headers) options.headers = {};
    if (token) options.headers["Authorization"] = `Bearer ${token}`;
    return fetch(url, options);
}
/**
 * =========================================================================
 * AUTHENTICATION MODULE: Login, Registration, JWT handling
 * =========================================================================
 */

const Auth = {
    init() {
        this.bindEvents();
        this.updateNavProfile();

        // Listen for 401 unauthorized events
        window.addEventListener("auth:unauthorized", () => {
            this.showLoginModal();
        });

        // If not logged in, prompt login
        if (!API.getToken()) {
            this.showLoginModal();
        }
    },

    bindEvents() {
        const loginForm = document.getElementById("loginForm");
        if (loginForm) {
            loginForm.addEventListener("submit", (e) => this.handleLogin(e));
        }

        const registerForm = document.getElementById("registerForm");
        if (registerForm) {
            registerForm.addEventListener("submit", (e) => this.handleRegister(e));
        }

        const logoutBtn = document.getElementById("logoutBtn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", () => this.handleLogout());
        }
    },

    showLoginModal() {
        const modalEl = document.getElementById("authModal");
        if (modalEl) {
            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    },

    hideLoginModal() {
        const modalEl = document.getElementById("authModal");
        if (modalEl) {
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
        }
    },

    async handleLogin(e) {
        e.preventDefault();
        const usernameInput = document.getElementById("loginUsername").value.trim();
        const passwordInput = document.getElementById("loginPassword").value;
        const alertBox = document.getElementById("loginAlert");

        alertBox.classList.add("d-none");

        try {
            const res = await API.post("/api/auth/login", {
                username: usernameInput,
                password: passwordInput
            });

            if (res.success && res.data) {
                API.setToken(res.data.token);
                API.setUser({
                    id: res.data.id,
                    username: res.data.username,
                    email: res.data.email,
                    role: res.data.role
                });

                API.showToast("Signed in successfully as " + res.data.username, "success");
                this.updateNavProfile();
                this.hideLoginModal();

                // Trigger reload of dashboard
                if (window.Dashboard) {
                    Dashboard.loadSummary();
                }
            }
        } catch (err) {
            alertBox.innerText = err.message || "Invalid credentials. Please try again.";
            alertBox.classList.remove("d-none");
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        const username = document.getElementById("regUsername").value.trim();
        const email = document.getElementById("regEmail").value.trim();
        const password = document.getElementById("regPassword").value;
        const alertBox = document.getElementById("registerAlert");

        alertBox.classList.add("d-none");

        try {
            const res = await API.post("/api/auth/register", { username, email, password });
            if (res.success && res.data) {
                API.setToken(res.data.token);
                API.setUser({
                    id: res.data.id,
                    username: res.data.username,
                    email: res.data.email,
                    role: res.data.role
                });

                API.showToast("Account registered! Logged in as " + res.data.username, "success");
                this.updateNavProfile();
                this.hideLoginModal();

                if (window.Dashboard) {
                    Dashboard.loadSummary();
                }
            }
        } catch (err) {
            alertBox.innerText = err.message || "Registration failed. Try a different username.";
            alertBox.classList.remove("d-none");
        }
    },

    async handleLogout() {
        try {
            await API.post("/api/auth/logout", {});
        } catch (e) {
            // Ignore logout errors
        }
        API.setToken(null);
        API.setUser(null);
        API.showToast("Logged out successfully", "info");
        this.updateNavProfile();
        this.showLoginModal();
    },

    updateNavProfile() {
        const user = API.getUser();
        const profileContainer = document.getElementById("navUserProfile");
        const loginPromptBtn = document.getElementById("navLoginBtn");

        if (user && API.getToken()) {
            if (profileContainer) {
                profileContainer.classList.remove("d-none");
                document.getElementById("navUsername").innerText = user.username;
                const roleBadge = document.getElementById("navUserRole");
                if (roleBadge) {
                    roleBadge.innerText = user.role.replace("ROLE_", "");
                    roleBadge.className = `badge ${user.role === 'ROLE_ADMIN' ? 'bg-danger' : 'bg-primary'}`;
                }
            }
            if (loginPromptBtn) loginPromptBtn.classList.add("d-none");
        } else {
            if (profileContainer) profileContainer.classList.add("d-none");
            if (loginPromptBtn) loginPromptBtn.classList.remove("d-none");
        }
    }
};

document.addEventListener("DOMContentLoaded", () => Auth.init());

/**
 * =========================================================================
 * API CLIENT: Fetch wrapper with JWT authentication and toast alerts
 * =========================================================================
 */

const API = {
    baseUrl: window.location.origin,

    getToken() {
        return localStorage.getItem("jwt_token");
    },

    setToken(token) {
        if (token) {
            localStorage.setItem("jwt_token", token);
        } else {
            localStorage.removeItem("jwt_token");
        }
    },

    getUser() {
        const u = localStorage.getItem("user_profile");
        return u ? JSON.parse(u) : null;
    },

    setUser(user) {
        if (user) {
            localStorage.setItem("user_profile", JSON.stringify(user));
        } else {
            localStorage.removeItem("user_profile");
        }
    },

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const headers = {
            "Content-Type": "application/json",
            "Accept": "application/json",
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, { ...options, headers });
            
            if (response.status === 401) {
                // Unauthorized - prompt login
                this.setToken(null);
                this.setUser(null);
                window.dispatchEvent(new CustomEvent("auth:unauthorized"));
                throw new Error("Session expired or unauthorized. Please login.");
            }

            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.message || `HTTP ${response.status}: Request failed`);
            }
            return data;
        } catch (err) {
            console.error(`[API ERROR] ${endpoint}:`, err);
            throw err;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: "GET" });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: "POST",
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: "PUT",
            body: JSON.stringify(body)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: "DELETE" });
    },

    showToast(message, type = "info") {
        const toastEl = document.getElementById("liveToast");
        const toastBody = document.getElementById("toastMessage");
        const toastTitle = document.getElementById("toastTitle");
        
        if (!toastEl) return;

        toastBody.innerText = message;
        toastTitle.innerText = type === "success" ? "Success" : type === "error" ? "Notice" : "Information";
        
        toastEl.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type === 'success' ? 'success' : 'primary'} border-0`;
        const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
        toast.show();
    }
};

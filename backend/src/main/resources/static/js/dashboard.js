/**
 * =========================================================================
 * DASHBOARD CONTROLLER: Real-time telemetry, toggle actuation, All-Off
 * =========================================================================
 */

const Dashboard = {
    pollTimer: null,
    pendingDeviceToggles: new Set(),

    init() {
        this.bindEvents();
        this.loadSummary();
        this.startPolling();
    },

    bindEvents() {
        const refreshBtn = document.getElementById("refreshDashboardBtn");
        if (refreshBtn) {
            refreshBtn.addEventListener("click", () => this.loadSummary());
        }

        const confirmAllOffBtn = document.getElementById("confirmAllOffBtn");
        if (confirmAllOffBtn) {
            confirmAllOffBtn.addEventListener("click", () => this.executeAllOff());
        }
    },

    startPolling() {
        if (this.pollTimer) clearInterval(this.pollTimer);
        this.pollTimer = setInterval(() => {
            if (API.getToken()) {
                this.loadSummary(true); // silent background tick
            }
        }, 2500);
    },

    async loadSummary(isSilent = false) {
        if (!API.getToken()) return;

        try {
            const res = await API.get("/api/dashboard/summary");
            if (res.success && res.data) {
                this.renderMetrics(res.data);
                this.renderDeviceCards(res.data.devices);
                document.getElementById("lastSyncTime").innerText = new Date().toLocaleTimeString();
            }
        } catch (err) {
            if (!isSilent) {
                console.warn("[Dashboard] Error loading summary:", err);
            }
        }
    },

    renderMetrics(data) {
        document.getElementById("metricTotal").innerText = data.totalDevices;
        document.getElementById("metricOnline").innerText = data.onlineDevices;
        document.getElementById("metricActive").innerText = data.activeDevices;
        document.getElementById("metricOffline").innerText = data.offlineDevices;

        const hwBadge = document.getElementById("hwStatusBadge");
        if (hwBadge) {
            if (data.onlineDevices > 0) {
                hwBadge.innerHTML = `<span class="pulse-online me-1"></span> ESP32 Connected (${data.onlineDevices} Online)`;
                hwBadge.className = "badge bg-success-subtle text-success border border-success-subtle";
            } else {
                hwBadge.innerHTML = `<span class="pulse-offline me-1"></span> ESP32 Disconnected (Offline)`;
                hwBadge.className = "badge bg-danger-subtle text-danger border border-danger-subtle";
            }
        }
    },

    renderDeviceCards(devices) {
        const container = document.getElementById("deviceCardsContainer");
        if (!container) return;

        if (!devices || devices.length === 0) {
            container.innerHTML = `
                <div class="col-12 text-center py-5">
                    <p class="text-secondary">No devices registered in the system yet.</p>
                </div>
            `;
            return;
        }

        let html = "";
        devices.forEach(device => {
            const isOn = device.confirmedState === "ON";
            const isOnline = device.isOnline;
            const isPending = this.pendingDeviceToggles.has(device.id);
            const isFan = device.type === "FAN";

            const iconClass = isFan ? "bi-fan" : "bi-lightbulb-fill";
            const iconWrapperType = isFan ? "fan-icon" : "light-icon";
            const spinningClass = (isFan && isOn) ? "fan-spinning" : "";

            html += `
                <div class="col-12 col-md-6 col-xl-3 mb-4">
                    <div class="glass-card device-card ${isOn ? 'is-on' : ''}">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div class="device-icon-wrapper ${iconWrapperType}">
                                <i class="bi ${iconClass} ${spinningClass}"></i>
                            </div>
                            <div class="d-flex flex-column align-items-end">
                                <label class="switch-large mb-1">
                                    <input type="checkbox" id="toggle_${device.id}" 
                                        ${isOn ? 'checked' : ''} 
                                        ${(!device.enabled || isPending) ? 'disabled' : ''}
                                        onchange="Dashboard.toggleDevice(${device.id}, this.checked)">
                                    <span class="slider-round"></span>
                                </label>
                                <span class="badge ${isOnline ? 'bg-success' : 'bg-secondary'} badge-custom mt-1">
                                    <span class="${isOnline ? 'pulse-online' : 'pulse-offline'} me-1"></span>
                                    ${isOnline ? 'ONLINE' : 'OFFLINE'}
                                </span>
                            </div>
                        </div>

                        <h5 class="fw-bold mb-1">${escapeHtml(device.name)}</h5>
                        <p class="text-secondary small mb-2">
                            <i class="bi bi-geo-alt"></i> ${escapeHtml(device.roomName || 'General')} &bull; 
                            <span class="pin-badge">GPIO ${device.pinNumber} (CH${device.relayChannel})</span>
                        </p>

                        <div class="p-2 rounded bg-dark bg-opacity-25 border border-secondary border-opacity-25 mt-3">
                            <div class="d-flex justify-content-between small">
                                <span class="text-secondary">Confirmed State:</span>
                                <span class="fw-bold ${isOn ? 'text-success' : 'text-danger'}">
                                    ${isPending ? '<span class="spinner-border spinner-border-sm me-1"></span> Pending...' : device.confirmedState}
                                </span>
                            </div>
                            ${!isOnline ? `
                            <div class="small text-warning mt-1" style="font-size: 0.76rem;">
                                <i class="bi bi-exclamation-triangle"></i> Hardware unreachable. Showing last known state.
                            </div>
                            ` : ''}
                        </div>

                        ${isFan ? `
                        <div class="mt-2 text-secondary" style="font-size: 0.72rem;">
                            <i class="bi bi-shield-check"></i> Relay Actuated (Safe ON/OFF logic)
                        </div>
                        ` : ''}
                    </div>
                </div>
            `;
        });

        container.innerHTML = html;
    },

    async toggleDevice(deviceId, shouldTurnOn) {
        const targetState = shouldTurnOn ? "ON" : "OFF";
        this.pendingDeviceToggles.add(deviceId);
        this.loadSummary(true);

        try {
            const res = await API.post(`/api/devices/${deviceId}/commands`, {
                requestedState: targetState
            });

            if (res.success) {
                API.showToast(`Command sent: Turn ${targetState}. Awaiting ESP32 acknowledgement...`, "info");
                
                // Poll quickly for acknowledgement reflection
                setTimeout(() => {
                    this.pendingDeviceToggles.delete(deviceId);
                    this.loadSummary(true);
                }, 1800);
            }
        } catch (err) {
            this.pendingDeviceToggles.delete(deviceId);
            API.showToast(err.message || "Failed to dispatch command to hardware", "error");
            this.loadSummary(true);
        }
    },

    async executeAllOff() {
        const modalEl = document.getElementById("allOffConfirmModal");
        if (modalEl) {
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
        }

        try {
            const res = await API.post("/api/commands/all-off", {});
            if (res.success) {
                API.showToast("All-Off sequence dispatched to all channels!", "success");
                setTimeout(() => this.loadSummary(true), 1500);
            }
        } catch (err) {
            API.showToast(err.message || "Failed to execute All-Off command", "error");
        }
    }
};

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/[&<>"']/g, function(m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}

document.addEventListener("DOMContentLoaded", () => Dashboard.init());

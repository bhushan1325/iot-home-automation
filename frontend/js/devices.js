/**
 * =========================================================================
 * DEVICE MANAGEMENT MODULE: Add, Edit, Delete, Enable/Disable, Safe Pins
 * =========================================================================
 */

const DeviceManager = {
    devicesList: [],

    init() {
        this.bindEvents();
        const tabEl = document.getElementById("devices-tab");
        if (tabEl) {
            tabEl.addEventListener("shown.bs.tab", () => this.loadDevices());
        }
    },

    bindEvents() {
        const addForm = document.getElementById("addDeviceForm");
        if (addForm) {
            addForm.addEventListener("submit", (e) => this.handleAddDevice(e));
        }

        const editForm = document.getElementById("editDeviceForm");
        if (editForm) {
            editForm.addEventListener("submit", (e) => this.handleUpdateDevice(e));
        }
    },

    async loadDevices() {
        if (!API.getToken()) return;

        try {
            const res = await API.get("/api/devices");
            if (res.success && res.data) {
                this.devicesList = res.data;
                this.renderTable(res.data);
            }
        } catch (err) {
            console.error("Failed to load devices:", err);
            API.showToast("Could not load device records", "error");
        }
    },

    renderTable(devices) {
        const tbody = document.getElementById("devicesTableBody");
        if (!tbody) return;

        const user = API.getUser();
        const isAdmin = user && user.role === "ROLE_ADMIN";

        if (devices.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-secondary">No devices configured</td></tr>`;
            return;
        }

        let html = "";
        devices.forEach(d => {
            html += `
                <tr>
                    <td class="fw-bold">${escapeHtml(d.deviceKey)}</td>
                    <td>${escapeHtml(d.name)}</td>
                    <td><span class="badge bg-secondary-subtle text-secondary">${escapeHtml(d.roomName || 'General')}</span></td>
                    <td>${escapeHtml(d.type)}</td>
                    <td><span class="pin-badge">GPIO ${d.pinNumber}</span></td>
                    <td>CH ${d.relayChannel}</td>
                    <td>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" role="switch" 
                                ${d.enabled ? 'checked' : ''} 
                                onchange="DeviceManager.toggleEnabled(${d.id}, this.checked)">
                        </div>
                    </td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-info me-1" onclick="DeviceManager.openEditModal(${d.id})">
                            <i class="bi bi-pencil"></i>
                        </button>
                        ${isAdmin ? `
                        <button class="btn btn-sm btn-outline-danger" onclick="DeviceManager.deleteDevice(${d.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                        ` : ''}
                    </td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
    },

    async toggleEnabled(deviceId, isEnabled) {
        const device = this.devicesList.find(d => d.id === deviceId);
        if (!device) return;

        try {
            await API.put(`/api/devices/${deviceId}`, {
                name: device.name,
                roomId: device.roomId,
                type: device.type,
                pinNumber: device.pinNumber,
                relayChannel: device.relayChannel,
                enabled: isEnabled
            });
            API.showToast(`Device ${isEnabled ? 'enabled' : 'disabled'} successfully`, "success");
            this.loadDevices();
        } catch (err) {
            API.showToast(err.message || "Failed to update enabled status", "error");
            this.loadDevices();
        }
    },

    async handleAddDevice(e) {
        e.preventDefault();
        const deviceKey = document.getElementById("addDeviceKey").value.trim();
        const name = document.getElementById("addDeviceName").value.trim();
        const roomId = parseInt(document.getElementById("addDeviceRoom").value);
        const type = document.getElementById("addDeviceType").value;
        const pinNumber = parseInt(document.getElementById("addDevicePin").value);
        const relayChannel = parseInt(document.getElementById("addDeviceRelayCh").value);
        const deviceSecret = document.getElementById("addDeviceSecret").value.trim() || undefined;

        try {
            const res = await API.post("/api/devices", {
                deviceKey, name, roomId, type, pinNumber, relayChannel, deviceSecret
            });

            if (res.success) {
                API.showToast("New device configured successfully!", "success");
                const modalEl = document.getElementById("addDeviceModal");
                bootstrap.Modal.getInstance(modalEl).hide();
                document.getElementById("addDeviceForm").reset();
                this.loadDevices();
            }
        } catch (err) {
            API.showToast(err.message || "Failed to register device", "error");
        }
    },

    openEditModal(deviceId) {
        const device = this.devicesList.find(d => d.id === deviceId);
        if (!device) return;

        document.getElementById("editDeviceId").value = device.id;
        document.getElementById("editDeviceName").value = device.name;
        document.getElementById("editDeviceRoom").value = device.roomId || 1;
        document.getElementById("editDeviceType").value = device.type;
        document.getElementById("editDevicePin").value = device.pinNumber;
        document.getElementById("editDeviceRelayCh").value = device.relayChannel;

        const modal = new bootstrap.Modal(document.getElementById("editDeviceModal"));
        modal.show();
    },

    async handleUpdateDevice(e) {
        e.preventDefault();
        const id = document.getElementById("editDeviceId").value;
        const name = document.getElementById("editDeviceName").value.trim();
        const roomId = parseInt(document.getElementById("editDeviceRoom").value);
        const type = document.getElementById("editDeviceType").value;
        const pinNumber = parseInt(document.getElementById("editDevicePin").value);
        const relayChannel = parseInt(document.getElementById("editDeviceRelayCh").value);

        try {
            const res = await API.put(`/api/devices/${id}`, {
                name, roomId, type, pinNumber, relayChannel
            });

            if (res.success) {
                API.showToast("Device configurations updated!", "success");
                const modalEl = document.getElementById("editDeviceModal");
                bootstrap.Modal.getInstance(modalEl).hide();
                this.loadDevices();
            }
        } catch (err) {
            API.showToast(err.message || "Failed to update device", "error");
        }
    },

    async deleteDevice(deviceId) {
        if (!confirm("Are you sure you want to remove this device? This will also remove its command logs.")) {
            return;
        }

        try {
            const res = await API.delete(`/api/devices/${deviceId}`);
            if (res.success) {
                API.showToast("Device removed successfully", "success");
                this.loadDevices();
            }
        } catch (err) {
            API.showToast(err.message || "Failed to delete device", "error");
        }
    }
};

document.addEventListener("DOMContentLoaded", () => DeviceManager.init());

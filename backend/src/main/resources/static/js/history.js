/**
 * =========================================================================
 * HISTORY & AUDIT MODULE: Event logs, hardware state changes, filters
 * =========================================================================
 */

const HistoryManager = {
    init() {
        this.bindEvents();
        const tabEl = document.getElementById("history-tab");
        if (tabEl) {
            tabEl.addEventListener("shown.bs.tab", () => {
                this.populateDeviceFilter();
                this.loadHistory();
            });
        }
    },

    bindEvents() {
        const filterDevice = document.getElementById("filterHistoryDevice");
        if (filterDevice) {
            filterDevice.addEventListener("change", () => this.loadHistory());
        }

        const refreshBtn = document.getElementById("refreshHistoryBtn");
        if (refreshBtn) {
            refreshBtn.addEventListener("click", () => this.loadHistory());
        }
    },

    async populateDeviceFilter() {
        const select = document.getElementById("filterHistoryDevice");
        if (!select || select.children.length > 1) return;

        try {
            const res = await API.get("/api/devices");
            if (res.success && res.data) {
                res.data.forEach(d => {
                    const opt = document.createElement("option");
                    opt.value = d.id;
                    opt.innerText = `${d.name} (${d.deviceKey})`;
                    select.appendChild(opt);
                });
            }
        } catch (e) {
            // Ignore filter population error
        }
    },

    async loadHistory() {
        if (!API.getToken()) return;

        const select = document.getElementById("filterHistoryDevice");
        const deviceId = select ? select.value : "";
        let endpoint = "/api/history";
        if (deviceId) {
            endpoint += `?deviceId=${deviceId}`;
        }

        try {
            const res = await API.get(endpoint);
            if (res.success && res.data) {
                this.renderTable(res.data);
            }
        } catch (err) {
            console.error("Failed to load history:", err);
            API.showToast("Could not load event history", "error");
        }
    },

    renderTable(events) {
        const tbody = document.getElementById("historyTableBody");
        if (!tbody) return;

        if (events.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-secondary">No recorded events match the filter</td></tr>`;
            return;
        }

        let html = "";
        events.forEach(ev => {
            const timeStr = new Date(ev.createdAt).toLocaleString();
            let badgeClass = "bg-secondary";
            if (ev.eventType === "COMMAND_ACKNOWLEDGED" || ev.eventType === "ONLINE_DETECTED") badgeClass = "bg-success";
            else if (ev.eventType === "OFFLINE_DETECTED" || ev.eventType === "ERROR") badgeClass = "bg-danger";
            else if (ev.eventType === "COMMAND_CREATED" || ev.eventType === "COMMAND_DISPATCHED") badgeClass = "bg-info";

            html += `
                <tr>
                    <td class="text-secondary small font-monospace">${timeStr}</td>
                    <td class="fw-bold">${escapeHtml(ev.deviceName || ev.deviceKey || 'System')}</td>
                    <td><span class="badge bg-secondary-subtle text-secondary">${escapeHtml(ev.roomName || 'N/A')}</span></td>
                    <td><span class="badge ${badgeClass} badge-custom">${escapeHtml(ev.eventType)}</span></td>
                    <td>
                        ${ev.oldState ? `<span class="badge bg-dark">${ev.oldState}</span> &rarr; ` : ''}
                        ${ev.newState ? `<span class="badge ${ev.newState === 'ON' ? 'bg-success' : 'bg-secondary'}">${ev.newState}</span>` : '-'}
                    </td>
                    <td class="small text-secondary">${escapeHtml(ev.details || '')}</td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
    }
};

document.addEventListener("DOMContentLoaded", () => HistoryManager.init());

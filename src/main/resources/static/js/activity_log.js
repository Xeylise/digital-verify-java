// Activity Log Data & Logic

const activityLogData = [
    {
        id: 1,
        timestampDate: 'May 26, 2026',
        timestampTime: '10:45 AM',
        dateValue: '2026-05-26',
        auditorInitials: 'PC',
        auditorName: 'Paola Castro',
        auditorAvatarClass: 'avatar-pc',
        eventType: 'Status Changed',
        eventBadgeClass: 'badge-status-changed',
        eventIcon: 'fa-regular fa-circle-check',
        caseId: 'DV-1024',
        detailsHtml: 'Changed case status from <span class="text-highlight-review">In Review</span> to <span class="text-highlight-approved">Approved</span>.'
    },
    {
        id: 2,
        timestampDate: 'May 26, 2026',
        timestampTime: '09:15 AM',
        dateValue: '2026-05-26',
        auditorInitials: 'SY',
        auditorName: 'System',
        auditorAvatarClass: 'avatar-sy',
        eventType: 'Case Reviewed',
        eventBadgeClass: 'badge-case-reviewed',
        eventIcon: 'fa-regular fa-eye',
        caseId: 'DV-1025',
        detailsHtml: 'Automated verification check performed: Facial biometrics matched at 98.5%.'
    },
    {
        id: 3,
        timestampDate: 'May 25, 2026',
        timestampTime: '14:30 PM',
        dateValue: '2026-05-25',
        auditorInitials: 'MG',
        auditorName: 'Maria Gomez',
        auditorAvatarClass: 'avatar-mg',
        eventType: 'Case Reviewed',
        eventBadgeClass: 'badge-case-reviewed',
        eventIcon: 'fa-regular fa-eye',
        caseId: 'DV-1022',
        detailsHtml: 'Reviewed Proactive KYC documentation. Requested additional ID scan.'
    },
    {
        id: 4,
        timestampDate: 'May 25, 2026',
        timestampTime: '08:00 AM',
        dateValue: '2026-05-25',
        auditorInitials: 'PC',
        auditorName: 'Paola Castro',
        auditorAvatarClass: 'avatar-pc',
        eventType: 'User Login',
        eventBadgeClass: 'badge-user-login',
        eventIcon: 'fa-solid fa-arrow-right-to-bracket',
        caseId: '-',
        detailsHtml: 'Successful login from IP 192.168.1.105 via Secure Portal.'
    },
    {
        id: 5,
        timestampDate: 'May 24, 2026',
        timestampTime: '16:20 PM',
        dateValue: '2026-05-24',
        auditorInitials: 'JR',
        auditorName: 'John Roe',
        auditorAvatarClass: 'avatar-jr',
        eventType: 'Account Suspended',
        eventBadgeClass: 'badge-account-suspended',
        eventIcon: 'fa-solid fa-ban',
        caseId: 'DV-1018',
        detailsHtml: 'Account suspended pending manual review. Reason: High risk score.'
    },
    {
        id: 6,
        timestampDate: 'May 24, 2026',
        timestampTime: '11:10 AM',
        dateValue: '2026-05-24',
        auditorInitials: 'PC',
        auditorName: 'Paola Castro',
        auditorAvatarClass: 'avatar-pc',
        eventType: 'Status Changed',
        eventBadgeClass: 'badge-status-changed',
        eventIcon: 'fa-regular fa-circle-check',
        caseId: 'DV-1015',
        detailsHtml: 'Changed case status from <span class="text-highlight-review">Pending</span> to <span class="text-highlight-approved">Approved</span>.'
    },
    {
        id: 7,
        timestampDate: 'May 23, 2026',
        timestampTime: '17:05 PM',
        dateValue: '2026-05-23',
        auditorInitials: 'SY',
        auditorName: 'System',
        auditorAvatarClass: 'avatar-sy',
        eventType: 'Status Changed',
        eventBadgeClass: 'badge-status-changed',
        eventIcon: 'fa-regular fa-circle-check',
        caseId: 'DV-1012',
        detailsHtml: 'Changed case status from <span class="text-highlight-review">Pending</span> to <span class="text-highlight-approved">Approved</span> via Auto-Rule.'
    },
    {
        id: 8,
        timestampDate: 'May 22, 2026',
        timestampTime: '13:40 PM',
        dateValue: '2026-05-22',
        auditorInitials: 'MG',
        auditorName: 'Maria Gomez',
        auditorAvatarClass: 'avatar-mg',
        eventType: 'Case Reviewed',
        eventBadgeClass: 'badge-case-reviewed',
        eventIcon: 'fa-regular fa-eye',
        caseId: 'DV-1009',
        detailsHtml: 'Identity verification approved after verifying secondary proof of residence.'
    }
];

let currentPage = 1;
const rowsPerPage = 5;
let filteredLogs = [...activityLogData];

function renderActivityTable() {
    const tbody = document.getElementById('activityTableBody');
    if (!tbody) return;

    const startIdx = (currentPage - 1) * rowsPerPage;
    const endIdx = startIdx + rowsPerPage;
    const pagedLogs = filteredLogs.slice(startIdx, endIdx);

    if (pagedLogs.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-5 text-muted">
                    <i class="fa-regular fa-folder-open fs-2 mb-2 d-block"></i>
                    No activity records found for this criteria.
                </td>
            </tr>
        `;
    } else {
        tbody.innerHTML = pagedLogs.map(item => `
            <tr>
                <td>
                    <div class="fw-bold text-white" style="font-size: 13px;">${item.timestampDate}</div>
                    <div class="text-muted" style="font-size: 11px;">${item.timestampTime}</div>
                </td>
                <td>
                    <div class="d-flex align-items-center gap-2">
                        <span class="auditor-avatar-circle ${item.auditorAvatarClass}">${item.auditorInitials}</span>
                        <span class="text-white fw-semibold" style="font-size: 13px;">${item.auditorName}</span>
                    </div>
                </td>
                <td>
                    <span class="event-type-badge ${item.eventBadgeClass}">
                        <i class="${item.eventIcon}"></i> ${item.eventType}
                    </span>
                </td>
                <td>
                    ${item.caseId !== '-' ? `<a href="queue.html" class="activity-case-link">${item.caseId}</a>` : '<span class="text-muted">-</span>'}
                </td>
                <td class="text-muted" style="font-size: 13px; max-width: 380px;">
                    ${item.detailsHtml}
                </td>
            </tr>
        `).join('');
    }

    renderPagination();
}

function renderPagination() {
    const totalCount = 128; // Display total 128 matching reference image
    const showingFrom = filteredLogs.length > 0 ? (currentPage - 1) * rowsPerPage + 1 : 0;
    const showingTo = Math.min(currentPage * rowsPerPage, 128);

    const showingText = document.getElementById('showingEntriesText');
    if (showingText) {
        showingText.textContent = `Showing ${showingFrom} to ${showingTo} of ${totalCount} entries`;
    }

    const paginationControls = document.getElementById('activityPaginationControls');
    if (paginationControls) {
        paginationControls.innerHTML = `
            <a href="#" class="bet-page-item ${currentPage === 1 ? 'disabled opacity-50' : ''}" onclick="changeActivityPage(${currentPage - 1}); return false;">
                <i class="fa-solid fa-chevron-left"></i>
            </a>
            <a href="#" class="bet-page-item ${currentPage === 1 ? 'active' : ''}" onclick="changeActivityPage(1); return false;">1</a>
            <a href="#" class="bet-page-item ${currentPage === 2 ? 'active' : ''}" onclick="changeActivityPage(2); return false;">2</a>
            <a href="#" class="bet-page-item ${currentPage === 3 ? 'active' : ''}" onclick="changeActivityPage(3); return false;">3</a>
            <span class="text-muted mx-1">...</span>
            <a href="#" class="bet-page-item" onclick="changeActivityPage(${currentPage + 1}); return false;">
                <i class="fa-solid fa-chevron-right"></i>
            </a>
        `;
    }
}

function changeActivityPage(page) {
    if (page < 1) return;
    currentPage = page;
    renderActivityTable();
}

let datePickerInstance = null;
let selectedStartDate = new Date('2026-05-20');
let selectedEndDate = new Date('2026-05-26');

function applyDateFilter() {
    const filterBtn = document.getElementById('filterDateBtn');

    if (filterBtn) {
        const originalText = filterBtn.innerHTML;
        filterBtn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin"></i> Filtering...`;
        filterBtn.disabled = true;

        setTimeout(() => {
            if (selectedStartDate && selectedEndDate) {
                const s = new Date(selectedStartDate).setHours(0, 0, 0, 0);
                const e = new Date(selectedEndDate).setHours(23, 59, 59, 999);

                filteredLogs = activityLogData.filter(item => {
                    const itemDate = new Date(item.dateValue).getTime();
                    return itemDate >= s && itemDate <= e;
                });
            } else {
                filteredLogs = [...activityLogData];
            }

            filterBtn.innerHTML = originalText;
            filterBtn.disabled = false;
            currentPage = 1;
            renderActivityTable();
        }, 300);
    }
}

function clearDateFilter() {
    selectedStartDate = new Date('2026-05-20');
    selectedEndDate = new Date('2026-05-26');

    if (datePickerInstance) {
        datePickerInstance.setDate(['May 20, 2026', 'May 26, 2026'], true);
    } else {
        const input = document.getElementById('dateRangeInput');
        if (input) input.value = 'May 20, 2026 - May 26, 2026';
    }

    filteredLogs = [...activityLogData];
    currentPage = 1;
    renderActivityTable();
}

function exportLogsCsv() {
    let csv = "Timestamp,Auditor,Event Type,Case ID,Details\n";
    activityLogData.forEach(row => {
        const cleanDetails = row.detailsHtml.replace(/<[^>]*>/g, '');
        csv += `"${row.timestampDate} ${row.timestampTime}","${row.auditorName}","${row.eventType}","${row.caseId}","${cleanDetails}"\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Activity_Log_Export_${new Date().toISOString().slice(0,10)}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// User Profile dropdown
function toggleUserDropdown(event) {
    if (event) event.stopPropagation();
    const dropdown = document.getElementById('userProfileDropdown');
    if (dropdown) dropdown.classList.toggle('show');
}

function closeUserDropdown() {
    const dropdown = document.getElementById('userProfileDropdown');
    if (dropdown && dropdown.classList.contains('show')) {
        dropdown.classList.remove('show');
    }
}

// Mobile drawer toggle
function toggleDrawer() {
    const drawer = document.getElementById('mobileDrawer');
    const overlay = document.getElementById('drawerOverlay');
    if (drawer && overlay) {
        const isOpen = drawer.classList.contains('open');
        if (isOpen) {
            drawer.classList.remove('open');
            overlay.classList.remove('open');
        } else {
            drawer.classList.add('open');
            overlay.classList.add('open');
        }
    }
}

// Notifications modal
function openNotificationsModal() {
    const modal = document.getElementById('notificationsModal');
    if (modal) modal.style.display = 'flex';
}

function closeNotificationsModal() {
    const modal = document.getElementById('notificationsModal');
    if (modal) modal.style.display = 'none';
}

function updateNotificationBadge() {
    const remaining = document.querySelectorAll('#notificationsListContainer > div').length;
    const badge = document.getElementById('headerBellBadge');
    const countText = document.getElementById('unreadNotifCountText');

    if (badge) {
        if (remaining > 0) {
            badge.style.display = '';
            badge.textContent = remaining;
        } else {
            badge.style.display = 'none';
        }
    }

    if (countText) {
        countText.textContent = remaining === 1 ? '1 unread notification' : `${remaining} unread notifications`;
    }
}

function dismissNotification(itemId) {
    const item = document.getElementById(itemId);
    if (item) {
        item.style.transition = 'all 0.25s ease';
        item.style.opacity = '0';
        item.style.transform = 'translateX(20px)';
        setTimeout(() => {
            item.remove();
            updateNotificationBadge();
            const container = document.getElementById('notificationsListContainer');
            if (container && container.children.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4 text-muted">
                        <i class="fa-regular fa-bell-slash fs-3 mb-2 d-block"></i>
                        <span class="small">No unread notifications</span>
                    </div>
                `;
            }
        }, 250);
    }
}

function markAllNotificationsAsRead() {
    const container = document.getElementById('notificationsListContainer');
    if (container) {
        container.innerHTML = `
            <div class="text-center py-4 text-muted">
                <i class="fa-regular fa-bell-slash fs-3 mb-2 d-block"></i>
                <span class="small">All caught up! No unread notifications</span>
            </div>
        `;
        updateNotificationBadge();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // Initialize Flatpickr Date Range Picker
    if (typeof flatpickr !== 'undefined') {
        datePickerInstance = flatpickr('#dateRangeInput', {
            mode: 'range',
            dateFormat: 'M d, Y',
            defaultDate: ['May 20, 2026', 'May 26, 2026'],
            onChange: function(selectedDates) {
                if (selectedDates.length === 2) {
                    selectedStartDate = selectedDates[0];
                    selectedEndDate = selectedDates[1];
                } else if (selectedDates.length === 1) {
                    selectedStartDate = selectedDates[0];
                    selectedEndDate = selectedDates[0];
                }
            }
        });
    }

    renderActivityTable();

    document.addEventListener('click', function(event) {
        const container = document.getElementById('userProfileDropdownContainer');
        if (container && !container.contains(event.target)) {
            closeUserDropdown();
        }
    });
});

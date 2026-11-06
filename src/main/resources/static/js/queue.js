// Modal Handlers & Fallbacks for Review Case Modal
if (!window.openReviewModal) {
    window.openReviewModal = async function (caseId) {
        const modal = document.getElementById('reviewCaseModal');
        if (modal) modal.style.display = 'flex';
        if (typeof openReviewModal === 'function' && openReviewModal !== window.openReviewModal) {
            return openReviewModal(caseId);
        }
    };
}
if (!window.closeReviewModal) {
    window.closeReviewModal = function () {
        const modal = document.getElementById('reviewCaseModal');
        if (modal) modal.style.display = 'none';
    };
}

// User Profile Dropdown Toggle
function toggleUserDropdown(event) {
    if (event) event.stopPropagation();
    const dropdown = document.getElementById('userProfileDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

function closeUserDropdown() {
    const dropdown = document.getElementById('userProfileDropdown');
    if (dropdown && dropdown.classList.contains('show')) {
        dropdown.classList.remove('show');
    }
}

// Notifications Modal Handlers
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

// ----------------------------------------------------
// Queue Table Filtering, Pagination & Refresh Logics
// ----------------------------------------------------
let currentQueuePage = 1;
const queuePageSize = 6;

function filterQueueTable() {
    const searchVal = (document.getElementById('queueSearchInput')?.value || '').toLowerCase().trim();
    const reasonVal = document.getElementById('queueReasonSelect')?.value || 'all';

    const rows = Array.from(document.querySelectorAll('#queueCasesTable tbody tr'));
    
    // Find all matching rows
    const matchingRows = rows.filter(row => {
        const caseId = (row.getAttribute('data-case') || '').toLowerCase();
        const account = (row.getAttribute('data-account') || '').toLowerCase();
        const player = (row.getAttribute('data-player') || '').toLowerCase();
        const reason = (row.getAttribute('data-reason') || '').toLowerCase();
        const textContent = row.textContent.toLowerCase();

        const matchesSearch = !searchVal ||
            caseId.includes(searchVal) ||
            account.includes(searchVal) ||
            player.includes(searchVal) ||
            textContent.includes(searchVal);

        const matchesReason = (reasonVal === 'all') || (reason === reasonVal) || reason.includes(reasonVal);

        return matchesSearch && matchesReason;
    });

    // Reset to page 1 if current page exceeds total pages
    const totalMatching = matchingRows.length;
    const totalPages = Math.ceil(totalMatching / queuePageSize) || 1;
    if (currentQueuePage > totalPages) {
        currentQueuePage = 1;
    }

    // Hide all rows first
    rows.forEach(r => r.style.display = 'none');

    // Show rows corresponding to current page
    const startIndex = (currentQueuePage - 1) * queuePageSize;
    const endIndex = startIndex + queuePageSize;
    const pagedRows = matchingRows.slice(startIndex, endIndex);

    pagedRows.forEach(r => r.style.display = '');

    // Update Showing text
    const showingText = document.getElementById('queueShowingText');
    if (showingText) {
        if (totalMatching === 0) {
            showingText.textContent = 'Showing 0 cases';
        } else {
            const startNum = startIndex + 1;
            const endNum = Math.min(endIndex, totalMatching);
            showingText.textContent = `Showing ${startNum} to ${endNum} of ${totalMatching} cases`;
        }
    }

    // Update Pagination UI
    renderPagination(totalMatching, totalPages);
}

function renderPagination(totalCount, totalPages) {
    const controls = document.getElementById('queuePaginationControls');
    if (!controls) return;

    if (totalCount === 0) {
        controls.innerHTML = `
            <a href="#" class="bet-page-item disabled"><i class="fa-solid fa-chevron-left"></i></a>
            <a href="#" class="bet-page-item active">1</a>
            <a href="#" class="bet-page-item disabled"><i class="fa-solid fa-chevron-right"></i></a>
        `;
        return;
    }

    let html = `
        <a href="#" class="bet-page-item ${currentQueuePage === 1 ? 'disabled' : ''}" onclick="changeQueuePage('prev'); return false;"><i class="fa-solid fa-chevron-left"></i></a>
    `;

    if (totalPages <= 5) {
        for (let p = 1; p <= totalPages; p++) {
            html += `
                <a href="#" class="bet-page-item ${p === currentQueuePage ? 'active' : ''}" onclick="goToQueuePage(${p}); return false;">${p}</a>
            `;
        }
    } else {
        // Mirrors dashboard.html pattern: 1, 2, 3, ..., 10
        if (currentQueuePage <= 3) {
            for (let p = 1; p <= 3; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentQueuePage ? 'active' : ''}" onclick="goToQueuePage(${p}); return false;">${p}</a>
                `;
            }
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item ${totalPages === currentQueuePage ? 'active' : ''}" onclick="goToQueuePage(${totalPages}); return false;">${totalPages}</a>
            `;
        } else if (currentQueuePage >= totalPages - 2) {
            html += `
                <a href="#" class="bet-page-item" onclick="goToQueuePage(1); return false;">1</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            for (let p = totalPages - 2; p <= totalPages; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentQueuePage ? 'active' : ''}" onclick="goToQueuePage(${p}); return false;">${p}</a>
                `;
            }
        } else {
            html += `
                <a href="#" class="bet-page-item" onclick="goToQueuePage(1); return false;">1</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item active" onclick="goToQueuePage(${currentQueuePage}); return false;">${currentQueuePage}</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item" onclick="goToQueuePage(${totalPages}); return false;">${totalPages}</a>
            `;
        }
    }

    html += `
        <a href="#" class="bet-page-item ${currentQueuePage === totalPages ? 'disabled' : ''}" onclick="changeQueuePage('next'); return false;"><i class="fa-solid fa-chevron-right"></i></a>
    `;

    controls.innerHTML = html;
}

function goToQueuePage(pageNum) {
    currentQueuePage = pageNum;
    filterQueueTable();
}

function changeQueuePage(direction) {
    const rows = Array.from(document.querySelectorAll('#queueCasesTable tbody tr'));
    const searchVal = (document.getElementById('queueSearchInput')?.value || '').toLowerCase().trim();
    const reasonVal = document.getElementById('queueReasonSelect')?.value || 'all';

    const matchingCount = rows.filter(row => {
        const caseId = (row.getAttribute('data-case') || '').toLowerCase();
        const account = (row.getAttribute('data-account') || '').toLowerCase();
        const reason = row.getAttribute('data-reason') || '';
        return (!searchVal || caseId.includes(searchVal) || account.includes(searchVal)) &&
               (reasonVal === 'all' || reason === reasonVal);
    }).length;

    const totalPages = Math.ceil(matchingCount / queuePageSize) || 1;

    if (direction === 'prev' && currentQueuePage > 1) {
        currentQueuePage--;
        filterQueueTable();
    } else if (direction === 'next' && currentQueuePage < totalPages) {
        currentQueuePage++;
        filterQueueTable();
    }
}

function clearQueueFilters() {
    const searchInput = document.getElementById('queueSearchInput');
    const reasonSelect = document.getElementById('queueReasonSelect');
    if (searchInput) searchInput.value = '';
    if (reasonSelect) reasonSelect.value = 'all';
    currentQueuePage = 1;
    filterQueueTable();
}

// Refresh table with smooth feedback (only affects table)
function refreshQueueTable() {
    const refreshBtn = document.getElementById('refreshQueueBtn');
    const refreshIcon = document.getElementById('refreshIcon');
    const tableBody = document.querySelector('#queueCasesTable tbody');

    if (refreshIcon) {
        refreshIcon.classList.add('fa-spin');
    }
    if (tableBody) {
        tableBody.style.opacity = '0.35';
        tableBody.style.transition = 'opacity 0.25s ease';
    }

    setTimeout(() => {
        clearQueueFilters();
        if (tableBody) {
            tableBody.style.opacity = '1';
        }
        if (refreshIcon) {
            refreshIcon.classList.remove('fa-spin');
        }
    }, 400);
}

// Close modals and dropdowns when clicking outside or pressing Escape key
window.addEventListener('click', function (e) {
    const reviewOverlay = document.getElementById('reviewCaseModal');
    if (e.target === reviewOverlay) {
        closeReviewModal();
    }

    const notifOverlay = document.getElementById('notificationsModal');
    if (e.target === notifOverlay) {
        closeNotificationsModal();
    }

    const dropdownContainer = document.getElementById('userProfileDropdownContainer');
    if (dropdownContainer && !dropdownContainer.contains(e.target)) {
        closeUserDropdown();
    }
});

window.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        closeReviewModal();
        closeNotificationsModal();
        closeUserDropdown();
    }
});

// Explicit window bindings
window.filterQueueTable = filterQueueTable;
window.goToQueuePage = goToQueuePage;
window.changeQueuePage = changeQueuePage;
window.clearQueueFilters = clearQueueFilters;
window.refreshQueueTable = refreshQueueTable;

// Initialize table on load
document.addEventListener('DOMContentLoaded', function () {
    filterQueueTable();
});

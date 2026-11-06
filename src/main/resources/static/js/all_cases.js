// All Cases Page Logic: Filtering, Pagination, Modals & Dynamic Updates

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
// All Cases Table Filtering, Pagination & Refresh Logics
// ----------------------------------------------------
let currentAllCasesPage = 1;
const allCasesPageSize = 8;

function filterAllCasesTable() {
    const searchVal = (document.getElementById('allCasesSearchInput')?.value || '').toLowerCase().trim();
    const reasonVal = document.getElementById('allCasesReasonSelect')?.value || 'all';

    const rows = Array.from(document.querySelectorAll('#allCasesTable tbody tr'));
    
    // Filter matching rows
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

    const totalMatching = matchingRows.length;
    const totalPages = Math.ceil(totalMatching / allCasesPageSize) || 1;
    if (currentAllCasesPage > totalPages) {
        currentAllCasesPage = 1;
    }

    // Hide all rows first
    rows.forEach(r => r.style.display = 'none');

    // Show rows for current page
    const startIndex = (currentAllCasesPage - 1) * allCasesPageSize;
    const endIndex = startIndex + allCasesPageSize;
    const pagedRows = matchingRows.slice(startIndex, endIndex);

    pagedRows.forEach(r => r.style.display = '');

    // Update Showing text
    const showingText = document.getElementById('allCasesShowingText');
    if (showingText) {
        if (totalMatching === 0) {
            showingText.textContent = 'Showing 0 cases';
        } else {
            const startNum = startIndex + 1;
            const endNum = Math.min(endIndex, totalMatching);
            showingText.textContent = `Showing ${startNum} to ${endNum} of ${totalMatching} cases`;
        }
    }

    // Render Pagination Controls
    renderAllCasesPagination(totalMatching, totalPages);
}

function renderAllCasesPagination(totalCount, totalPages) {
    const controls = document.getElementById('allCasesPaginationControls');
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
        <a href="#" class="bet-page-item ${currentAllCasesPage === 1 ? 'disabled' : ''}" onclick="changeAllCasesPage('prev'); return false;"><i class="fa-solid fa-chevron-left"></i></a>
    `;

    if (totalPages <= 5) {
        for (let p = 1; p <= totalPages; p++) {
            html += `
                <a href="#" class="bet-page-item ${p === currentAllCasesPage ? 'active' : ''}" onclick="goToAllCasesPage(${p}); return false;">${p}</a>
            `;
        }
    } else {
        if (currentAllCasesPage <= 3) {
            for (let p = 1; p <= 3; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentAllCasesPage ? 'active' : ''}" onclick="goToAllCasesPage(${p}); return false;">${p}</a>
                `;
            }
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item ${totalPages === currentAllCasesPage ? 'active' : ''}" onclick="goToAllCasesPage(${totalPages}); return false;">${totalPages}</a>
            `;
        } else if (currentAllCasesPage >= totalPages - 2) {
            html += `
                <a href="#" class="bet-page-item" onclick="goToAllCasesPage(1); return false;">1</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            for (let p = totalPages - 2; p <= totalPages; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentAllCasesPage ? 'active' : ''}" onclick="goToAllCasesPage(${p}); return false;">${p}</a>
                `;
            }
        } else {
            html += `
                <a href="#" class="bet-page-item" onclick="goToAllCasesPage(1); return false;">1</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item active" onclick="goToAllCasesPage(${currentAllCasesPage}); return false;">${currentAllCasesPage}</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item" onclick="goToAllCasesPage(${totalPages}); return false;">${totalPages}</a>
            `;
        }
    }

    html += `
        <a href="#" class="bet-page-item ${currentAllCasesPage === totalPages ? 'disabled' : ''}" onclick="changeAllCasesPage('next'); return false;"><i class="fa-solid fa-chevron-right"></i></a>
    `;

    controls.innerHTML = html;
}

function goToAllCasesPage(pageNum) {
    currentAllCasesPage = pageNum;
    filterAllCasesTable();
}

function changeAllCasesPage(direction) {
    const rows = Array.from(document.querySelectorAll('#allCasesTable tbody tr'));
    const searchVal = (document.getElementById('allCasesSearchInput')?.value || '').toLowerCase().trim();
    const reasonVal = document.getElementById('allCasesReasonSelect')?.value || 'all';

    const matchingCount = rows.filter(row => {
        const caseId = (row.getAttribute('data-case') || '').toLowerCase();
        const account = (row.getAttribute('data-account') || '').toLowerCase();
        const reason = row.getAttribute('data-reason') || '';

        const matchesSearch = !searchVal || caseId.includes(searchVal) || account.includes(searchVal);
        const matchesReason = (reasonVal === 'all') || (reason === reasonVal);

        return matchesSearch && matchesReason;
    }).length;

    const totalPages = Math.ceil(matchingCount / allCasesPageSize) || 1;

    if (direction === 'prev' && currentAllCasesPage > 1) {
        currentAllCasesPage--;
        filterAllCasesTable();
    } else if (direction === 'next' && currentAllCasesPage < totalPages) {
        currentAllCasesPage++;
        filterAllCasesTable();
    }
}

function clearAllCasesFilters() {
    const searchInput = document.getElementById('allCasesSearchInput');
    const reasonSelect = document.getElementById('allCasesReasonSelect');

    if (searchInput) searchInput.value = '';
    if (reasonSelect) reasonSelect.value = 'all';

    currentAllCasesPage = 1;
    filterAllCasesTable();
}

// Refresh table only with visual animation
function refreshAllCasesTable() {
    const table = document.getElementById('allCasesTable');
    const refreshIcon = document.getElementById('refreshAllCasesIcon');
    const btn = document.getElementById('refreshAllCasesBtn');

    if (refreshIcon) refreshIcon.classList.add('fa-spin');
    if (btn) btn.disabled = true;

    if (table) {
        table.style.transition = 'opacity 0.25s ease';
        table.style.opacity = '0.3';
    }

    setTimeout(() => {
        if (table) table.style.opacity = '1';
        if (refreshIcon) refreshIcon.classList.remove('fa-spin');
        if (btn) btn.disabled = false;
        filterAllCasesTable();
    }, 450);
}

// Global click listeners
document.addEventListener('click', function(event) {
    const reviewOverlay = document.getElementById('reviewCaseModal');
    if (event.target === reviewOverlay) {
        closeReviewModal();
    }

    const notifOverlay = document.getElementById('notificationsModal');
    if (event.target === notifOverlay) {
        closeNotificationsModal();
    }

    const container = document.getElementById('userProfileDropdownContainer');
    if (container && !container.contains(event.target)) {
        closeUserDropdown();
    }
});

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeReviewModal();
        closeNotificationsModal();
        closeUserDropdown();
    }
});

// Explicit window bindings
window.filterAllCasesTable = filterAllCasesTable;
window.goToAllCasesPage = goToAllCasesPage;
window.changeAllCasesPage = changeAllCasesPage;
window.clearAllCasesFilters = clearAllCasesFilters;
window.refreshAllCasesTable = refreshAllCasesTable;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', function() {
    filterAllCasesTable();
});

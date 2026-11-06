// Dashboard JavaScript: Search, Filtering, Pagination and Modal Logics

let currentDashboardPage = 1;
const dashboardPageSize = 6;
let activeTabFilter = 'all';
let activeSearchKeyword = '';
let activeReasonFilter = '';

// Search Cases Modal Handlers
function openSearchCasesModal() {
    const modal = document.getElementById('searchCasesModal');
    if (modal) {
        modal.style.display = 'flex';
        const input = document.getElementById('modalSearchInput');
        if (input) {
            input.value = activeSearchKeyword;
            setTimeout(() => input.focus(), 100);
        }
        const reasonSelect = document.getElementById('modalReasonSelect');
        if (reasonSelect) {
            reasonSelect.value = activeReasonFilter;
        }
    }
}

function closeSearchCasesModal() {
    const modal = document.getElementById('searchCasesModal');
    if (modal) modal.style.display = 'none';
}

function applyDashboardSearchModal() {
    const searchInput = document.getElementById('modalSearchInput');
    const reasonSelect = document.getElementById('modalReasonSelect');

    activeSearchKeyword = (searchInput?.value || '').trim();
    activeReasonFilter = (reasonSelect?.value || '').trim();

    const banner = document.getElementById('dashboardActiveFilterBanner');
    const filterText = document.getElementById('dashboardActiveFilterText');

    if (activeSearchKeyword || activeReasonFilter) {
        let textParts = [];
        if (activeSearchKeyword) textParts.push(`"${activeSearchKeyword}"`);
        if (activeReasonFilter) textParts.push(`Reason: ${activeReasonFilter}`);
        if (filterText) filterText.textContent = `Filtered by: ${textParts.join(' • ')}`;
        if (banner) {
            banner.classList.remove('d-none');
            banner.classList.add('d-flex');
        }
    } else {
        if (banner) {
            banner.classList.add('d-none');
            banner.classList.remove('d-flex');
        }
    }

    currentDashboardPage = 1;
    closeSearchCasesModal();
    filterDashboardTable();
}

function clearDashboardSearchModal() {
    activeSearchKeyword = '';
    activeReasonFilter = '';

    const searchInput = document.getElementById('modalSearchInput');
    const reasonSelect = document.getElementById('modalReasonSelect');
    if (searchInput) searchInput.value = '';
    if (reasonSelect) reasonSelect.selectedIndex = 0;

    const banner = document.getElementById('dashboardActiveFilterBanner');
    if (banner) {
        banner.classList.add('d-none');
        banner.classList.remove('d-flex');
    }

    currentDashboardPage = 1;
    filterDashboardTable();
}

// Tab Filter Logic for Recent Cases
function filterByTab(buttonEl, filterKey) {
    const tabButtons = document.querySelectorAll('#dashboardTabs .tab-filter-btn');
    tabButtons.forEach(btn => btn.classList.remove('active'));
    if (buttonEl) buttonEl.classList.add('active');

    activeTabFilter = filterKey || 'all';
    currentDashboardPage = 1;
    filterDashboardTable();
}

// Central Engine: Combined Filtering & Pagination
function filterDashboardTable() {
    const tableRows = Array.from(document.querySelectorAll('#recentCasesTable tbody tr'));
    const searchLower = activeSearchKeyword.toLowerCase();
    const reasonLower = activeReasonFilter.toLowerCase();

    const matchingRows = tableRows.filter(row => {
        const status = (row.getAttribute('data-status') || '').toLowerCase();
        const reason = (row.getAttribute('data-reason') || '').toLowerCase();
        const owner = (row.getAttribute('data-owner') || '').toLowerCase();
        const caseNumber = (row.getAttribute('data-case') || '').toLowerCase();
        const account = (row.getAttribute('data-account') || '').toLowerCase();
        const player = (row.getAttribute('data-player') || '').toLowerCase();
        const textContent = row.textContent.toLowerCase();

        // 1. Tab check
        let matchesTab = false;
        switch (activeTabFilter) {
            case 'all':
                matchesTab = true;
                break;
            case 'pending':
                matchesTab = (status === 'pending');
                break;
            case 'in_review':
                matchesTab = (status === 'in_review');
                break;
            case 'fraud_review':
                matchesTab = (reason === 'fraud_review');
                break;
            case 'proactive_kyc':
                matchesTab = (reason === 'proactive_kyc');
                break;
            case 'assigned_to_me':
                matchesTab = (owner.includes('paola castro'));
                break;
            default:
                matchesTab = true;
                break;
        }

        // 2. Search check (Case ID, Account, Player name or text)
        const matchesSearch = !searchLower ||
            caseNumber.includes(searchLower) ||
            account.includes(searchLower) ||
            player.includes(searchLower) ||
            textContent.includes(searchLower);

        // 3. Reason filter check
        const matchesReason = !reasonLower || reason === reasonLower || reason.includes(reasonLower);

        return matchesTab && matchesSearch && matchesReason;
    });

    const totalMatching = matchingRows.length;
    const totalPages = Math.ceil(totalMatching / dashboardPageSize) || 1;
    if (currentDashboardPage > totalPages) {
        currentDashboardPage = 1;
    }

    // Hide all rows first
    tableRows.forEach(row => row.style.display = 'none');

    // Show current page rows
    const startIndex = (currentDashboardPage - 1) * dashboardPageSize;
    const endIndex = startIndex + dashboardPageSize;
    const pagedRows = matchingRows.slice(startIndex, endIndex);
    pagedRows.forEach(row => row.style.display = '');

    // Update Showing text
    const showingText = document.getElementById('dashboardShowingText');
    if (showingText) {
        if (totalMatching === 0) {
            showingText.textContent = 'Showing 0 cases';
        } else {
            const startNum = startIndex + 1;
            const endNum = Math.min(endIndex, totalMatching);
            showingText.textContent = `Showing ${startNum} to ${endNum} of ${totalMatching} cases`;
        }
    }

    renderDashboardPagination(totalMatching, totalPages);
}

function renderDashboardPagination(totalCount, totalPages) {
    const controls = document.getElementById('dashboardPaginationControls');
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
        <a href="#" class="bet-page-item ${currentDashboardPage === 1 ? 'disabled' : ''}" onclick="changeDashboardPage('prev'); return false;"><i class="fa-solid fa-chevron-left"></i></a>
    `;

    if (totalPages <= 5) {
        for (let p = 1; p <= totalPages; p++) {
            html += `
                <a href="#" class="bet-page-item ${p === currentDashboardPage ? 'active' : ''}" onclick="goToDashboardPage(${p}); return false;">${p}</a>
            `;
        }
    } else {
        if (currentDashboardPage <= 3) {
            for (let p = 1; p <= 3; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentDashboardPage ? 'active' : ''}" onclick="goToDashboardPage(${p}); return false;">${p}</a>
                `;
            }
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item ${totalPages === currentDashboardPage ? 'active' : ''}" onclick="goToDashboardPage(${totalPages}); return false;">${totalPages}</a>
            `;
        } else if (currentDashboardPage >= totalPages - 2) {
            html += `<a href="#" class="bet-page-item" onclick="goToDashboardPage(1); return false;">1</a><span class="text-muted mx-1">...</span>`;
            for (let p = totalPages - 2; p <= totalPages; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentDashboardPage ? 'active' : ''}" onclick="goToDashboardPage(${p}); return false;">${p}</a>
                `;
            }
        } else {
            html += `<a href="#" class="bet-page-item" onclick="goToDashboardPage(1); return false;">1</a><span class="text-muted mx-1">...</span>`;
            html += `<a href="#" class="bet-page-item active" onclick="goToDashboardPage(${currentDashboardPage}); return false;">${currentDashboardPage}</a><span class="text-muted mx-1">...</span>`;
            html += `<a href="#" class="bet-page-item" onclick="goToDashboardPage(${totalPages}); return false;">${totalPages}</a>`;
        }
    }

    html += `
        <a href="#" class="bet-page-item ${currentDashboardPage === totalPages ? 'disabled' : ''}" onclick="changeDashboardPage('next'); return false;"><i class="fa-solid fa-chevron-right"></i></a>
    `;

    controls.innerHTML = html;
}

function goToDashboardPage(pageNum) {
    currentDashboardPage = pageNum;
    filterDashboardTable();
}

function changeDashboardPage(direction) {
    const tableRows = Array.from(document.querySelectorAll('#recentCasesTable tbody tr'));
    const searchLower = activeSearchKeyword.toLowerCase();
    const reasonLower = activeReasonFilter.toLowerCase();

    const matchingCount = tableRows.filter(row => {
        const status = (row.getAttribute('data-status') || '').toLowerCase();
        const reason = (row.getAttribute('data-reason') || '').toLowerCase();
        const owner = (row.getAttribute('data-owner') || '').toLowerCase();
        const caseNumber = (row.getAttribute('data-case') || '').toLowerCase();
        const account = (row.getAttribute('data-account') || '').toLowerCase();
        const player = (row.getAttribute('data-player') || '').toLowerCase();
        const textContent = row.textContent.toLowerCase();

        let matchesTab = false;
        switch (activeTabFilter) {
            case 'all': matchesTab = true; break;
            case 'pending': matchesTab = (status === 'pending'); break;
            case 'in_review': matchesTab = (status === 'in_review'); break;
            case 'fraud_review': matchesTab = (reason === 'fraud_review'); break;
            case 'proactive_kyc': matchesTab = (reason === 'proactive_kyc'); break;
            case 'assigned_to_me': matchesTab = (owner.includes('paola castro')); break;
            default: matchesTab = true; break;
        }

        const matchesSearch = !searchLower ||
            caseNumber.includes(searchLower) ||
            account.includes(searchLower) ||
            player.includes(searchLower) ||
            textContent.includes(searchLower);

        const matchesReason = !reasonLower || reason === reasonLower || reason.includes(reasonLower);

        return matchesTab && matchesSearch && matchesReason;
    }).length;

    const totalPages = Math.ceil(matchingCount / dashboardPageSize) || 1;

    if (direction === 'prev' && currentDashboardPage > 1) {
        currentDashboardPage--;
        filterDashboardTable();
    } else if (direction === 'next' && currentDashboardPage < totalPages) {
        currentDashboardPage++;
        filterDashboardTable();
    }
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

// Date Range Dropdown Handlers
function toggleDateDropdown(event) {
    if (event) event.stopPropagation();
    const dropdown = document.getElementById('dateRangeDropdown');
    const chevron = document.getElementById('dateChevronIcon');
    if (dropdown) {
        dropdown.classList.toggle('show');
        if (chevron) {
            chevron.style.transform = dropdown.classList.contains('show') ? 'rotate(180deg)' : 'rotate(0deg)';
        }
    }
}

function closeDateDropdown() {
    const dropdown = document.getElementById('dateRangeDropdown');
    const chevron = document.getElementById('dateChevronIcon');
    if (dropdown && dropdown.classList.contains('show')) {
        dropdown.classList.remove('show');
        if (chevron) chevron.style.transform = 'rotate(0deg)';
    }
}

function selectDatePreset(label, dateString) {
    const dateTextEl = document.getElementById('currentDateRangeText');
    if (dateTextEl) dateTextEl.textContent = dateString;
    const items = document.querySelectorAll('#dateRangeDropdown .date-dropdown-item');
    items.forEach(item => {
        if (item.textContent.trim().includes(label)) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
    closeDateDropdown();
}

function applyCustomDateRange() {
    const startInput = document.getElementById('customStartDate');
    const endInput = document.getElementById('customEndDate');
    if (startInput && endInput && startInput.value && endInput.value) {
        const startParts = startInput.value.split('-');
        const endParts = endInput.value.split('-');
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const startFormatted = `${months[parseInt(startParts[1], 10) - 1]} ${startParts[2]}, ${startParts[0]}`;
        const endFormatted = `${months[parseInt(endParts[1], 10) - 1]} ${endParts[2]}, ${endParts[0]}`;
        const dateTextEl = document.getElementById('currentDateRangeText');
        if (dateTextEl) dateTextEl.textContent = `${startFormatted} - ${endFormatted}`;
        const items = document.querySelectorAll('#dateRangeDropdown .date-dropdown-item');
        items.forEach(item => item.classList.remove('active'));
    }
    closeDateDropdown();
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

// Close modals and dropdowns when clicking outside or pressing Escape key
window.addEventListener('click', function (e) {
    const reviewOverlay = document.getElementById('reviewCaseModal');
    if (e.target === reviewOverlay) {
        if (window.closeReviewModal) window.closeReviewModal();
        else { const m = document.getElementById('reviewCaseModal'); if (m) m.style.display = 'none'; }
    }

    const searchOverlay = document.getElementById('searchCasesModal');
    if (e.target === searchOverlay) {
        closeSearchCasesModal();
    }

    const notifOverlay = document.getElementById('notificationsModal');
    if (e.target === notifOverlay) {
        closeNotificationsModal();
    }

    const dropdownContainer = document.getElementById('userProfileDropdownContainer');
    if (dropdownContainer && !dropdownContainer.contains(e.target)) {
        closeUserDropdown();
    }

    const dateContainer = document.getElementById('dateRangeDropdownContainer');
    if (dateContainer && !dateContainer.contains(e.target)) {
        closeDateDropdown();
    }
});

window.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        if (window.closeReviewModal) window.closeReviewModal();
        closeSearchCasesModal();
        closeNotificationsModal();
        closeUserDropdown();
        closeDateDropdown();
    }
});

// Explicit window bindings
window.openSearchCasesModal = openSearchCasesModal;
window.closeSearchCasesModal = closeSearchCasesModal;
window.applyDashboardSearchModal = applyDashboardSearchModal;
window.clearDashboardSearchModal = clearDashboardSearchModal;
window.filterByTab = filterByTab;
window.filterDashboardTable = filterDashboardTable;
window.goToDashboardPage = goToDashboardPage;
window.changeDashboardPage = changeDashboardPage;

// Initialize Dashboard table on load
document.addEventListener('DOMContentLoaded', function () {
    filterDashboardTable();
});

// Reports Page Logic: Dynamic generation, table filtering, pagination, export and modal handlers

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

// Export Menu Toggle
function toggleExportMenu(event) {
    if (event) event.stopPropagation();
    const menu = document.getElementById('reportsExportMenu');
    if (menu) {
        menu.classList.toggle('show');
    }
}

function closeExportMenu() {
    const menu = document.getElementById('reportsExportMenu');
    if (menu && menu.classList.contains('show')) {
        menu.classList.remove('show');
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
// Reports Pagination, Generation & Export Logics
// ----------------------------------------------------
let currentReportPage = 1;
const reportPageSize = 12;

function renderReportPagination(totalCount, totalPages) {
    const controls = document.getElementById('reportsPaginationControls');
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
        <a href="#" class="bet-page-item ${currentReportPage === 1 ? 'disabled' : ''}" onclick="changeReportPage('prev'); return false;"><i class="fa-solid fa-chevron-left"></i></a>
    `;

    if (totalPages <= 5) {
        for (let p = 1; p <= totalPages; p++) {
            html += `
                <a href="#" class="bet-page-item ${p === currentReportPage ? 'active' : ''}" onclick="goToReportPage(${p}); return false;">${p}</a>
            `;
        }
    } else {
        if (currentReportPage <= 3) {
            for (let p = 1; p <= 3; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentReportPage ? 'active' : ''}" onclick="goToReportPage(${p}); return false;">${p}</a>
                `;
            }
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item ${totalPages === currentReportPage ? 'active' : ''}" onclick="goToReportPage(${totalPages}); return false;">${totalPages}</a>
            `;
        } else if (currentReportPage >= totalPages - 2) {
            html += `
                <a href="#" class="bet-page-item" onclick="goToReportPage(1); return false;">1</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            for (let p = totalPages - 2; p <= totalPages; p++) {
                html += `
                    <a href="#" class="bet-page-item ${p === currentReportPage ? 'active' : ''}" onclick="goToReportPage(${p}); return false;">${p}</a>
                `;
            }
        } else {
            html += `
                <a href="#" class="bet-page-item" onclick="goToReportPage(1); return false;">1</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item active" onclick="goToReportPage(${currentReportPage}); return false;">${currentReportPage}</a>
            `;
            html += `<span class="text-muted mx-1">...</span>`;
            html += `
                <a href="#" class="bet-page-item" onclick="goToReportPage(${totalPages}); return false;">${totalPages}</a>
            `;
        }
    }

    html += `
        <a href="#" class="bet-page-item ${currentReportPage === totalPages ? 'disabled' : ''}" onclick="changeReportPage('next'); return false;"><i class="fa-solid fa-chevron-right"></i></a>
    `;

    controls.innerHTML = html;
}

function updateReportPageDisplay() {
    const rows = Array.from(document.querySelectorAll('#reportsCasesTable tbody tr'));
    const totalCount = 124; // Representative total from screenshot
    const visibleCount = rows.length;
    const totalPages = 5; // 5 pages shown in screenshot

    // Page rows slice
    rows.forEach((r, idx) => {
        // Display rows belonging to page
        r.style.display = '';
    });

    const showingText = document.getElementById('reportsShowingText');
    if (showingText) {
        const start = ((currentReportPage - 1) * 12) + 1;
        const end = Math.min(currentReportPage * 12, totalCount);
        showingText.textContent = `Mostrando ${start} a ${end} de ${totalCount} registros`;
    }

    renderReportPagination(totalCount, totalPages);
}

function goToReportPage(p) {
    currentReportPage = p;
    const table = document.getElementById('reportsCasesTable');
    if (table) {
        table.style.transition = 'opacity 0.2s';
        table.style.opacity = '0.3';
        setTimeout(() => {
            table.style.opacity = '1';
            updateReportPageDisplay();
        }, 200);
    } else {
        updateReportPageDisplay();
    }
}

function changeReportPage(dir) {
    if (dir === 'prev' && currentReportPage > 1) {
        goToReportPage(currentReportPage - 1);
    } else if (dir === 'next' && currentReportPage < 5) {
        goToReportPage(currentReportPage + 1);
    }
}

// Generate Report Action
function generateReport() {
    const origenSelect = document.getElementById('reportMesOrigen');
    const compSelect = document.getElementById('reportMesComp');
    const metaInput = document.getElementById('reportMeta');
    const btn = document.getElementById('btnGenerarReporte');
    const table = document.getElementById('reportsCasesTable');
    const subtitle = document.getElementById('reportSubtitleText');

    const origenText = origenSelect ? origenSelect.options[origenSelect.selectedIndex].text : 'Septiembre 2026';
    const compText = compSelect ? compSelect.options[compSelect.selectedIndex].text : 'Agosto 2026';

    if (btn) {
        btn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin me-1"></i> Generando...`;
        btn.disabled = true;
    }

    if (table) {
        table.style.transition = 'opacity 0.25s ease';
        table.style.opacity = '0.3';
    }

    setTimeout(() => {
        if (subtitle) {
            subtitle.textContent = `Productividad del equipo - ${origenText} vs ${compText}`;
        }
        if (table) table.style.opacity = '1';
        if (btn) {
            btn.innerHTML = `<i class="fa-solid fa-magnifying-glass me-1"></i> Generar reporte`;
            btn.disabled = false;
        }
        currentReportPage = 1;
        updateReportPageDisplay();
    }, 450);
}

// Clear Report Filters
function clearReportFilters() {
    const origen = document.getElementById('reportMesOrigen');
    const comp = document.getElementById('reportMesComp');
    const meta = document.getElementById('reportMeta');

    if (origen) origen.value = '2026-09';
    if (comp) comp.value = '2026-08';
    if (meta) meta.value = '';

    const subtitle = document.getElementById('reportSubtitleText');
    if (subtitle) {
        subtitle.textContent = `Productividad del equipo - Septiembre 2026 vs Agosto 2026`;
    }

    currentReportPage = 1;
    updateReportPageDisplay();
}

// Export Trigger Feedback
function exportReport(format) {
    closeExportMenu();
    const alertMsg = format === 'excel' 
        ? 'Exportando reporte a formato Excel (.xlsx)... ¡Descarga iniciada!' 
        : 'Exportando reporte a formato CSV (.csv)... ¡Descarga iniciada!';
    alert(alertMsg);
}

// Global click handler to close dropdowns
document.addEventListener('click', function(event) {
    const userContainer = document.getElementById('userProfileDropdownContainer');
    if (userContainer && !userContainer.contains(event.target)) {
        closeUserDropdown();
    }

    const exportContainer = document.getElementById('reportsExportContainer');
    if (exportContainer && !exportContainer.contains(event.target)) {
        closeExportMenu();
    }
});

// Initialize on load
document.addEventListener('DOMContentLoaded', function() {
    updateReportPageDisplay();
});

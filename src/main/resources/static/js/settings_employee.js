// Settings Employee Interactivity

function saveSettings() {
    const btn = document.querySelector('.btn-save-settings');
    if (btn) {
        const originalContent = btn.innerHTML;
        btn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin me-1"></i> Saving...`;
        btn.disabled = true;

        setTimeout(() => {
            btn.innerHTML = `<i class="fa-solid fa-circle-check me-1"></i> Saved!`;
            btn.style.backgroundColor = '#10b981';

            setTimeout(() => {
                btn.innerHTML = originalContent;
                btn.style.backgroundColor = '';
                btn.disabled = false;
                alert('Preferences and system notifications updated successfully.');
            }, 1000);
        }, 600);
    }
}

function updatePassword() {
    const currentPass = document.getElementById('currentPasswordInput').value;
    const newPass = document.getElementById('newPasswordInput').value;

    if (!currentPass) {
        alert('Please enter your current password.');
        return;
    }

    if (!newPass || newPass.length < 6) {
        alert('Please enter a valid new password (at least 6 characters).');
        return;
    }

    const btn = document.querySelector('.btn-update-password');
    if (btn) {
        btn.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin me-1"></i> Updating...`;
        btn.disabled = true;

        setTimeout(() => {
            document.getElementById('currentPasswordInput').value = '';
            document.getElementById('newPasswordInput').value = '';
            btn.innerHTML = `UPDATE PASSWORD`;
            btn.disabled = false;
            alert('Password updated successfully!');
        }, 600);
    }
}

// User profile dropdown toggle
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

// Global click listener
document.addEventListener('click', function(event) {
    const container = document.getElementById('userProfileDropdownContainer');
    if (container && !container.contains(event.target)) {
        closeUserDropdown();
    }
});

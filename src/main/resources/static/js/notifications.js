function markAsRead(cardId, actionId) {
    const card = document.getElementById(cardId);
    const action = document.getElementById(actionId);

    if (card && action) {
        // Change card background color style
        card.classList.add('read-status');

        // Change mark as read to Opened + open envelope icon
        action.innerHTML = `
                    <div class="action-opened">
                        <span>Opened</span>
                        <i class="fa-regular fa-envelope-open fs-5"></i>
                    </div>
                `;
    }

    // Remove badges or highlight elsewhere if present
    const badge = document.getElementById('sidebarBadge');
    if (badge) {
        badge.style.display = 'none';
    }
    const dBadge = document.getElementById('drawerBadge');
    if (dBadge) {
        dBadge.style.display = 'none';
    }
}

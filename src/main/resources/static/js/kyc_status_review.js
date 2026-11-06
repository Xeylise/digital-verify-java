let currentState = 'pending';

function showState(state) {
    currentState = state;

    // Close any details open
    closeDetails();

    // Elements to configure dynamically
    const reviewItem = document.getElementById('timeline-review-item');
    const reviewDot = document.getElementById('review-dot');
    const reviewTitle = document.getElementById('review-title');
    const reviewDesc = document.getElementById('review-desc');
    const actionItem = document.getElementById('timeline-action-item');
    const completedItem = document.getElementById('timeline-completed-item');
    const completedDot = document.getElementById('completed-dot');
    const completedTitleArea = document.getElementById('completed-title-area');
    const completedInfoPill = document.getElementById('completed-info-pill');

    if (state === 'pending') {
        // DV 05 KYC Status / Review style
        reviewItem.className = 'timeline-item active-blue';
        reviewDot.className = 'timeline-dot active-blue';
        reviewDot.innerHTML = '<i class="fa-solid fa-clock"></i>';
        reviewTitle.innerText = 'In review';
        reviewDesc.style.display = 'block';

        actionItem.style.display = 'none';

        completedItem.className = 'timeline-item';
        completedDot.className = 'timeline-dot';
        completedDot.innerHTML = '';
        completedInfoPill.style.display = 'none';
    }
    else if (state === 'rejected') {
        // DV 06 Additional Documents Required style
        reviewItem.className = 'timeline-item';
        reviewDot.className = 'timeline-dot';
        reviewDot.innerHTML = '';
        reviewTitle.innerText = 'In review';
        reviewDesc.style.display = 'none';

        actionItem.style.display = 'block';
        actionItem.className = 'timeline-item active-danger';
        const actionDot = actionItem.querySelector('.timeline-dot');
        actionDot.className = 'timeline-dot active-danger';
        actionDot.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i>';

        completedItem.className = 'timeline-item';
        completedDot.className = 'timeline-dot';
        completedDot.innerHTML = '';
        completedInfoPill.style.display = 'none';
    }
    else if (state === 'completed') {
        // DV 07 KYC Status / Completed style
        reviewItem.className = 'timeline-item';
        reviewDot.className = 'timeline-dot';
        reviewDot.innerHTML = '';
        reviewTitle.innerText = 'In review';
        reviewDesc.style.display = 'none';

        actionItem.style.display = 'none';

        completedItem.className = 'timeline-item active-success';
        completedDot.className = 'timeline-dot active-success';
        completedDot.innerHTML = '<i class="fa-solid fa-check"></i>';
        completedInfoPill.style.display = 'inline-flex';
    }
}

function openDetails(type) {
    if (type === 'rejected') {
        document.getElementById('detail-rejected-overlay').style.display = 'flex';
    } else if (type === 'completed') {
        document.getElementById('detail-completed-overlay').style.display = 'flex';
    }
}

function closeDetails() {
    document.getElementById('detail-rejected-overlay').style.display = 'none';
    document.getElementById('detail-completed-overlay').style.display = 'none';
}

document.addEventListener('DOMContentLoaded', function() {
    const kycData = document.getElementById('realKycData');
    if (kycData) {
        const rawStatus = (kycData.getAttribute('data-status') || '').toLowerCase();
        const notes = kycData.getAttribute('data-notes');

        if (rawStatus === 'approved' || rawStatus === 'completed') {
            showState('completed');
        } else if (rawStatus === 'rejected') {
            showState('rejected');
            if (notes && notes.trim() !== '') {
                const noteElem = document.getElementById('rejected-reason-text');
                if (noteElem) {
                    noteElem.innerText = notes;
                }
            }
        } else {
            showState('pending');
        }
    }
});

// Main JavaScript file for Digital Verify Prototype

document.addEventListener("DOMContentLoaded", function () {
    console.log("Digital Verify Prototype Loaded");

    // Toggle reference image helper if present
    const toggleBtn = document.getElementById("toggleRefBtn");
    const refImg = document.getElementById("figmaRefImage");
    if (toggleBtn && refImg) {
        toggleBtn.addEventListener("click", function () {
            if (refImg.style.display === "none") {
                refImg.style.display = "block";
                toggleBtn.innerText = "Hide Figma Reference Screen";
                toggleBtn.classList.replace("btn-secondary", "btn-dark");
            } else {
                refImg.style.display = "none";
                toggleBtn.innerText = "Show Figma Reference Screen";
                toggleBtn.classList.replace("btn-dark", "btn-secondary");
            }
        });
    }
});

// Mobile Drawer Menu Toggle
window.toggleDrawer = function () {
    const drawer = document.getElementById("mobileDrawer");
    const overlay = document.getElementById("drawerOverlay");
    if (drawer && overlay) {
        if (drawer.classList.contains("open")) {
            drawer.classList.remove("open");
            overlay.classList.remove("open");
            overlay.style.opacity = "0";
            setTimeout(() => {
                if (!drawer.classList.contains("open")) {
                    overlay.style.display = "none";
                }
            }, 300);
        } else {
            overlay.style.display = "block";
            setTimeout(() => {
                drawer.classList.add("open");
                overlay.classList.add("open");
                overlay.style.opacity = "1";
            }, 10);
        }
    }
};

// =========================================================================
// Global Review Case Modal Handlers (Dashboard, Queue, All Cases)
// =========================================================================
let currentReviewCaseId = null;

async function openReviewModal(caseId) {
    const modal = document.getElementById('reviewCaseModal');
    if (!modal) return;

    modal.style.display = 'flex';
    
    if (caseId && typeof caseId === 'object') {
        if (caseId.getAttribute) {
            caseId = caseId.getAttribute('data-id');
        } else if (caseId.target) {
            const el = caseId.target.closest('[data-id]');
            caseId = el ? el.getAttribute('data-id') : null;
        }
    }

    if (!caseId) {
        const visibleRow = document.querySelector('tbody tr[data-id]:not([style*="display: none"])');
        const anyRow = document.querySelector('tbody tr[data-id]');
        if (visibleRow) {
            caseId = visibleRow.getAttribute('data-id');
        } else if (anyRow) {
            caseId = anyRow.getAttribute('data-id');
        } else {
            caseId = 1;
        }
    }

    currentReviewCaseId = caseId;

    try {
        const response = await fetch('/api/cases/' + caseId);
        if (!response.ok) return;
        const data = await response.json();
        const c = data.case || {};
        const docs = data.documents || [];

        // Set Header
        const modalTitle = document.getElementById('reviewModalTitle');
        if (modalTitle) {
            const playerSuffix = c.playerName ? ' (' + c.playerName + ')' : '';
            modalTitle.textContent = 'Review Verification - ' + (c.caseNumber || '') + playerSuffix;
        }

        const statusBadge = document.getElementById('reviewCaseStatusBadge');
        if (statusBadge) {
            const st = (c.status || 'PENDING').toLowerCase();
            statusBadge.textContent = st.toUpperCase();
            if (st === 'approved') {
                statusBadge.style.backgroundColor = 'rgba(16, 185, 129, 0.15)';
                statusBadge.style.color = '#10b981';
                statusBadge.style.borderColor = 'rgba(16, 185, 129, 0.3)';
            } else if (st === 'rejected') {
                statusBadge.style.backgroundColor = 'rgba(239, 68, 68, 0.15)';
                statusBadge.style.color = '#ef4444';
                statusBadge.style.borderColor = 'rgba(239, 68, 68, 0.3)';
            } else {
                statusBadge.style.backgroundColor = 'rgba(245, 158, 11, 0.08)';
                statusBadge.style.color = '#f59e0b';
                statusBadge.style.borderColor = 'rgba(245, 158, 11, 0.3)';
            }
        }

        const submittedText = document.getElementById('reviewSubmittedText');
        if (submittedText) {
            submittedText.textContent = '• Submitted: ' + (c.submittedAt ? String(c.submittedAt).replace('T', ' ').substring(0, 16) : 'Recent');
        }

        const assignedText = document.getElementById('reviewAssignedText');
        if (assignedText) {
            assignedText.textContent = c.assignedTo && c.assignedTo !== 'Unassigned' 
                ? '• Assigned to: ' + c.assignedTo 
                : '• Waiting for assignment';
        }

        // Account Info
        const playerNameEl = document.getElementById('reviewPlayerName');
        if (playerNameEl) playerNameEl.textContent = c.playerName || 'N/A';

        const accNum = document.getElementById('reviewAccountNumber');
        if (accNum) accNum.textContent = c.accountNumber || 'N/A';

        const countryEl = document.getElementById('reviewCountry');
        if (countryEl) countryEl.textContent = c.country || 'N/A';

        const accStatusEl = document.getElementById('reviewAccountStatus');
        if (accStatusEl) {
            accStatusEl.textContent = c.accountStatus || 'Active';
            if (c.accountStatus === 'Active') {
                accStatusEl.className = 'badge bg-success bg-opacity-25 text-success border border-success border-opacity-25';
            } else {
                accStatusEl.className = 'badge bg-danger bg-opacity-25 text-danger border border-danger border-opacity-25';
            }
        }

        const reasonEl = document.getElementById('reviewVerificationReason');
        if (reasonEl) reasonEl.innerHTML = '<i class="fa-solid fa-shield-halved text-muted me-1"></i> ' + (c.reason || 'Verification');

        // Verification Details
        const verType = document.getElementById('reviewVerificationType');
        if (verType) verType.textContent = c.reason || 'KYC Check';

        const subDate = document.getElementById('reviewDetailsSubmissionDate');
        if (subDate) subDate.textContent = c.submittedAt ? String(c.submittedAt).replace('T', ' ').substring(0, 16) : '';

        const currStatus = document.getElementById('reviewDetailsStatus');
        if (currStatus) currStatus.textContent = (c.status || 'Pending').toUpperCase();

        const assignedToEl = document.getElementById('reviewDetailsAssigned');
        if (assignedToEl) assignedToEl.innerHTML = '<i class="fa-regular fa-user me-1"></i> ' + (c.assignedTo || 'Unassigned');

        // Documents
        const docsTbody = document.getElementById('reviewDocumentsTbody');
        if (docsTbody) {
            if (docs.length === 0) {
                docsTbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-3 small">No documents attached to this case yet.</td></tr>';
            } else {
                docsTbody.innerHTML = docs.map(doc => {
                    const docUrl = doc.id ? `/api/documents/${doc.id}/view` : (doc.filePath || '#');
                    return `
                    <tr class="border-bottom border-secondary border-opacity-25">
                        <td class="py-3">
                            <div class="fw-bold text-white small">${doc.documentType || 'Identity Document'}</div>
                            <div class="text-muted" style="font-size: 11px;">${doc.fileName || 'file'}</div>
                        </td>
                        <td><span class="badge ${doc.status === 'VERIFIED' || doc.status === 'APPROVED' ? 'bg-success' : 'bg-warning text-dark'} px-2" style="font-size: 10px; font-weight: 700; text-transform: uppercase;">${doc.status || 'PENDING'}</span></td>
                        <td style="text-align: center;"><i class="fa-regular fa-eye text-muted cursor-pointer" style="font-size: 14px;" onclick="window.open('${docUrl}', '_blank');"></i></td>
                        <td style="text-align: right;"><button class="btn-table-action" style="font-size: 11px; padding: 4px 10px;" onclick="window.open('${docUrl}', '_blank');">Open Document</button></td>
                    </tr>
                `;
                }).join('');
            }
        }

        // Timeline
        const stepSubDate = document.getElementById('reviewStepSubmittedDate');
        if (stepSubDate) stepSubDate.innerHTML = c.submittedAt ? String(c.submittedAt).replace('T', '<br>') : 'Recent';

        const stepAssignedText = document.getElementById('reviewStepAssignedText');
        const stepAssignedIcon = document.getElementById('reviewStepAssignedIcon');
        if (c.assignedTo && c.assignedTo !== 'Unassigned') {
            if (stepAssignedText) stepAssignedText.textContent = c.assignedTo;
            if (stepAssignedIcon) {
                stepAssignedIcon.style.borderColor = 'var(--winarena-primary)';
                stepAssignedIcon.style.backgroundColor = 'var(--winarena-primary)';
                stepAssignedIcon.style.color = '#ffffff';
            }
        } else {
            if (stepAssignedText) stepAssignedText.textContent = 'Not assigned';
            if (stepAssignedIcon) {
                stepAssignedIcon.style.borderColor = 'var(--winarena-border)';
                stepAssignedIcon.style.backgroundColor = '#070a14';
                stepAssignedIcon.style.color = 'var(--winarena-text-muted)';
            }
        }

        // Decision radio synchronization
        const decRadio1 = document.getElementById('decRadio1');
        const decRadio2 = document.getElementById('decRadio2');
        const decRadio3 = document.getElementById('decRadio3');
        if (c.status === 'rejected') {
            if (decRadio2) decRadio2.checked = true;
        } else if (c.status === 'escalated') {
            if (decRadio3) decRadio3.checked = true;
        } else {
            if (decRadio1) decRadio1.checked = true;
        }

        // Account status decision select
        const accDecisionEl = document.getElementById('reviewAccountStatusDecision');
        if (accDecisionEl) {
            if (c.accountStatus === 'Suspended') {
                accDecisionEl.value = 'Keep Suspended';
            } else {
                accDecisionEl.value = 'Reactivate Account';
            }
        }

        // Comments
        const commentsEl = document.getElementById('reviewComments');
        if (commentsEl) commentsEl.value = c.decisionNotes || '';

    } catch (e) {
        console.error('Error fetching case details:', e);
    }
}

function closeReviewModal() {
    const modal = document.getElementById('reviewCaseModal');
    if (modal) modal.style.display = 'none';
}

async function submitCaseDecision() {
    if (!currentReviewCaseId) {
        alert('No hay un caso seleccionado.');
        return;
    }

    const commentsEl = document.getElementById('reviewComments');
    const decisionNotes = commentsEl ? commentsEl.value.trim() : '';
    if (!decisionNotes) {
        alert('Por favor ingrese un comentario sobre la decisión.');
        return;
    }

    let status = 'approved';
    let verDecision = 'Approve Verification';
    const decRadio2 = document.getElementById('decRadio2');
    const decRadio3 = document.getElementById('decRadio3');
    if (decRadio2 && decRadio2.checked) {
        status = 'rejected';
        verDecision = 'Reject Verification (Needs Reupload)';
    } else if (decRadio3 && decRadio3.checked) {
        status = 'escalated';
        verDecision = 'Escalate to Compliance';
    }

    const accDecisionEl = document.getElementById('reviewAccountStatusDecision');
    const accountStatusDecision = accDecisionEl ? accDecisionEl.value : (status === 'approved' ? 'Reactivate Account' : 'Keep Suspended');

    try {
        const response = await fetch('/api/cases/' + currentReviewCaseId + '/decision', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                status: status,
                verificationDecision: verDecision,
                accountStatusDecision: accountStatusDecision,
                decisionNotes: decisionNotes,
                comments: decisionNotes
            })
        });

        const resData = await response.json();
        if (resData.success || response.ok) {
            alert('¡Decisión registrada exitosamente para el caso!');
            closeReviewModal();
            window.location.reload();
        } else {
            alert('Error: ' + (resData.message || 'No se pudo guardar la decisión.'));
        }
    } catch (err) {
        alert('Error de conexión al registrar la decisión.');
    }
}

async function assignCurrentCase() {
    if (!currentReviewCaseId) return;

    try {
        const response = await fetch('/api/cases/' + currentReviewCaseId + '/assign', {
            method: 'POST'
        });
        const resData = await response.json();
        if (resData.success) {
            alert('¡Caso asignado exitosamente!');
            openReviewModal(currentReviewCaseId);
        }
    } catch (err) {
        alert('Error al asignar el caso.');
    }
}

// Explicit window bindings for global accessibility
window.openReviewModal = openReviewModal;
window.closeReviewModal = closeReviewModal;
window.submitCaseDecision = submitCaseDecision;
window.assignCurrentCase = assignCurrentCase;

// Global click delegation & modal listeners to ensure reviewCaseModal opens reliably
document.addEventListener('click', function(e) {
    const btnAction = e.target.closest('.btn-table-action');
    if (btnAction) {
        const tr = btnAction.closest('tr');
        const caseId = btnAction.getAttribute('data-id') || tr?.getAttribute('data-id');
        if (caseId) {
            e.preventDefault();
            openReviewModal(caseId);
            return;
        }
    }

    const caseIdLink = e.target.closest('td[style*="cursor: pointer"]');
    if (caseIdLink) {
        const tr = caseIdLink.closest('tr');
        const caseId = caseIdLink.getAttribute('data-id') || tr?.getAttribute('data-id');
        if (caseId) {
            e.preventDefault();
            openReviewModal(caseId);
            return;
        }
    }

    // Modal background overlay click
    const reviewOverlay = document.getElementById('reviewCaseModal');
    if (e.target === reviewOverlay) {
        closeReviewModal();
    }
});

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeReviewModal();
    }
});

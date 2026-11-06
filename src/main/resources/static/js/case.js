document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const state = urlParams.get('state');

    const backLink = document.getElementById("backLink");
    const approveBtn = document.getElementById("approveBtn");
    const vrPopupOverlay = document.getElementById("vrPopupOverlay");
    const caseStatusBadge = document.getElementById("caseStatusBadge");

    // Update back link and status if already validated
    if (state === "validated") {
        backLink.href = "dashboard.html?state=validated";
        caseStatusBadge.className = "status-sent-vr";
        caseStatusBadge.textContent = "Sent to VR";
    }

    approveBtn.addEventListener("click", function () {
        // Show Screen 3 overlay popup
        vrPopupOverlay.style.display = "flex";
    });
});

function closePopup() {
    document.getElementById("vrPopupOverlay").style.display = "none";
}

function redirectToDashboard() {
    // Redirect to Screen 4 (Validation Dashboard)
    window.location.href = "dashboard.html?state=validated";
}

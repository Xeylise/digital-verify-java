function openModal(type) {
    closeModals();
    if (type === 'login') {
        const modal = document.getElementById('loginModalOverlay');
        if (modal) modal.style.display = 'flex';
    } else if (type === 'register') {
        const modal = document.getElementById('registerModalOverlay');
        if (modal) modal.style.display = 'flex';
    }
}

function closeModals() {
    const loginOverlay = document.getElementById('loginModalOverlay');
    const registerOverlay = document.getElementById('registerModalOverlay');
    if (loginOverlay) loginOverlay.style.display = 'none';
    if (registerOverlay) registerOverlay.style.display = 'none';
}

// NOTA: El modal NO se cierra al hacer clic fuera del card ni con ESC,
// únicamente al hacer clic en el botón de cerrar 'X' (closeModals).

document.addEventListener("DOMContentLoaded", function () {
    // Password Toggle for Login
    const togglePasswordBtn = document.getElementById("togglePasswordBtn");
    const passwordInput = document.getElementById("password");
    const eyeIcon = document.getElementById("eyeIcon");

    if (togglePasswordBtn && passwordInput && eyeIcon) {
        togglePasswordBtn.addEventListener("click", function () {
            if (passwordInput.type === "password") {
                passwordInput.type = "text";
                eyeIcon.classList.replace("fa-eye", "fa-eye-slash");
            } else {
                passwordInput.type = "password";
                eyeIcon.classList.replace("fa-eye-slash", "fa-eye");
            }
        });
    }

    // Password Toggle for Register
    const toggleRegPasswordBtn = document.getElementById("toggleRegPasswordBtn");
    const regPasswordInput = document.getElementById("reg_password");
    const regEyeIcon = document.getElementById("regEyeIcon");

    if (toggleRegPasswordBtn && regPasswordInput && regEyeIcon) {
        toggleRegPasswordBtn.addEventListener("click", function () {
            if (regPasswordInput.type === "password") {
                regPasswordInput.type = "text";
                regEyeIcon.classList.replace("fa-eye", "fa-eye-slash");
            } else {
                regPasswordInput.type = "password";
                regEyeIcon.classList.replace("fa-eye-slash", "fa-eye");
            }
        });
    }

    // Auto-open modal if server-side error exists on page load
    const errorCard = document.getElementById("errorCard");
    if (errorCard && errorCard.style.display === "block") {
        openModal('login');
    }

    // Interactive Login with Client Validation and Success Modal
    const loginForm = document.getElementById("loginForm");
    if (loginForm) {
        loginForm.addEventListener("submit", async function (e) {
            e.preventDefault();

            const emailInput = document.getElementById("email");
            const passInput = document.getElementById("password");
            const submitBtn = loginForm.querySelector("button[type='submit']");
            const errorSubtitle = errorCard ? errorCard.querySelector(".error-card-subtitle") : null;

            const emailVal = emailInput ? emailInput.value.trim() : "";
            const passVal = passInput ? passInput.value.trim() : "";

            // Client-side validations
            if (!emailVal || !passVal) {
                if (errorCard && errorSubtitle) {
                    errorSubtitle.textContent = "El correo electrónico y la contraseña son obligatorios.";
                    errorCard.style.display = "block";
                }
                return;
            }

            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailPattern.test(emailVal)) {
                if (errorCard && errorSubtitle) {
                    errorSubtitle.textContent = "Por favor ingresa un correo electrónico válido.";
                    errorCard.style.display = "block";
                }
                return;
            }

            // Show loading state on button
            const originalBtnText = submitBtn ? submitBtn.innerHTML : "Log in";
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Validando...';
            }

            try {
                const formData = new FormData(loginForm);
                const response = await fetch("/login", {
                    method: "POST",
                    body: new URLSearchParams(formData),
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        "X-Requested-With": "XMLHttpRequest"
                    }
                });

                // Check if response redirected to authenticated page
                if (response.redirected && (response.url.includes("/home.html") || response.url.includes("/dashboard.html"))) {
                    // Hide login modal and show success modal
                    closeModals();
                    const successModal = document.getElementById("clientLoginSuccessModal");
                    if (successModal) {
                        successModal.style.display = "flex";
                    }
                    setTimeout(() => {
                        window.location.href = response.url;
                    }, 1200);
                    return;
                }

                // If not redirected or server returned status 200/400 (reloaded index with error)
                const htmlText = await response.text();
                const parser = new DOMParser();
                const doc = parser.parseFromString(htmlText, "text/html");
                const serverErrorEl = doc.getElementById("errorCard");

                let errorMessage = "Credenciales incorrectas. Verifica tu correo y contraseña.";
                if (serverErrorEl) {
                    const msgEl = serverErrorEl.querySelector(".error-card-subtitle");
                    if (msgEl && msgEl.textContent.trim()) {
                        errorMessage = msgEl.textContent.trim();
                    }
                }

                if (errorCard && errorSubtitle) {
                    errorSubtitle.textContent = errorMessage;
                    errorCard.style.display = "block";
                }

                // Keep modal open and re-enable submit
                openModal('login');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalBtnText;
                }
            } catch (err) {
                // Fallback to normal form submit if fetch fails
                loginForm.submit();
            }
        });
    }
});

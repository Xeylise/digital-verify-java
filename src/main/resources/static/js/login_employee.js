function openLoginModal() {
    const overlay = document.getElementById('employeeLoginOverlay');
    if (overlay) {
        overlay.style.display = 'flex';
    }
}

function closeLoginModal() {
    const overlay = document.getElementById('employeeLoginOverlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

// NOTA: El modal NO se cierra al hacer clic en el backdrop/overlay,
// únicamente al presionar el botón 'X' (closeLoginModal).
window.addEventListener('click', function (e) {
    const overlay = document.getElementById('employeeLoginOverlay');
    if (e.target === overlay) {
        e.stopPropagation();
        e.stopImmediatePropagation();
        e.preventDefault();
    }
}, true);

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("employeeLoginForm");
    const overlay = document.getElementById("employeeLoginOverlay");

    if (form) {
        form.addEventListener("submit", async function (e) {
            e.preventDefault();

            const emailInput = document.getElementById("employee_email");
            const passInput = document.getElementById("emp_password");
            const submitBtn = form.querySelector("button[type='submit']");
            let errorAlert = document.getElementById("employeeErrorAlert");

            const emailVal = emailInput ? emailInput.value.trim() : "";
            const passVal = passInput ? passInput.value.trim() : "";

            // Client-side validations
            if (!emailVal || !passVal) {
                showError("El correo de validador y la contraseña son obligatorios.");
                return;
            }

            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailPattern.test(emailVal)) {
                showError("Por favor ingresa un correo electrónico corporativo válido.");
                return;
            }

            const originalBtnText = submitBtn ? submitBtn.innerHTML : "Log in";
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Autenticando...';
            }

            try {
                const formData = new FormData(form);
                const response = await fetch("/login_employee", {
                    method: "POST",
                    body: new URLSearchParams(formData),
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        "X-Requested-With": "XMLHttpRequest"
                    }
                });

                if (response.redirected && response.url.includes("/dashboard.html")) {
                    closeLoginModal();
                    const successModal = document.getElementById("employeeLoginSuccessModal");
                    if (successModal) {
                        successModal.style.display = "flex";
                    }
                    setTimeout(() => {
                        window.location.href = response.url;
                    }, 1200);
                    return;
                }

                // If credentials failed, read response HTML error
                const htmlText = await response.text();
                const parser = new DOMParser();
                const doc = parser.parseFromString(htmlText, "text/html");
                const serverError = doc.getElementById("employeeErrorAlert") || doc.querySelector(".alert-danger");

                let message = "Credenciales de validador inválidas o acceso no autorizado.";
                if (serverError && serverError.textContent.trim()) {
                    message = serverError.textContent.trim();
                }

                showError(message);
                openLoginModal();

                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalBtnText;
                }
            } catch (err) {
                // Fallback to native submission
                form.submit();
            }

            function showError(msg) {
                if (!errorAlert) {
                    errorAlert = document.createElement("div");
                    errorAlert.id = "employeeErrorAlert";
                    errorAlert.className = "alert alert-danger py-2 mb-3";
                    errorAlert.style.fontSize = "13px";
                    errorAlert.style.borderRadius = "8px";
                    form.insertBefore(errorAlert, form.firstChild);
                }
                errorAlert.textContent = msg;
                errorAlert.style.display = "block";
            }
        });
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const togglePasswordBtn = document.getElementById("togglePasswordBtn");
    const passwordInput = document.getElementById("password");
    const eyeIcon = document.getElementById("eyeIcon");

    togglePasswordBtn.addEventListener("click", function () {
        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            eyeIcon.classList.replace("fa-eye", "fa-eye-slash");
        } else {
            passwordInput.type = "password";
            eyeIcon.classList.replace("fa-eye-slash", "fa-eye");
        }
    });
});

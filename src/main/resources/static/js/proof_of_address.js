function handleFileSelected() {
    const input = document.getElementById('fileAddress');
    const label = document.getElementById('labelAddress');
    const icon = document.getElementById('iconAddress');

    if (input.files && input.files.length > 0) {
        const fileName = input.files[0].name;
        label.innerText = "Selected: " + (fileName.length > 25 ? fileName.substring(0, 22) + "..." : fileName);
        label.style.color = "var(--winarena-primary)";
        icon.style.color = "var(--winarena-primary)";
        icon.style.borderColor = "var(--winarena-primary)";
    }
}

function openSuccessModal() {
    document.getElementById('successModalOverlay').style.display = 'flex';
}

function closeSuccessModal() {
    document.getElementById('successModalOverlay').style.display = 'none';
}

// Close when clicking outside modal card
window.addEventListener('click', function (e) {
    const successOverlay = document.getElementById('successModalOverlay');
    if (e.target === successOverlay) {
        closeSuccessModal();
    }
});

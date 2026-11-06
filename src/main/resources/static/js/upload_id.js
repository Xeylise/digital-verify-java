function handleFileSelected(side) {
    const input = document.getElementById('file' + side.charAt(0).toUpperCase() + side.slice(1));
    const label = document.getElementById('label' + side.charAt(0).toUpperCase() + side.slice(1));
    const icon = document.getElementById('icon' + side.charAt(0).toUpperCase() + side.slice(1));

    if (input.files && input.files.length > 0) {
        const fileName = input.files[0].name;
        label.innerText = "Selected: " + (fileName.length > 25 ? fileName.substring(0, 22) + "..." : fileName);
        label.style.color = "#00d2ff";
        icon.style.color = "#00d2ff";
        icon.style.borderColor = "#00d2ff";
    }
}

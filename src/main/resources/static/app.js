// === Replacing hardcoded sampleTasks with dynamic backend data ===

// API endpoint URLs
const API_BASE = "/api/tasks"; // Change if deployed

// Utility function to get formatted date string
function formatDate(date) {
    return date.toISOString().split('T')[0];
}

// Get yesterday, today, and tomorrow dates
const today = new Date();
const yesterday = new Date(today);
yesterday.setDate(today.getDate() - 1);
const tomorrow = new Date(today);
tomorrow.setDate(today.getDate() + 1);

const dateMap = {
    yesterday: formatDate(yesterday),
    today: formatDate(today),
    tomorrow: formatDate(tomorrow)
};

async function fetchTasksByDate(date) {
    const response = await fetch(`${API_BASE}/date/${date}`);
    if (!response.ok) {
        console.error(`Failed to fetch tasks for ${date}. Status: ${response.status}`);
        const errorText = await response.text();
        console.error(`Error details: ${errorText}`);
        return [];
    }
    return await response.json();
}

async function updateTaskStatus(taskId, status) {
    const response = await fetch(`${API_BASE}/${taskId}/status?status=${status}`, {
        method: 'PUT'
    });
    if (!response.ok) {
        alert('Failed to update task status');
    }
}

async function loadTasksFromJson() {
    try {
        const response = await fetch(`${API_BASE}/load`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({}) // Sending an empty JSON object as the body
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`HTTP error! Status: ${response.status}, Body: ${errorText}`);
            throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }
        const message = await response.text();
        alert('Reschedule Tasks operation successful: ' + message);
        await refreshAllTasks(); // Refresh tasks after loading new ones
    } catch (error) {
        console.error('Error loading tasks:', error);
        alert('Failed to reschedule tasks: ' + error.message);
    }
}

async function refreshAllTasks() {
    await displayTasksForDate('Yesterday', '#yesterday-tasks', dateMap.yesterday);
    await displayTasksForDate('Today', '#today-tasks', dateMap.today);
    await displayTasksForDate('Tomorrow', '#tomorrow-tasks', dateMap.tomorrow);
}

function createTaskCard(task) {
    const taskElement = document.createElement('div');
    taskElement.className = 'task';

    const titleElement = document.createElement('div');
    titleElement.className = 'task-title';
    titleElement.textContent = task.title;
    taskElement.appendChild(titleElement);

    const categoryElement = document.createElement('div');
    categoryElement.className = 'task-category';
    categoryElement.textContent = task.category;
    taskElement.appendChild(categoryElement);

    const detailsElement = document.createElement('div');
    detailsElement.className = 'task-details';
    detailsElement.innerHTML = `
        <div>Scheduled Date: ${task.scheduledDate}</div>
        <div>Estimated Hours: ${task.estimatedHours}</div>
        <div>Status: ${task.status}</div>
    `;
    taskElement.appendChild(detailsElement);

    const statusSelect = document.createElement('select');
    statusSelect.className = 'task-status-select';
    statusSelect.dataset.taskId = task.id;

    const statusOptions = ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'PENDING'];
    statusOptions.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option;
        optionElement.textContent = option.replace('_', ' ').toUpperCase();
        if (option === task.status) {
            optionElement.selected = true;
        }
        statusSelect.appendChild(optionElement);
    });

    statusSelect.addEventListener('change', async (event) => {
        const newStatus = event.target.value;
        const taskId = event.target.dataset.taskId;
        await updateTaskStatus(taskId, newStatus);
        alert(`Task status updated to ${newStatus}`);
    });

    taskElement.appendChild(statusSelect);
    return taskElement;
}

async function displayTasksForDate(label, containerSelector, dateStr) {
    const container = document.querySelector(containerSelector);
    container.innerHTML = '<p>Loading...</p>';
    const tasks = await fetchTasksByDate(dateStr);
    container.innerHTML = '';

    if (tasks.length === 0) {
        container.innerHTML = '<p>No tasks found.</p>';
        return;
    }

    tasks.forEach(task => {
        container.appendChild(createTaskCard(task));
    });
}

document.addEventListener('DOMContentLoaded', () => {
    refreshAllTasks();

    const rescheduleButton = document.getElementById('reschedule-button');
    if (rescheduleButton) {
        rescheduleButton.addEventListener('click', loadTasksFromJson);
    }
});
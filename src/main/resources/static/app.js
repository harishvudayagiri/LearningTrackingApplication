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
        console.error(`Failed to fetch tasks for ${date}`);
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
    displayTasksForDate('Yesterday', '#yesterday-tasks', dateMap.yesterday);
    displayTasksForDate('Today', '#today-tasks', dateMap.today);
    displayTasksForDate('Tomorrow', '#tomorrow-tasks', dateMap.tomorrow);
});

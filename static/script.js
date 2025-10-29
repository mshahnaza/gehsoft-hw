const currentPort = window.location.port || 8080;
document.getElementById('server-port').textContent = currentPort;
document.getElementById('stat-port').textContent = currentPort;

const serverType = currentPort === '8080' ? 'Virtual Threads' : 'Platform Threads';
document.getElementById('server-type').textContent = serverType;

document.getElementById('echoForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(e.target);
    const message = formData.get('message');
    const responseBox = document.getElementById('response');
    const submitBtn = e.target.querySelector('button[type="submit"]');

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Sending...';
    responseBox.textContent = 'Waiting for response...';
    responseBox.classList.add('show');

    try {
        const startTime = Date.now();
        const response = await fetch('/api/echo', {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain' },
            body: message
        });

        const duration = Date.now() - startTime;
        const text = await response.text();

        responseBox.textContent = `✓ Response received in ${duration}ms:\n\n${text}`;
        responseBox.style.color = 'var(--success)';

        e.target.reset();

    } catch (error) {
        responseBox.textContent = `✗ Error: ${error.message}`;
        responseBox.style.color = 'var(--danger)';
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Send Echo Request';
    }
});

async function updateStats() {
    try {
        const response = await fetch('/api/stats');
        const data = await response.json();

        document.getElementById('stat-requests').textContent =
            data.requestsServed.toLocaleString();

        const hours = Math.floor(data.uptimeSeconds / 3600);
        const minutes = Math.floor((data.uptimeSeconds % 3600) / 60);
        const seconds = data.uptimeSeconds % 60;
        document.getElementById('stat-uptime').textContent =
            `${hours}h ${minutes}m ${seconds}s`;

        document.getElementById('stat-connections').textContent =
            data.webSocketConnections || 0;

        console.log('Stats updated:', data);

    } catch (error) {
        console.error('Failed to fetch stats:', error);
        document.getElementById('stat-requests').textContent = 'Error';
        document.getElementById('stat-uptime').textContent = 'Error';
        document.getElementById('stat-connections').textContent = 'Error';
    }
}

document.getElementById('refresh-stats').addEventListener('click', async (e) => {
    const btn = e.target;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Refreshing...';

    await updateStats();

    btn.disabled = false;
    btn.textContent = 'Refresh Statistics';
});

let statsInterval = setInterval(updateStats, 10000);

updateStats();

let requestCount = 0;
let totalResponseTime = 0;

const originalFetch = window.fetch;
window.fetch = function(...args) {
    const start = Date.now();
    return originalFetch.apply(this, args).then(response => {
        const duration = Date.now() - start;
        totalResponseTime += duration;
        requestCount++;

        if (requestCount % 10 === 0) {
            const avgTime = (totalResponseTime / requestCount).toFixed(2);
            console.log(`Performance: ${requestCount} requests, avg ${avgTime}ms`);
        }

        return response;
    });
};

window.addEventListener('load', () => {
    const loadTime = performance.timing.loadEventEnd - performance.timing.navigationStart;
    console.log(`
╔═══════════════════════════════════════════════════════╗
║           Custom Web Server - Client Side             ║
╚═══════════════════════════════════════════════════════╝

   Page Performance:
   Load Time: ${loadTime}ms
   Server: ${serverType}
   Port: ${currentPort}

   Keyboard Shortcuts:
   Ctrl+R - Refresh statistics
   
   Available in console:
   updateStats() - Refresh server statistics
   testPerformance() - Run performance test
    `);
});

document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
        e.preventDefault();
        updateStats();
        console.log('Statistics refreshed');
    }
});

window.addEventListener('beforeunload', () => {
    if (statsInterval) {
        clearInterval(statsInterval);
    }
});

async function testPerformance(requestCount = 10) {
    console.log(`Starting performance test with ${requestCount} requests...`);

    const results = [];
    const startTime = Date.now();

    for (let i = 0; i < requestCount; i++) {
        const reqStart = Date.now();
        try {
            await fetch('/api/time');
            results.push(Date.now() - reqStart);
        } catch (error) {
            console.error(`Request ${i + 1} failed:`, error);
        }
    }

    const totalTime = Date.now() - startTime;
    const avgTime = results.reduce((a, b) => a + b, 0) / results.length;
    const minTime = Math.min(...results);
    const maxTime = Math.max(...results);

    console.log(`
   Performance Test Results:
   Total Requests: ${requestCount}
   Total Time: ${totalTime}ms
   Average Response: ${avgTime.toFixed(2)}ms
   Min Response: ${minTime}ms
   Max Response: ${maxTime}ms
   Throughput: ${(requestCount / (totalTime / 1000)).toFixed(2)} req/s
    `);

    return { totalTime, avgTime, minTime, maxTime };
}

window.updateStats = updateStats;
window.testPerformance = testPerformance;

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

document.querySelectorAll('section').forEach(section => {
    section.style.opacity = '0';
    section.style.transform = 'translateY(30px)';
    section.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
    observer.observe(section);
});
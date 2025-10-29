import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const VIRTUAL_SERVER = 'http://localhost:8080';
const PLATFORM_SERVER = 'http://localhost:8081';

const virtualTime = new Trend('virtual_response_time');
const platformTime = new Trend('platform_response_time');

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'],
        'http_req_failed': ['rate<0.01'],
        'virtual_response_time': ['p(95)<300'],
        'platform_response_time': ['p(95)<500'],
    },
};

export default function() {
    let res = http.get(`${VIRTUAL_SERVER}/`);
    check(res, { 'virtual homepage': (r) => r.status === 200 });
    virtualTime.add(res.timings.duration);

    res = http.get(`${VIRTUAL_SERVER}/api/time`);
    check(res, { 'virtual time api': (r) => r.status === 200 });
    virtualTime.add(res.timings.duration);

    res = http.get(`${VIRTUAL_SERVER}/api/stats`);
    check(res, { 'virtual stats api': (r) => r.status === 200 });
    virtualTime.add(res.timings.duration);

    res = http.post(`${VIRTUAL_SERVER}/api/echo`, 'test', {
        headers: { 'Content-Type': 'text/plain' }
    });
    check(res, { 'virtual echo api': (r) => r.status === 200 });
    virtualTime.add(res.timings.duration);

    sleep(0.5);

    res = http.get(`${PLATFORM_SERVER}/`);
    check(res, { 'platform homepage': (r) => r.status === 200 });
    platformTime.add(res.timings.duration);

    res = http.get(`${PLATFORM_SERVER}/api/time`);
    check(res, { 'platform time api': (r) => r.status === 200 });
    platformTime.add(res.timings.duration);

    res = http.get(`${PLATFORM_SERVER}/api/stats`);
    check(res, { 'platform stats api': (r) => r.status === 200 });
    platformTime.add(res.timings.duration);

    res = http.post(`${PLATFORM_SERVER}/api/echo`, 'test', {
        headers: { 'Content-Type': 'text/plain' }
    });
    check(res, { 'platform echo api': (r) => r.status === 200 });
    platformTime.add(res.timings.duration);

    sleep(0.5);
}

export function handleSummary(data) {
    const vTime = data.metrics.virtual_response_time?.values?.avg || 0;
    const pTime = data.metrics.platform_response_time?.values?.avg || 0;
    const improvement = pTime > 0 ? ((pTime - vTime) / pTime * 100).toFixed(2) : 0;

    console.log('\n' + '='.repeat(70));
    console.log('LOAD TEST RESULTS - Virtual vs Platform Threads');
    console.log('='.repeat(70));
    console.log(`\nTotal Requests: ${data.metrics.http_reqs?.values?.count || 0}`);
    console.log(`Requests/sec: ${(data.metrics.http_reqs?.values?.rate || 0).toFixed(2)}`);
    console.log(`\nSuccess Rate: ${((data.metrics.checks?.values?.rate || 0) * 100).toFixed(2)}%`);

    console.log('\nSERVER COMPARISON:');
    console.log(`   Virtual Threads (8080):  ${vTime.toFixed(2)}ms avg, ${(data.metrics.virtual_response_time?.values['p(95)'] || 0).toFixed(2)}ms p95`);
    console.log(`   Platform Threads (8081): ${pTime.toFixed(2)}ms avg, ${(data.metrics.platform_response_time?.values['p(95)'] || 0).toFixed(2)}ms p95`);
    console.log(`   Improvement: ${improvement}% ${parseFloat(improvement) >= 0 ? 'FASTER' : 'SLOWER'}`);
    console.log('='.repeat(70) + '\n');

    return {
        'stdout': '',
    };
}
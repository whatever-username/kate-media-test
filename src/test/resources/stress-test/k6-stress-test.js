import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    // { duration: '1m', target: 200 },
    { duration: '1m', target: 400 },
    // { duration: '1m', target: 0 },
  ],
};

// Helper function to generate random string for counter names
function randomString(length) {
  const chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export default function () {
  // Create a new counter
  let counterName = randomString(10);
  let createRes = http.post('http://localhost:8080/api/v1/counter', JSON.stringify({ name: counterName, value: 0 }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(createRes, {
    'create status was 201': (r) => r.status === 201,
  });

  // Perform 1000 increments asynchronously
  let incrementRequests = Array.from({ length: 100 }, () => ({
    method: 'POST',
    url: `http://localhost:8080/api/v1/counter/${counterName}/increment`,
  }));

  let incrementResponses = http.batch(incrementRequests);
  let incrementChecks = incrementResponses.every(res => res.status === 200);
  check(incrementChecks, {
    'all increments succeeded': () => incrementChecks,
  });

  // Retrieve the counter
  let getRes = http.get(`http://localhost:8080/api/v1/counter/${counterName}`);
  check(getRes, {
    'get status was 200': (r) => r.status === 200,
    'counter value is 100': (r) => r.json() === 100,
  });

  // Simulate some wait time between requests
  sleep(1);
}

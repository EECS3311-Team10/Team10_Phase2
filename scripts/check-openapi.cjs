/* Dependency-free structural/contract checks; not a replacement for an OpenAPI linter. */
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const spec = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../openapi/consultlink-v1.json'), 'utf8'));
const resolve = node => {
  if (!node.$ref) return node;
  assert(node.$ref.startsWith('#/'), `External reference unsupported: ${node.$ref}`);
  const result = node.$ref.slice(2).split('/').reduce((part, key) => part?.[key.replace(/~1/g, '/').replace(/~0/g, '~')], spec);
  assert(result, `Unresolved reference ${node.$ref}`);
  return result;
};
function walk(node) {
  if (!node || typeof node !== 'object') return;
  if (node.$ref) resolve(node);
  if (node.type === 'object' && node.properties) {
    for (const required of node.required || []) assert(required in node.properties, `Required field absent: ${required}`);
  }
  for (const child of Object.values(node)) walk(child);
}
walk(spec);
assert.equal(spec.openapi, '3.0.3');
assert.deepEqual(spec.servers.map(s => s.url), ['/api/v1']);
const ids = new Set();
let count = 0;
for (const [route, methods] of Object.entries(spec.paths)) {
  assert(route.startsWith('/'));
  for (const [method, operation] of Object.entries(methods)) {
    count++;
    assert(!ids.has(operation.operationId), `Duplicate operation ID ${operation.operationId}`);
    ids.add(operation.operationId);
    assert(operation.security, `Security must be explicit: ${route}`);
    assert(operation['x-roles']?.length, `Missing role scope: ${route}`);
    const parameters = operation.parameters.map(resolve);
    const names = parameters.map(p => `${p.in}:${p.name}`);
    assert.equal(new Set(names).size, names.length, `Duplicate parameters: ${route}`);
    const expected = [...route.matchAll(/\{(\w+)\}/g)].map(m => m[1]).sort();
    const actual = parameters.filter(p => p.in === 'path').map(p => {
      assert.equal(p.required, true, `Optional path parameter: ${route}`);
      return p.name;
    }).sort();
    assert.deepEqual(actual, expected, `Path parameter mismatch: ${route}`);
    assert(Object.keys(operation.responses).some(s => /^2\d\d$/.test(s)), `No success response: ${route}`);
    for (const status of ['400','403','404','409']) assert(operation.responses[status], `Missing ${status}: ${route}`);
    if (operation.security.length) assert(operation.responses['401'], `Missing 401: ${route}`);
    if (method !== 'get') assert(parameters.some(p => p.name === 'X-CSRF-TOKEN' && p.required), `Missing CSRF: ${route}`);
    if (operation.requestBody) {
      const body = resolve(operation.requestBody.content['application/json'].schema);
      assert.equal(body.type, 'object');
      assert.equal(body.additionalProperties, false, `Writable DTO accepts unknown fields: ${route}`);
    }
    for (const [status, response] of Object.entries(operation.responses)) {
      if (status === '204') assert(!response.content, `204 must have no body: ${route}`);
      if (+status >= 400) assert(response.content?.['application/problem+json'], `Inconsistent error shape: ${route}`);
    }
  }
}
for (const id of ['createBooking','payBooking','cancelBooking']) {
  const operation = Object.values(spec.paths).flatMap(Object.values).find(op => op.operationId === id);
  assert(operation.parameters.map(resolve).some(p => p.name === 'Idempotency-Key' && p.required), `${id} must be idempotent`);
}
for (const name of ['User','Consultant','Payment','Refund']) {
  const json = JSON.stringify(spec.components.schemas[name]);
  assert(!/password_hash|passwordHash|cardNumber|cvv/i.test(json), `Sensitive response field: ${name}`);
}
assert(!('price' in spec.components.schemas.CreateBookingRequest.properties));
assert(!('clientId' in spec.components.schemas.CreateBookingRequest.properties));
console.log(`PASS: ${count} operations, ${ids.size} unique IDs, all references/paths/security/error/DTO checks.`);

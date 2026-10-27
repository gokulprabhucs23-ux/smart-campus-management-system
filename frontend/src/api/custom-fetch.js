const NO_BODY_STATUS = new Set([204, 205, 304]);
const DEFAULT_JSON_ACCEPT = "application/json, application/problem+json";

// ---------------------------------------------------------------------------
// Module-level configuration
// ---------------------------------------------------------------------------

let _baseUrl =
  typeof import.meta !== "undefined" &&
  (import.meta.env?.VITE_API_URL || import.meta.env?.VITE_BACKEND_URL)
    ? (import.meta.env.VITE_API_URL || import.meta.env.VITE_BACKEND_URL).replace(/\/+$/, "")
    : null;
let _authTokenGetter = null;

/**
 * Set a base URL that is prepended to every relative request URL
 * (i.e. paths that start with `/`).
 *
 * Useful for Expo bundles that need to call a remote API server.
 * Pass `null` to clear the base URL.
 */
export function setBaseUrl(url) {
  _baseUrl = url ? url.replace(/\/+$/, "") : null;
}

/**
 * Register a getter that supplies a bearer auth token.  Before every fetch
 * the getter is invoked; when it returns a non-null string, an
 * `Authorization: Bearer <token>` header is attached to the request.
 *
 * Useful for Expo bundles making token-gated API calls.
 * Pass `null` to clear the getter.
 *
 * NOTE: This function should never be used in web applications where session
 * token cookies are automatically associated with API calls by the browser.
 */
export function setAuthTokenGetter(getter) {
  _authTokenGetter = getter;
}

function isRequest(input) {
  return typeof Request !== "undefined" && input instanceof Request;
}

function resolveMethod(input, explicitMethod) {
  if (explicitMethod) return explicitMethod.toUpperCase();
  if (isRequest(input)) return input.method.toUpperCase();
  return "GET";
}

// Use loose check for URL — some runtimes (e.g. React Native) polyfill URL
// differently, so `instanceof URL` can fail.
function isUrl(input) {
  return typeof URL !== "undefined" && input instanceof URL;
}

function applyBaseUrl(input) {
  if (!_baseUrl) return input;
  const url = resolveUrl(input);
  // Only prepend to relative paths (starting with /)
  if (!url.startsWith("/")) return input;

  const absolute = `${_baseUrl}${url}`;
  if (typeof input === "string") return absolute;
  if (isUrl(input)) return new URL(absolute);
  return new Request(absolute, input);
}

function resolveUrl(input) {
  if (typeof input === "string") return input;
  if (isUrl(input)) return input.toString();
  return input.url;
}

function mergeHeaders(...sources) {
  const headers = new Headers();

  for (const source of sources) {
    if (!source) continue;
    new Headers(source).forEach((value, key) => {
      headers.set(key, value);
    });
  }

  return headers;
}

function getMediaType(headers) {
  const value = headers.get("content-type");
  return value ? value.split(";", 1)[0].trim().toLowerCase() : null;
}

function isJsonMediaType(mediaType) {
  return (
    mediaType === "application/json" || Boolean(mediaType?.endsWith("+json"))
  );
}

function isTextMediaType(mediaType) {
  return Boolean(
    mediaType &&
    (mediaType.startsWith("text/") ||
      mediaType === "application/xml" ||
      mediaType === "text/xml" ||
      mediaType.endsWith("+xml") ||
      mediaType === "application/x-www-form-urlencoded"),
  );
}

// Use strict equality: in browsers, `response.body` is `null` when the
// response genuinely has no content.  In React Native, `response.body` is
// always `undefined` because the ReadableStream API is not implemented —
// even when the response carries a full payload readable via `.text()` or
// `.json()`.  Loose equality (`== null`) matches both `null` and `undefined`,
// which causes every React Native response to be treated as empty.
function hasNoBody(response, method) {
  if (method === "HEAD") return true;
  if (NO_BODY_STATUS.has(response.status)) return true;
  if (response.headers.get("content-length") === "0") return true;
  if (response.body === null) return true;
  return false;
}

function stripBom(text) {
  return text.charCodeAt(0) === 0xfeff ? text.slice(1) : text;
}

function looksLikeJson(text) {
  const trimmed = text.trimStart();
  return trimmed.startsWith("{") || trimmed.startsWith("[");
}

function getStringField(value, key) {
  if (!value || typeof value !== "object") return undefined;

  const candidate = value[key];
  if (typeof candidate !== "string") return undefined;

  const trimmed = candidate.trim();
  return trimmed === "" ? undefined : trimmed;
}

function truncate(text, maxLength = 300) {
  return text.length > maxLength ? `${text.slice(0, maxLength - 1)}…` : text;
}

function buildErrorMessage(response, data) {
  const prefix = `HTTP ${response.status} ${response.statusText}`;

  if (typeof data === "string") {
    const text = data.trim();
    return text ? `${prefix}: ${truncate(text)}` : prefix;
  }

  const title = getStringField(data, "title");
  const detail = getStringField(data, "detail");
  const message =
    getStringField(data, "message") ??
    getStringField(data, "error_description") ??
    getStringField(data, "error");

  if (title && detail) return `${prefix}: ${title} — ${detail}`;
  if (detail) return `${prefix}: ${detail}`;
  if (message) return `${prefix}: ${message}`;
  if (title) return `${prefix}: ${title}`;

  return prefix;
}

export class ApiError extends Error {
  name = "ApiError";

  constructor(response, data, requestInfo) {
    super(buildErrorMessage(response, data));
    Object.setPrototypeOf(this, new.target.prototype);

    this.status = response.status;
    this.statusText = response.statusText;
    this.data = data;
    this.headers = response.headers;
    this.response = response;
    this.method = requestInfo.method;
    this.url = response.url || requestInfo.url;
  }
}

export class ResponseParseError extends Error {
  name = "ResponseParseError";

  constructor(response, rawBody, cause, requestInfo) {
    super(
      `Failed to parse response from ${requestInfo.method} ${response.url || requestInfo.url} ` +
        `(${response.status} ${response.statusText}) as JSON`,
    );
    Object.setPrototypeOf(this, new.target.prototype);

    this.status = response.status;
    this.statusText = response.statusText;
    this.headers = response.headers;
    this.response = response;
    this.method = requestInfo.method;
    this.url = response.url || requestInfo.url;
    this.rawBody = rawBody;
    this.cause = cause;
  }
}

async function parseJsonBody(response, requestInfo) {
  const raw = await response.text();
  const normalized = stripBom(raw);

  if (normalized.trim() === "") {
    return null;
  }

  try {
    return JSON.parse(normalized);
  } catch (cause) {
    throw new ResponseParseError(response, raw, cause, requestInfo);
  }
}

async function parseErrorBody(response, method) {
  if (hasNoBody(response, method)) {
    return null;
  }

  const mediaType = getMediaType(response.headers);

  // Fall back to text when blob() is unavailable (e.g. some React Native builds).
  if (mediaType && !isJsonMediaType(mediaType) && !isTextMediaType(mediaType)) {
    return typeof response.blob === "function"
      ? response.blob()
      : response.text();
  }

  const raw = await response.text();
  const normalized = stripBom(raw);
  const trimmed = normalized.trim();

  if (trimmed === "") {
    return null;
  }

  if (isJsonMediaType(mediaType) || looksLikeJson(normalized)) {
    try {
      return JSON.parse(normalized);
    } catch {
      return raw;
    }
  }

  return raw;
}

function inferResponseType(response) {
  const mediaType = getMediaType(response.headers);

  if (isJsonMediaType(mediaType)) return "json";
  if (isTextMediaType(mediaType) || mediaType == null) return "text";
  return "blob";
}

async function parseSuccessBody(response, responseType, requestInfo) {
  if (hasNoBody(response, requestInfo.method)) {
    return null;
  }

  const effectiveType =
    responseType === "auto" ? inferResponseType(response) : responseType;

  switch (effectiveType) {
    case "json":
      return parseJsonBody(response, requestInfo);

    case "text": {
      const text = await response.text();
      return text === "" ? null : text;
    }

    case "blob":
      if (typeof response.blob !== "function") {
        throw new TypeError(
          "Blob responses are not supported in this runtime. " +
            'Use responseType "json" or "text" instead.',
        );
      }
      return response.blob();
  }
}

import { handleMockRequest } from "./mock-data";

export async function customFetch(input, options = {}) {
  const method = resolveMethod(input, options.method);
  const requestInfo = { method, url: resolveUrl(input) };

  const useMockExplicitly = typeof import.meta !== "undefined" && import.meta.env?.VITE_USE_MOCK === "true";

  if (useMockExplicitly) {
    const mockResult = handleMockRequest(method, requestInfo.url);
    if (mockResult.handled) {
      await new Promise((resolve) => setTimeout(resolve, 300));
      return mockResult.data;
    }
  }

  input = applyBaseUrl(input);
  const { responseType = "auto", headers: headersInit, ...init } = options;

  if (init.body != null && (method === "GET" || method === "HEAD")) {
    throw new TypeError(`customFetch: ${method} requests cannot have a body.`);
  }

  const headers = mergeHeaders(
    isRequest(input) ? input.headers : undefined,
    headersInit,
  );

  let activeRole = "student";
  let activeUsername = "";
  let authToken = "";

  if (typeof window !== "undefined") {
    try {
      const storedUser = JSON.parse(localStorage.getItem("campusUser") || "{}");
      if (storedUser?.role) activeRole = storedUser.role;
      if (storedUser?.username) activeUsername = storedUser.username;
    } catch (_) {}
    if (!activeRole) activeRole = localStorage.getItem("mockRole") || "student";
    if (!activeUsername) activeUsername = localStorage.getItem("campusUsername") || "";
    authToken = localStorage.getItem("campusToken") || "";
  }

  if (!headers.has("X-User-Role") && !headers.has("x-user-role")) {
    headers.set("X-User-Role", activeRole);
  }
  if (!headers.has("X-Username") && !headers.has("x-username") && activeUsername) {
    headers.set("X-Username", activeUsername);
  }
  if (!headers.has("authorization") && authToken) {
    headers.set("authorization", `Bearer ${authToken}`);
  }

  if (
    typeof init.body === "string" &&
    !headers.has("content-type") &&
    looksLikeJson(init.body)
  ) {
    headers.set("content-type", "application/json");
  }

  if (responseType === "json" && !headers.has("accept")) {
    headers.set("accept", DEFAULT_JSON_ACCEPT);
  }

  // Attach bearer token when an auth getter is configured and no
  // Authorization header has been explicitly provided.
  if (_authTokenGetter && !headers.has("authorization")) {
    const token = await _authTokenGetter();
    if (token) {
      headers.set("authorization", `Bearer ${token}`);
    }
  }

  let response;
  try {
    response = await fetch(input, { ...init, method, headers });
  } catch (networkError) {
    console.warn(`[API] Failed connecting to backend at ${requestInfo.url}:`, networkError.message);
    const mockResult = handleMockRequest(method, requestInfo.url);
    if (mockResult.handled) {
      console.warn(`[API] Using local fallback for ${requestInfo.url}`);
      return mockResult.data;
    }
    throw networkError;
  }

  if (!response.ok) {
    const errorData = await parseErrorBody(response, method);
    throw new ApiError(response, errorData, requestInfo);
  }

  return await parseSuccessBody(response, responseType, requestInfo);
}


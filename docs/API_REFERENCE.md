### LeafShot API Reference

All endpoints are relative to the base URL of the LeafShot server (default port: `8091`).

---

### Endpoints

#### 1. Upload Image
Uploads an image to the server.

*   **URL:** `/api/v1/image`
*   **Method:** `POST`
*   **Request Header:**
    *   `X-Forwarded-For` (optional): Used to identify the client IP for rate limiting when behind a proxy.
*   **Parameters:**
    *   `image`: The image file data (Multipart form-data).
*   **Success Response:**
    *   **Code:** 200 OK
    *   **Content:**
        ```json
        {
          "id": "A1b2C3d4",
          "message": "Upload successful"
        }
        ```
*   **Error Responses:**
    *   **Code:** 400 Bad Request
    *   **Content:** `{"error": "No image data"}`, `{"error": "Only images are allowed"}`, `{"error": "Invalid image format"}`
    *   **Code:** 429 Too Many Requests
    *   **Content:** `{"error": "Rate limit exceeded"}`
    *   **Code:** 500 Internal Server Error
    *   **Content:** `{"error": "Internal error"}`

---

#### 2. Get Image
Retrieves the image associated with the given ID. Accessing this endpoint increments the view count and may prolong the resource's lifetime if configured.

*   **URL:** `/api/v1/image`
*   **Method:** `GET`
*   **Request Header:**
    *   `X-Forwarded-For` (optional): Used to identify the client IP for rate limiting.
*   **Parameters:**
    *   `id`: The unique identifier of the image (e.g., `?id=A1b2C3d4`).
*   **Success Response:**
    *   **Code:** 200 OK
    *   **Content:** Binary image data (MIME Type: `image/png`).
*   **Error Responses:**
    *   **Code:** 400 Bad Request
    *   **Content:** No content (Invalid ID format)
    *   **Code:** 404 Not Found
    *   **Content:** No content (Image file missing)
    *   **Code:** 410 Gone
    *   **Content:** No content (Resource expired or deleted)
    *   **Code:** 429 Too Many Requests
    *   **Content:** No content
    *   **Code:** 500 Internal Server Error
    *   **Content:** No content

---

#### 3. Get Manifest
Retrieves the metadata (manifest) for the given resource ID.

*   **URL:** `/api/v1/manifest`
*   **Method:** `GET`
*   **Request Header:**
    *   `X-Forwarded-For` (optional): Used to identify the client IP for rate limiting.
*   **Parameters:**
    *   `id`: The unique identifier of the resource (e.g., `?id=A1b2C3d4`).
*   **Success Response:**
    *   **Code:** 200 OK
    *   **Content:**
        ```json
        {
          "id": "A1b2C3d4",
          "reports": 0,
          "views": 10,
          "earmarkedForRemoval": false,
          "deleted": false,
          "timestampOfCreation": 1672531200000,
          "expirationTimestamp": 1673136000000,
          "fileName": "A1b2C3d4.png",
          "mimeType": "image/png",
          "fileSize": 102400,
          "width": 1920,
          "height": 1080,
          "message": "resource found!"
        }
        ```
*   **Error Responses:**
    *   **Code:** 400 Bad Request
    *   **Content:** `{"error": "Invalid ID"}`
    *   **Code:** 404 Not Found
    *   **Content:** `{"message": "Resource not found"}`
    *   **Code:** 429 Too Many Requests
    *   **Content:** `{"error": "Rate limit exceeded"}`

---

#### 4. Report Resource
Reports a resource. Reporting increments the report count and reduces the resource's lifetime. If the report threshold is reached, the resource is earmarked for removal.

*   **URL:** `/api/v1/report`
*   **Method:** `PATCH`
*   **Request Header:**
    *   `X-Forwarded-For` (optional): Used to identify the client IP for rate limiting.
*   **Parameters:**
    *   `id`: The unique identifier of the resource (e.g., `?id=A1b2C3d4`).
*   **Success Response:**
    *   **Code:** 200 OK
    *   **Content:** `{"message": "Resource reported"}`
*   **Error Responses:**
    *   **Code:** 400 Bad Request
    *   **Content:** `{"error": "Invalid ID"}`
    *   **Code:** 404 Not Found
    *   **Content:** No content
    *   **Code:** 429 Too Many Requests
    *   **Content:** `{"error": "Rate limit exceeded"}`
    *   **Code:** 500 Internal Server Error
    *   **Content:** No content

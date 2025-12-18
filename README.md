<p align="center">
  <img src="src/main/resources/LeafShot.png" alt="LeafShot Preview" width="160">
</p>
<h3 align="center">LeafShot Web</h3>

<p align="center">
    <strong>Capture, Annotate, Copy, and Share.</strong><br>
    A lightweight, image hosting server built with Spring Boot. LeafShot provides a simple API for uploading, retrieving, and reporting images with automatic expiration and cleanup features.
It's the back-bone of the LeafShotSwingUI client in terms of hosting and sharing images.
</p>

## Features

- **Fast Image Uploads**: Supports multipart, raw binary, and Base64 encoded uploads.
- **Automatic Expiration**: Resources are automatically purged after a configurable lifetime.
- **Lifetime Management**:
    - **Prolongation**: Resources can have their lifetime extended each time they are accessed (optional).
    - **Reduction**: Reporting a resource reduces its remaining lifetime.
- **Automatic Cleanup**: A background task runs hourly to delete expired or earmarked resources.
- **Metadata Management**: Each resource has a `manifest.json` containing metadata like dimensions, file size, and view counts.

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/ozanaaslan/leafshot-webmin.git
   cd leafshot-webmin
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the server:
   ```bash
   mvn spring-boot:run
   ```

The server will start on port `8091` by default.

## Configuration

Configuration is managed via `src/main/resources/application.properties`. Key settings include:
- `leafshot.resource.lifetime-hours`: Default lifetime for new uploads (default: 168 hours / 1 week).
- `leafshot.resource.prolongable`: Whether access extends resource life (default: true).
- `leafshot.reports.deletion-threshold`: Number of reports required to earmark a resource for removal.

## API Usage

LeafShot provides a RESTful API for all operations. See the [API Reference](API_REFERENCE.md) for detailed documentation.

### Example: Uploading an Image
```bash
curl -X POST http://localhost:8091/api/v1/image \
     -H "Content-Type: text/plain" \
     --data-binary "@image_base64.txt"
```

### Example: Retrieving an Image
```bash
# Simply open in your browser or use:
curl http://localhost:8091/api/v1/image?id=A1b2C3d4
```

## Client Library

The project includes a Java `UploadHandler` client for easy integration into other applications.

```java
UploadHandler client = new UploadHandler("http://localhost:8091");
String link = client.uploadImage(bufferedImage);
System.out.println("Image uploaded to: " + link);
```


<p align="center">
  <img src="docs/assets/LeafShot.png" alt="LeafShot Preview" width="160">
</p>
<h3 align="center">LeafShot Web</h3>

<p align="center">
    A lightweight image hosting server built with Spring Boot.
</p>

## Overview

LeafShot Web is a server-side application designed for temporary image hosting. It provides a REST API for uploading and retrieving images, with features for automatic lifecycle management and rate limiting.

## Features

- **Image Upload**: Supports `multipart/form-data` uploads.
- **Image Retrieval**: Serves images in PNG format.
- **Metadata Access**: Provides image metadata via a JSON manifest endpoint.
- **Automatic Expiration**: Resources are automatically marked as expired after a configurable duration.
- **Dynamic Lifetime**:
    - **Prolongation**: Resource lifetime can be extended upon each access.
    - **Reduction**: Reporting a resource decreases its remaining lifetime.
- **Automated Cleanup**: A scheduled task periodically removes expired or earmarked resources from the file system and database.
- **Rate Limiting**: Integrated request throttling based on client IP addresses.
- **Database Integration**: Uses H2 database to persist resource metadata (manifests).

## Prerequisites

- Java 11
- Maven 3.x

## Installation and Execution

### Manual Execution

1. **Clone the repository**:
   ```bash
   git clone https://github.com/ozanaaslan/leafshot-web-backend.git
   cd leafshot-web-backend
   ```

2. **Build the application**:
   ```bash
   mvn clean install
   ```

3. **Run the server**:
   ```bash
   mvn spring-boot:run
   ```

The server listens on port `8091` by default.

### Docker Execution

1. **Setup environment variables**:
   ```bash
   cp .env.example .env
   ```
   Edit `.env` to configure your server settings.

2. **Build and start the container**:
   ```bash
   docker-compose up -d --build
   ```

The application will be available at the port specified in your `.env` file (default `8091`). Data and images are persisted in the `./data` and `./workdir` directories on the host.

## Configuration

Configuration is managed via `src/main/resources/application.properties`.

### Core Settings
| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | The port the server listens on. | `8091` |
| `leafshot.working-directory` | Directory where images and data are stored. | `workdir` |
| `leafshot.upload.max-size-mb` | Maximum allowed upload size in MB. | `100` |

### Resource Management
| Property | Description | Default |
|----------|-------------|---------|
| `leafshot.resource.lifetime-hours` | Initial lifetime of a resource in hours. | `168` |
| `leafshot.resource.prolongable` | Whether accessing a resource extends its lifetime. | `true` |
| `leafshot.resource.prolonged-hours-per-access` | Hours added per access if prolongable. | `24` |

### Reporting and Removal
| Property | Description | Default |
|----------|-------------|---------|
| `leafshot.reports.deletion-threshold` | Number of reports required for earmarking removal. | `5` |
| `leafshot.reports.time-withdraw-hours` | Hours deducted from lifetime per report. | `24` |

### Rate Limiting
| Property | Description | Default |
|----------|-------------|---------|
| `leafshot.rate-limit.enabled` | Whether rate limiting is active. | `true` |
| `leafshot.rate-limit.requests-per-minute` | Maximum requests allowed per minute per IP. | `10` |

## API Usage

Refer to [docs/API_REFERENCE.md](docs/API_REFERENCE.md) for detailed endpoint documentation.

### Basic Examples

**Upload an Image (Multipart)**
```bash
curl -X POST http://localhost:8091/api/v1/image \
     -F "image=@path/to/your/image.png"
```

**Retrieve an Image**
```bash
curl http://localhost:8091/api/v1/image?id=RESOURCE_ID
```

## Client Integration

A Java `UploadHandler` is available in `com.github.ozanaaslan.leafshotweb.client` for simplified server interaction.

```java
UploadHandler client = new UploadHandler("http://localhost:8091");
String imageUrl = client.uploadImage(bufferedImage);
```


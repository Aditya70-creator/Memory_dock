# Media Metadata Extractor Module

A robust, independent Java utility designed to parse binary headers of media assets (Images and Videos) to extract hardware specifications, compression profiles, and embedded spatial parameters without loading heavy media streams into memory.

---

## Architecture & Function Breakdown

The core logic resides inside `MetadataExtract.java` and is split into distinct working chambers.

### 1. `extractMediaProperties(String filePath)`

**Working:**
This is the main entry point (API) of the module. It takes the absolute file path of any media asset, identifies whether it's an image or video, extracts its binary tag blocks, handles fallback routing, and triggers the geolocation display layer.

**Pipeline Flow:**

* **Phase A (Images):**

  * Targets `GpsDirectory.class` to extract EXIF location tags.

* **Phase B (Videos):**

  * If no image tags are found, it falls back to `Mp4Directory.class` to extract native MP4 location atom boxes (`TAG_LATITUDE` and `TAG_LONGITUDE`).

* **Phase C (Output):**

  * Consolidated data is formatted and forwarded to the reverse geocoding engine.

---

### 2. `fetchLocationName(double lat, double lon)`

**Working:**
This is a private network interaction layer. It converts raw coordinates into standard decimal strings, sets up a secure HTTP connection with a custom `User-Agent` to communicate with the OpenStreetMap API, parses the incoming JSON, and extracts the human-readable address.

---

### How to Call This Module From Another Project/File

Since the module has been fully refactored into a modular structure, you do **not** need to run it via its own `main()` method anymore. You can call it from any other controller or service inside your main project (e.g., `Memory_Dock`).

## Step 1: Add the Core Dependency

Add the following dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.drewnoakes</groupId>
    <artifactId>metadata-extractor</artifactId>
    <version>2.19.0</version>
</dependency>
```

---

## Step 2: Invoke the Module

Call the static method by passing the dynamic file path.

```java
MetadataExtract.extractMediaProperties(
    "C:\\Users\\Desktop\\Your_Uploaded_File.mp4"
);
```

---

## Example Usage

```java
public class UploadController {

    public void handleFileUpload(String savedFilePath) {

        System.out.println("File saved successfully. Triggering metadata extraction...");

        MetadataExtract.extractMediaProperties(savedFilePath);

    }
}
```

---

### Local Testing Setup

1. Place any test asset (`.jpg` or `.mp4`) on your local machine.

2. Pass the file path into the `main()` method inside `MetadataExtract.java`.

3. Execute the project using the Maven toolchain:

```bash
mvn compile exec:java -Dexec.mainClass="MetadataExtract"
```

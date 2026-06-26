import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation; 
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory; 

public class MetadataExtract {

    public static void extractMediaProperties(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("[-] Error: File not found at target location -> " + file.getAbsolutePath());
            return;
        }

        try {
            System.out.println("[+] Processing Target Resource: " + file.getName() + "...\n");
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            // Print normal segments and properties
            for (Directory directory : metadata.getDirectories()) {
                System.out.println("--- Segment: " + directory.getName() + " ---");
                for (Tag tag : directory.getTags()) {
                    System.out.println(tag.getTagName() + " : " + tag.getDescription());
                }
                System.out.println();
            }

            System.out.println("==================================================");
            System.out.println("            GEOLOCATION INFRASTRUCTURE            ");
            System.out.println("==================================================");
            
            double latitude = 0.0;
            double longitude = 0.0;
            boolean hasLocation = false;

            // Pipeline 1: Image Core Evaluation (EXIF Standard)
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                if (geoLocation != null && !geoLocation.isZero()) {
                    latitude = geoLocation.getLatitude();
                    longitude = geoLocation.getLongitude();
                    hasLocation = true;
                }
            }

            // Pipeline 2: Video Core Fallback (MP4 Atom Mapping)
            if (!hasLocation) {
                Mp4Directory mp4Directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
                if (mp4Directory != null) {
                    Double latObj = mp4Directory.getDoubleObject(Mp4Directory.TAG_LATITUDE);
                    Double lonObj = mp4Directory.getDoubleObject(Mp4Directory.TAG_LONGITUDE);
                    
                    if (latObj != null && lonObj != null && (latObj != 0.0 || lonObj != 0.0)) {
                        latitude = latObj;
                        longitude = lonObj;
                        hasLocation = true;
                    }
                }
            }

            // Unified Processing Checkpoint
            if (hasLocation) {
                System.out.println("Parsed Latitude  : " + latitude);
                System.out.println("Parsed Longitude : " + longitude);
                
                System.out.println("\nQuerying open-source database coordinates...");
                String resolvedLocation = fetchLocationName(latitude, longitude);
                System.out.println("📍 RESOLVED LOCATION : " + resolvedLocation);
                
                System.out.println("\n🔗 GOOGLE MAPS NAVIGATION UTILITY:");
                System.out.println("https://www.google.com/maps?q=27.929870099722223,88.7331329" + latitude + "," + longitude);
            } else {
                System.out.println("Location Status: No tracking vectors found or extracted parameters return zero.");
            }
            
            System.out.println("==================================================\n");

        } catch (ImageProcessingException | IOException e) {
            System.err.println("Execution Error Pipeline: " + e.getMessage());
        }
    }

    // 2. NETWORK INTERACTION FOR REVERSE GEOCODING
    private static String fetchLocationName(double lat, double lon) {
        try {
            String formattedLat = String.format(java.util.Locale.US, "%.4f", lat);
            String formattedLon = String.format(java.util.Locale.US, "%.4f", lon);
            
            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" 
                            + formattedLat + "&lon=" + formattedLon + "&accept-language=en";
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Java-Metadata-Extractor-Agent");

            if (conn.getResponseCode() == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }

                String json = response.toString();
                if (json.contains("\"display_name\":\"")) {
                    int start = json.indexOf("\"display_name\":\"") + 16;
                    int end = json.indexOf("\"", start);
                    return json.substring(start, end);
                }
            }
        } catch (IOException | RuntimeException e) {
            return "Network Fault: Unable to resolve string address configurations.";
        }
        return "Target parameters dropped from active geographical database maps.";
    }

    // 3. MAIN BLOCK - MINIMAL UTILITY FOR LOCAL REPO TESTING ONLY
    public static void main(String[] args) {
        // Direct absolute execution path setup
        extractMediaProperties("SAMPLE MEDIA");
    }
}
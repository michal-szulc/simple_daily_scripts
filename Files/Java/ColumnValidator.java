import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

public class ColumnValidator {

    public static void main(String[] args) {
        // Check if minimum arguments are provided
        if (args.length < 2) {
            System.out.println("Usage: java -jar ColumnValidator.jar <file_path> <separator_ascii> [charset] [remove_bad_rows]");
            return;
        }

        String filePath = args[0];
        char separator = (char) Integer.parseInt(args[1]);
        String charsetName = (args.length >= 3 && !args[2].isEmpty()) ? args[2] : "ISO-8859-2"; // Default to ISO-8859-2
        boolean removeBadRows = (args.length >= 4 && args[3].equalsIgnoreCase("remove_bad_rows"));

        String logFilePath = filePath.replace(".txt", "_validation_log.txt");
        String badRowsFilePath = filePath.replace(".txt", "_bad_rows.txt");
        String cleanedFilePath = filePath.replace(".txt", "_cleaned.txt");

        try {
            Charset charset = Charset.forName(charsetName);
            List<String> lines = Files.readAllLines(Paths.get(filePath), charset);

            if (lines.isEmpty()) {
                System.out.println("The file is empty.");
                return;
            }

            // Read header and determine column count
            String[] headers = lines.get(0).split(Character.toString(separator));
            int expectedColumns = headers.length;
            System.out.println("Expected column count: " + expectedColumns);

            List<String> badRows = new ArrayList<>();
            List<String> goodRows = new ArrayList<>();
            goodRows.add(lines.get(0)); // Always add header
            badRows.add(lines.get(0));  // Add header to bad rows file

            try (
                BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFilePath), charset));
                BufferedWriter badRowsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(badRowsFilePath), charset))
            ) {
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String[] values = line.split(Character.toString(separator), -1); // Preserve empty values

                    if (values.length != expectedColumns) {
                        StringBuilder logEntry = new StringBuilder("Line " + (i + 1) + ": ");
                        for (int j = 0; j < values.length; j++) {
                            String headerName = (j < headers.length) ? headers[j] : "UNKNOWN";
                            logEntry.append("\"").append(headerName).append("\"=>").append(values[j]).append("<<; ");
                        }

                        logWriter.write(logEntry.toString().trim() + "\n");
                        badRowsWriter.write(line + "\n");
                        badRows.add(line);
                    } else {
                        goodRows.add(line);
                    }
                }
            }

            System.out.println("Validation complete. Check '" + logFilePath + "' for details.");
            System.out.println("Bad rows saved in '" + badRowsFilePath + "'.");

            // Remove bad rows if requested
            if (removeBadRows) {
                Files.write(Paths.get(cleanedFilePath), goodRows, charset);
                System.out.println("Cleaned file without bad rows saved as '" + cleanedFilePath + "'.");
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid separator ASCII code. Must be a number.");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid charset: " + charsetName);
        }
    }
}

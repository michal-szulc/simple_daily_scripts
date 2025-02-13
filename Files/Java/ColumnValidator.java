import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

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

            try (
                BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), charset);
                BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFilePath), charset));
                BufferedWriter badRowsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(badRowsFilePath), charset));
                BufferedWriter cleanedWriter = removeBadRows
                        ? new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cleanedFilePath), charset))
                        : null
            ) {
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    System.out.println("The file is empty.");
                    return;
                }

                // Write header to bad rows file and cleaned file if needed
                badRowsWriter.write(headerLine);
                badRowsWriter.newLine();
                if (removeBadRows) {
                    cleanedWriter.write(headerLine);
                    cleanedWriter.newLine();
                }

                String[] headers = headerLine.split(Character.toString(separator));
                int expectedColumns = headers.length;
                System.out.println("Expected column count: " + expectedColumns);

                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    String[] values = line.split(Character.toString(separator), -1); // Preserve empty values

                    if (values.length != expectedColumns) {
                        // Log and save bad rows
                        StringBuilder logEntry = new StringBuilder("Line " + lineNumber + ": ");
                        for (int j = 0; j < values.length; j++) {
                            String headerName = (j < headers.length) ? headers[j] : "UNKNOWN";
                            logEntry.append("\"").append(headerName).append("\"=>").append(values[j]).append("<<; ");
                        }
                        logWriter.write(logEntry.toString().trim() + "\n");
                        badRowsWriter.write(line);
                        badRowsWriter.newLine();
                    } else {
                        // Save valid row if remove_bad_rows is active
                        if (removeBadRows) {
                            cleanedWriter.write(line);
                            cleanedWriter.newLine();
                        }
                    }
                }

                System.out.println("Validation complete. Check '" + logFilePath + "' for details.");
                System.out.println("Bad rows saved in '" + badRowsFilePath + "'.");
                if (removeBadRows) {
                    System.out.println("Cleaned file without bad rows saved as '" + cleanedFilePath + "'.");
                }

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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ColumnValidator {

    public static void main(String[] args) {
        // Check if correct arguments are provided
        if (args.length < 3) {
            System.out.println("Usage: java -jar ColumnValidator.jar <file_path> <separator_ascii> <charset>");
            return;
        }

        String filePath = args[0];
        char separator = (char) Integer.parseInt(args[1]);
        String charsetName = args[2]; // Charset as argument
        String logFilePath = "validation_log.txt";

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

            try (BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFilePath), charset))) {
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
                    }
                }
            }

            System.out.println("Validation complete. Check '" + logFilePath + "' for details.");

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid separator ASCII code. Must be a number.");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid charset: " + args[2]);
        }
    }
}

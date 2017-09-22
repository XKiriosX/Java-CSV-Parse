import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        String csvFile = "tq.csv";
        try (InputStream in = new FileInputStream(csvFile)) {
            CSV csv = new CSV(true, ';', in);
            List<String> columnNames = null;
            if (csv.hasNext()) {
                columnNames = new ArrayList<String>(csv.next());
            }
            while (csv.hasNext()) {
                List<String> fields = new ArrayList<String>(csv.next());
                rows.add(fields);
            }
        }
        catch (IOException e) {
            e.getMessage();
        }

    }
}


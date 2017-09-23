import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final String DELIM = ";";

    public static void main(String[] args) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader("src/book.csv"));
            List<Company> companyList = new ArrayList<>();
            String line = "";
            in.readLine();
            while ((line = in.readLine()) != null) {
                String[] companyDetails = line.split(DELIM);

                if(companyDetails.length > 0) {
                    Company company = new Company(
                            companyDetails[0],
                            companyDetails[1],
                            companyDetails[2],
                            companyDetails[3],
                            companyDetails[4],
                            Integer.parseInt(companyDetails[5]),
                            companyDetails[6],
                            companyDetails[7],
                            companyDetails[8],
                            companyDetails[9],
                            companyDetails[10],
                            companyDetails[11]
                            );
                    companyList.add(company);
                }
                for (Company e : companyList) {
                    System.out.println(e.getName() + " " + e.getShortTitle() + " " + e.getDateUpdate() + " " + e.getAddress() + " " + e.getDateFoundation()
                    + " " + e.getCountEmployees() + " " + e.getAuditor() + " " + e.getPhone() + " " + e.getEmail() + " " + e.getBranch() + " " + e.getActivity()
                    + " " + e.getLink());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}


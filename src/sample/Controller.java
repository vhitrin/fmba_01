package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Controller {

    public static final int TYPE_STRING = 0;
    public static final int TYPE_NUM = 1;
    public static final int TYPE_DATE = 2;

    public static final String EXCEL = "Test.xlsx";
    public static final String DATABASE = "db.sqlite";

    public TableView<DataInputString> dataInputTable;
    public TableColumn<DataInputString, String> ageColumn;
    public TableColumn<DataInputString, String> typeCitizensColumn;
    public TableColumn<DataInputString, Float> populationColumn;
    public TableColumn<DataInputString, Float> numberOfDeathsColumn;
    public TableColumn<DataInputString, Float> deathRateColumn;

    private ObservableList<DataInputString> dataInputStringObservableList = FXCollections.observableArrayList();

    public void ImportFromExcelOnAction() {
        try {
            readFromExcel(EXCEL, DATABASE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void readFromExcel(String file, String dbName) throws Exception {

        typeCitizensColumn.setVisible(true);
        ageColumn.setVisible(true);

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM 'OriginalData';");
        System.out.println("Deleting table complete!");

        Map<Integer, String> rowNames = new HashMap<>();
        rowNames.put(0, "Возраст");
        rowNames.put(1, "Тип жителя");
        rowNames.put(2, "Численность населения");
        rowNames.put(3, "Количество смертей");

        Map<Integer, Integer> rowTypes = new HashMap<>();
        rowTypes.put(0, TYPE_STRING);
        rowTypes.put(1, TYPE_STRING);
        rowTypes.put(2, TYPE_NUM);
        rowTypes.put(3, TYPE_NUM);

        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);

        int rowCount = myExcelSheet.getLastRowNum();
        int colCount = myExcelSheet.getRow(0).getLastCellNum();

        dataInputStringObservableList.clear();

        for (int i = 1; i <= rowCount; i++) {

            XSSFRow row = myExcelSheet.getRow(i);

            String query = "INSERT INTO 'OriginalData' ('Age', 'TypeCitizens', 'Population', 'NumberOfDeaths') VALUES (";

            String age = row.getCell(0).getStringCellValue();
            String typeCitizens = row.getCell(1).getStringCellValue();
            float population = (float)row.getCell(2).getNumericCellValue();
            float numberOfDeaths = (float)row.getCell(3).getNumericCellValue();

            for (int j = 0; j < colCount; j++) {
                if (rowTypes.get(j) == TYPE_STRING) {
                    query += "'" + row.getCell(j).getStringCellValue() + "'";
                } else if (rowTypes.get(j) == TYPE_NUM) {
                    query += row.getCell(j).getNumericCellValue();
                } else if (rowTypes.get(j) == TYPE_DATE) {
                    System.out.println(row.getCell(j).getDateCellValue());
                }

                if (j != colCount - 1) {
                    query += ", ";
                } else {
                    query += ");";
                }
            }

            statement.execute(query);
            dataInputStringObservableList.add(new DataInputString(age, typeCitizens, population, numberOfDeaths));
        }

        System.out.println("All data will be added!");

        conn.close();
        myExcelBook.close();
    }

    public void CalculateOnAction() throws Exception {

        typeCitizensColumn.setVisible(true);
        ageColumn.setVisible(true);

        Iterator<DataInputString> it = dataInputStringObservableList.iterator();
        while (it.hasNext()) {
            if (it.next().getAge().equals("ИТОГО")) {
                it.remove();
            }
        }

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT 'ИТОГО' AS Age, TypeCitizens, SUM(Population) AS Population, SUM(NumberOfDeaths) AS NumberOfDeaths FROM 'OriginalData' GROUP BY TypeCitizens");
        System.out.println("Reading data complete!");

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("age"),
                    rs.getString("typeCitizens"),
                    rs.getInt("population"),
                    rs.getInt("numberOfDeaths")));
        }
    }

    public void CalculateAgeOnAction() throws Exception {

        dataInputStringObservableList.clear();
        typeCitizensColumn.setVisible(false);
        ageColumn.setVisible(true);

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
        Statement statement = conn.createStatement();
        ResultSet rs;
        rs = statement.executeQuery("SELECT Age, SUM(Population) AS Population, SUM(NumberOfDeaths) AS NumberOfDeaths FROM 'OriginalData' GROUP BY Age");
        System.out.println("Reading data complete!");

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("age"),
                    "",
                    rs.getInt("population"),
                    rs.getInt("numberOfDeaths")));
        }

        rs = statement.executeQuery("SELECT SUM(Population) AS Population, SUM(NumberOfDeaths) AS NumberOfDeaths FROM 'OriginalData'");
        System.out.println("Reading data complete!");

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString("ИТОГО",
                    "",
                    rs.getInt("population"),
                    rs.getInt("numberOfDeaths")));
        }
    }

    public void ShowSummaryResultOnAction() throws Exception {

        dataInputStringObservableList.clear();
        typeCitizensColumn.setVisible(true);
        ageColumn.setVisible(false);

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("Select TypeCitizens, SUM(Population) AS Population, SUM(AgeDeathRate * Population / 100) AS numberOfDeaths, SUM(AgeDeathRate * Population / 100) / SUM(Population) AS DeathRate FROM 'OriginalData' AS OriginalDate " +
                "INNER  JOIN " +
                "(SELECT Age, (SUM(NumberOfDeaths) * 100 * 1000) / SUM(Population) AS AgeDeathRate FROM 'OriginalData' GROUP BY Age) AS AgeOriginalDate ON OriginalDate.Age = AgeOriginalDate.Age " +
                "GROUP BY TypeCitizens");
        System.out.println("Reading data complete!");

        dataInputStringObservableList.clear();

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString("",
                    rs.getString("typeCitizens"),
                    rs.getInt("population"),
                    rs.getInt("numberOfDeaths"),
                    rs.getFloat("DeathRate")));
        }

    }

    private void fillTable() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM 'OriginalData'");
        System.out.println("Reading data complete!");

        dataInputStringObservableList.clear();

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("age"),
                                                                    rs.getString("typeCitizens"),
                                                                    rs.getInt("population"),
                                                                    rs.getInt("numberOfDeaths")));
        }
    }

    public ObservableList<DataInputString> getDataInputStringObservableList() {
        return dataInputStringObservableList;
    }

    @FXML
    private void initialize() {
        ageColumn.setCellValueFactory(cellData -> cellData.getValue().ageProperty());
        typeCitizensColumn.setCellValueFactory(cellData -> cellData.getValue().typeCitizensProperty());
        populationColumn.setCellValueFactory(cellData -> cellData.getValue().populationProperty().asObject());
        numberOfDeathsColumn.setCellValueFactory(cellData -> cellData.getValue().numberOfDeathProperty().asObject());
        deathRateColumn.setCellValueFactory(cellData -> cellData.getValue().deathRateProperty().asObject());
        dataInputTable.setItems(getDataInputStringObservableList());

        try {
            fillTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

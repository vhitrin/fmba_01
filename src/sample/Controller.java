package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Controller {

    public static final int TYPE_STRING = 0;
    public static final int TYPE_NUM = 1;
    public static final int TYPE_DATE = 2;

    @FXML
    public static Stage STAGE;

    public TableView<DataInputString> dataInputTable;
    public TableColumn<DataInputString, String> D1Column;
    public TableColumn<DataInputString, String> P1Column;
    public TableColumn<DataInputString, Float> V1Column;
    public TableColumn<DataInputString, Float> V2Column;
    public TableColumn<DataInputString, Float> V3Column;

    private ObservableList<DataInputString> dataInputStringObservableList = FXCollections.observableArrayList();

    private Map<String, String> fieldsName = new TreeMap<>();

    public void ImportFromExcelOnAction() {
        try {
            String excelFileName = hndlOpenFile(true);
            if (!excelFileName.isEmpty()) {
                readFromExcel(excelFileName);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void readFromExcel(String file) throws Exception {

        P1Column.setVisible(true);
        D1Column.setVisible(true);

        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();
        statement.execute("DELETE FROM 'OriginalData';");
        statement.execute("DELETE FROM 'FieldsName';");
        System.out.println("Deleting table complete!");

        Map<Integer, Integer> rowTypes = new HashMap<>();
        rowTypes.put(0, TYPE_STRING);
        rowTypes.put(1, TYPE_STRING);
        rowTypes.put(2, TYPE_NUM);
        rowTypes.put(3, TYPE_NUM);
        rowTypes.put(4, TYPE_NUM);

        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);

        int rowCount = myExcelSheet.getLastRowNum();
        int colCount = myExcelSheet.getRow(0).getLastCellNum();

        XSSFRow row0 = myExcelSheet.getRow(0);
        XSSFRow row1 = myExcelSheet.getRow(1);

        for (int i = 0; i < colCount; i++) {

            String nameField = row0.getCell(i).getStringCellValue();
            String displayField = row1.getCell(i).getStringCellValue();

            fieldsName.put(nameField, displayField);

            String query = "INSERT INTO 'FieldsName' ('nameField', 'displayField') VALUES ('" + nameField + "', '" + displayField + "');";
            statement.execute(query);
        }

        setColumnsName();

        dataInputStringObservableList.clear();

        for (int i = 2; i <= rowCount; i++) {

            XSSFRow row = myExcelSheet.getRow(i);

            String query = "INSERT INTO 'OriginalData' ('D1', 'P1', 'V1', 'V2', 'V3') VALUES (";

            String D1 = row.getCell(0).getStringCellValue();
            String P1 = row.getCell(1).getStringCellValue();
            float V1 = (float)row.getCell(2).getNumericCellValue();
            float V2 = (float)row.getCell(3).getNumericCellValue();

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
            dataInputStringObservableList.add(new DataInputString(D1, P1, V1, V2));
        }

        System.out.println("All data will be added!");

        conn.close();
        myExcelBook.close();
    }

    public void CalculateOnAction() throws Exception {

        P1Column.setVisible(true);
        D1Column.setVisible(true);

        Iterator<DataInputString> it = dataInputStringObservableList.iterator();
        while (it.hasNext()) {
            if (it.next().getD1().equals("ИТОГО")) {
                it.remove();
            }
        }

        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT 'ИТОГО' AS D1, P1, SUM(V1) AS V1, SUM(V2) AS V2 FROM 'OriginalData' GROUP BY P1");
        System.out.println("Reading data complete!");

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("D1"),
                    rs.getString("P1"),
                    rs.getInt("V1"),
                    rs.getInt("V2")));
        }
    }

    public void CalculateD1OnAction() throws Exception {

        dataInputStringObservableList.clear();
        P1Column.setVisible(false);
        D1Column.setVisible(true);

        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs;
        rs = statement.executeQuery("SELECT D1, SUM(V1) AS V1, SUM(V2) AS V2 FROM 'OriginalData' GROUP BY D1");
        System.out.println("Reading data complete!");

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("D1"),
                    "",
                    rs.getInt("V1"),
                    rs.getInt("V2")));
        }

        rs = statement.executeQuery("SELECT SUM(V1) AS V1, SUM(V2) AS V2 FROM 'OriginalData'");
        System.out.println("Reading data complete!");

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString("ИТОГО",
                    "",
                    rs.getInt("V1"),
                    rs.getInt("V2")));
        }
    }

    public void ShowSummaryResultOnAction() throws Exception {

        dataInputStringObservableList.clear();
        P1Column.setVisible(true);
        D1Column.setVisible(false);

        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("Select P1, SUM(V1) AS V1, SUM(D1V3 * V1 / 100) AS V2, SUM(D1V3 * V1 / 100) / SUM(V1) AS V3 FROM 'OriginalData' AS OriginalData " +
                "INNER  JOIN " +
                "(SELECT D1, (SUM(V2) * 100 * 1000) / SUM(V1) AS D1V3 FROM 'OriginalData' GROUP BY D1) AS D1OriginalData ON OriginalData.D1 = D1OriginalData.D1 " +
                "GROUP BY P1");
        System.out.println("Reading data complete!");

        dataInputStringObservableList.clear();

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString("",
                    rs.getString("P1"),
                    rs.getInt("V1"),
                    rs.getInt("V2"),
                    rs.getFloat("V3")));
        }

    }

    public void ExportTableToExcelOnAction() {

        String excelFileName = hndlOpenFile(false);
        if (!excelFileName.isEmpty()) {
            try {
                writeToExcel(excelFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeToExcel(String file) throws IOException {

        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet myExcelSheet = book.createSheet("Данные из программы");

        XSSFRow row = myExcelSheet.createRow(0);
        XSSFRow row1 = myExcelSheet.createRow(1);

        int elementNumber = 0;
        for (Map.Entry<String, String> entry : fieldsName.entrySet()) {
            row.createCell(elementNumber).setCellValue(entry.getKey());
            row1.createCell(elementNumber).setCellValue(entry.getValue());
            elementNumber++;
        }

        int rowNumber = 2;
        for(DataInputString dataInputString : dataInputStringObservableList) {
            row = myExcelSheet.createRow(rowNumber);
            row.createCell(0).setCellValue(dataInputString.getD1());
            row.createCell(1).setCellValue(dataInputString.getP1());
            row.createCell(2).setCellValue(dataInputString.getV1());
            row.createCell(3).setCellValue(dataInputString.getV2());
            row.createCell(4).setCellValue(dataInputString.getV3());
            rowNumber++;
        }

        book.write(new FileOutputStream(file));
        book.close();
    }

    public void ReadDataFromDBOnAction() {
        try {
            fillTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Начальное заполнение таблицы на основании сохраненных в базе данных
    private void fillTable() throws Exception {
        Connection conn = DBHelper.getConnection();
        Statement statement = conn.createStatement();

        ResultSet setFieldsName = statement.executeQuery("SELECT * FROM FieldsName");

        while (setFieldsName.next()) {
            fieldsName.put(setFieldsName.getString("nameField"), setFieldsName.getString("displayField"));
        }

        setColumnsName();

        ResultSet rs = statement.executeQuery("SELECT * FROM 'OriginalData'");
        System.out.println("Reading data complete!");

        dataInputStringObservableList.clear();

        while (rs.next()) {
            dataInputStringObservableList.add(new DataInputString(rs.getString("D1"),
                                                                    rs.getString("P1"),
                                                                    rs.getInt("V1"),
                                                                    rs.getInt("V2")));
        }
    }

    public ObservableList<DataInputString> getDataInputStringObservableList() {
        return dataInputStringObservableList;
    }

    @FXML
    private void initialize() {
        D1Column.setCellValueFactory(cellData -> cellData.getValue().D1Property());
        P1Column.setCellValueFactory(cellData -> cellData.getValue().P1Property());
        V1Column.setCellValueFactory(cellData -> cellData.getValue().V1Property().asObject());
        V2Column.setCellValueFactory(cellData -> cellData.getValue().V2Property().asObject());
        V3Column.setCellValueFactory(cellData -> cellData.getValue().V3Property().asObject());
        dataInputTable.setItems(getDataInputStringObservableList());

        try {
            fillTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Устанавливает заголовки столбцам таблицы согласно ранее сохраненным данным
    private void setColumnsName() {
        D1Column.setText(fieldsName.get("D1"));
        P1Column.setText(fieldsName.get("P1"));
        V1Column.setText(fieldsName.get("V1"));
        V2Column.setText(fieldsName.get("V2"));
        V3Column.setText(fieldsName.get("V3"));
    }

    // Открывает диалог выбора файла для указания источника данных для базы
    @FXML
    private String hndlOpenFile(boolean isOpenFileDialog) {

        FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
        fileChooser.setTitle(isOpenFileDialog ? "Открытие файла" : "Сохранение файла");//Заголовок диалога
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("MS Excel files (*.xlsx)", "*.xlsx");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);

        File file;

        if (isOpenFileDialog) {
            file = fileChooser.showOpenDialog(STAGE);
        } else {
            file = fileChooser.showSaveDialog(STAGE);
        }

        if (file != null) {
            return file.getPath();
        }

        return "";
    }
}

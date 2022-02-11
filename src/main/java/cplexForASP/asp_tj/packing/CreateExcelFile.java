package cplexForASP.asp_tj.packing;
/**
 *
 */

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author bpan
 *
 * created 2018年2月27日
 */
public class CreateExcelFile {

    private static HSSFWorkbook hWorkbook = null;
    private static XSSFWorkbook xWorkbook = null;

    /**
     * 判断文件是否存在.
     * @param fileDir  文件路径
     * @return
     */
    public static boolean fileExist(String fileDir) {
        boolean flag = false;
        File file = new File(fileDir);
        flag = file.exists();
        return flag;
    }

    /**
     * 判断文件的sheet是否存在.
     * @param fileDir   文件路径
     * @param sheetName  表格索引名
     * @return boolean
     */
    public static boolean XlsSheetExist(String fileDir, String sheetName) {

        boolean flag = false;
        File file = new File(fileDir);

        if (file.exists()) {
            //文件存在，创建workbook
            try {
                hWorkbook = new HSSFWorkbook(new FileInputStream(file));

                HSSFSheet sheet = hWorkbook.getSheet(sheetName);
                if (sheet != null) {
                    //文件存在，sheet存在

                    flag = true;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            //文件不存在
            flag = false;
        }
        return flag;
    }

    /**
     * 创建新excel(xls).
     * @param fileDir excel的路径
     * @param sheetNames 要创建的表格索引列表
     * @param titleList  excel的第一行即表格头
     */
    public static void createExcelXls(String fileDir, List<String> sheetNames,List<String[]> titleList) {

        //创建workbook
        hWorkbook = new HSSFWorkbook();
        //新建文件
        FileOutputStream fileOutputStream = null;
        HSSFRow row = null;
        try {

            CellStyle cellStyle = hWorkbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);

            //添加Worksheet（不添加sheet时生成的xls文件打开时会报错)
            for (int i = 0; i < sheetNames.size(); i++) {
                hWorkbook.createSheet(sheetNames.get(i));
                hWorkbook.getSheet(sheetNames.get(i)).createRow(0);
                //添加表头, 创建第一行
                row = hWorkbook.getSheet(sheetNames.get(i)).createRow(0);
                row.setHeight((short) (20 * 20));
                for (short j = 0; j < titleList.get(i).length; j++) {
                    HSSFCell cell = row.createCell(j, CellType.BLANK);
                    cell.setCellValue(titleList.get(i)[j]);
                    cell.setCellStyle(cellStyle);
                }
                fileOutputStream = new FileOutputStream(fileDir);
                hWorkbook.write(fileOutputStream);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除文件.
     * @param fileDir  文件路径
     * @return 如果文件不存在返回false, 如果文件存在删除成功之后返回true
     */
    public static boolean deleteExcel(String fileDir) {
        boolean flag = false;
        File file = new File(fileDir);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                file.delete();
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 往excel(xls)中写入(已存在的数据无法写入).
     * @param fileDir    文件路径
     * @param sheetName  表格索引
     *                   * @throws Exception
     */

    public static void writeToExcelXls(String fileDir, String sheetName, List<Task> taskList) throws Exception {

        //创建workbook
        File file = new File(fileDir);

        try {
            hWorkbook = new HSSFWorkbook(new FileInputStream(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //文件流
        FileOutputStream fileOutputStream = null;
        HSSFSheet sheet = hWorkbook.getSheet(sheetName);
        // 获取表格的总行数
        // int rowCount = sheet.getLastRowNum() + 1; // 需要加一
        //获取表头的列数
        int columnCount = sheet.getRow(0).getLastCellNum();

        try {
            // 获得表头行对象
            HSSFRow titleRow = sheet.getRow(0);
            //创建单元格显示样式
            CellStyle cellStyle = hWorkbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);


            if (titleRow != null) {
                for (int rowId = 0; rowId < taskList.size(); rowId++) {
                    Task task = taskList.get(rowId);
                    HSSFRow newRow = sheet.createRow(rowId + 1);
                    newRow.setHeight((short) (20 * 20));//设置行高  基数为20
                    for (short columnIndex = 0; columnIndex < columnCount; columnIndex++) {  //遍历表头
                        //trim()的方法是删除字符串中首尾的空格
                        String titleString = titleRow.getCell(columnIndex).toString().trim();
                        HSSFCell cell = newRow.createCell(columnIndex);
                        cell.setCellStyle(cellStyle);
                        switch (titleString)
                        {
                            case "id":
                                cell.setCellValue(task.getTaskId());
                                break;
                            case "sku":
                                cell.setCellValue(task.getTaskSKU());
                                break;
                            case "appointMachine":
                                cell.setCellValue(task.getAppointMachine());
                                break;
                            case "startTime":
                                cell.setCellValue(task.getStartWorkTime());
                                break;
                            case "endTime":
                                cell.setCellValue(task.getEndWorkTime());
                                break;
                            case "demandCases":
                                cell.setCellValue(task.getDemandCases());
                                break;
                        }
                    }
                }
            }

            fileOutputStream = new FileOutputStream(fileDir);
            hWorkbook.write(fileOutputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToExcelXlsMachine(String fileDir, String sheetName, Map<String,List<List<Integer>>> totalMachineTransTimeMap) throws Exception {

        //创建workbook
        File file = new File(fileDir);

        try {
            hWorkbook = new HSSFWorkbook(new FileInputStream(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //文件流
        FileOutputStream fileOutputStream = null;
        HSSFSheet sheet = hWorkbook.getSheet(sheetName);
        // 获取表格的总行数
        // int rowCount = sheet.getLastRowNum() + 1; // 需要加一
        //获取表头的列数
        int columnCount = sheet.getRow(0).getLastCellNum();

        try {
            // 获得表头行对象
            HSSFRow titleRow = sheet.getRow(0);
            //创建单元格显示样式
            CellStyle cellStyle = hWorkbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);


            if (titleRow != null) {
                int rowId = 0;
                for (Map.Entry<String, List<List<Integer>>> stringListEntry : totalMachineTransTimeMap.entrySet()) {
                    String machine = stringListEntry.getKey();
                    List<List<Integer>> valueList = stringListEntry.getValue();
                    for (List<Integer> integers : valueList) {
                        HSSFRow newRow = sheet.createRow(rowId + 1);
                        newRow.setHeight((short) (20 * 20));//设置行高  基数为20
                        for (short columnIndex = 0; columnIndex < columnCount; columnIndex++) {  //遍历表头
                            //trim()的方法是删除字符串中首尾的空格
                            String titleString = titleRow.getCell(columnIndex).toString().trim();
                            HSSFCell cell = newRow.createCell(columnIndex);
                            cell.setCellStyle(cellStyle);
                            switch (titleString) {
                                case "machine":
                                    cell.setCellValue(machine);
                                    break;
                                case "transStartTime":
                                    cell.setCellValue(integers.get(0));
                                    break;
                                case "transEndTime":
                                    cell.setCellValue(integers.get(1));
                                    break;
                            }
                        }
                        rowId++;
                    }
                }
            }
            fileOutputStream = new FileOutputStream(fileDir);
            hWorkbook.write(fileOutputStream);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * 创建Excel(xlsx)
     * @param fileDir  文件名称及地址
     * @param sheetName sheet的名称
     * @param titleRow  表头
     */
    public static void createExcelXlsx(String fileDir, String sheetName, String titleRow[]) {


    }


    public  void outPutResult(String fileDir, List<Task> taskList, Map<String,List<List<Integer>>> totalMachineTransTimeMap) {

//        String fileDir = "d:\\workbook.xls";

        List<String> sheetName = new ArrayList<>();

        sheetName.add("TaskMesg");
        sheetName.add("MachineMesg");

        System.out.println(sheetName);

        String[] title1 = {"id", "sku", "appointMachine","startTime","endTime","demandCases"};
        String[] title2 = {"machine", "transStartTime", "transEndTime"};
        List<String[]> titleList=new ArrayList<>();
        titleList.add(title1);
        titleList.add(title2);

//        ast_tc.packing.GetResult getResult=new ast_tc.packing.GetResult();
//        List<ast_tc.packing.Task> taskList = new ArrayList<>();
//        ast_tc.packing.Task task=new ast_tc.packing.Task();
//        task.setTaskId(1);
//        task.setTaskSKU("fasdfsd");
//        task.setAppointMachine("stl");
//        task.setStartWorkTime(122);
//        task.setEndWorkTime(234);
//        taskList.add(task);
//
//
//        Map<String,List<List<Integer>>> totalMachineTransTimeMap=new HashMap<>();
//        List<List<Integer>> tlist=new ArrayList<>();
//        List<Integer> tlist2=new ArrayList<>();
//        tlist2.add(1);
//        tlist2.add(2);
//        tlist.add(tlist2);
//        totalMachineTransTimeMap.put("sdf",tlist);



        createExcelXls(fileDir, sheetName, titleList);

        try {
            writeToExcelXls(fileDir, sheetName.get(0), taskList);
            writeToExcelXlsMachine(fileDir, sheetName.get(1), totalMachineTransTimeMap);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

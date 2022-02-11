package cplexForASP.asp_tc.mixing;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetInputDataMix {
    private XSSFSheet sheet;
    private List<TaskMix> taskList;
    private double[][] taskTransTime;
    private List<String> machineList;
    private Map<String,List<TaskMix>> taskMap;
    private Date startDate;
    private Map<String,Map<Integer,List<TaskMix>>> taskSequenceListMap;
    private DecimalFormat df=new DecimalFormat("#.00");


    public void initialData(String filePath,String dateStart) {
        startDate=new Date(dateStart);
        taskList=new ArrayList<TaskMix>();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            XSSFWorkbook sheets = new XSSFWorkbook(fileInputStream);
            //获取sheet





            sheet=sheets.getSheet("MixTask");
            int startRow5=1;//开始行
            int totalRows5 = sheet.getPhysicalNumberOfRows();
//            int totalRows5 =120;
            List<Integer> ignorList=new ArrayList<>();
//            ignorList.add(66);
//            ignorList.add(99);
//            ignorList.add(136);
//            ignorList.add(146);
            taskMap=new HashMap<>();
            int taskId5=0;
            for(int i=startRow5;i<totalRows5;i++) {
                if(!ignorList.contains(i)) {
                    double demandCases = Double.parseDouble(df.format(Double.valueOf(getExcelDataByIndex(i,4))));
                    if (demandCases!= 0.0) {//仅当需求不为0时加入到模型中
                        TaskMix task = new TaskMix();
                        task.setTaskId(taskId5);
                        task.setTaskSKU(getExcelDataByIndex(i, 1));
                        task.setProductSku(getExcelDataByIndex(i, 0));
                        task.setSection(Double.valueOf(getExcelDataByIndex(i, 2)).intValue());
                        task.setSkuColor(getExcelDataByIndex(i, 3));
                        task.setDemandTons(Double.parseDouble(df.format(Double.valueOf(getExcelDataByIndex(i,4)))));
                        task.setEndWorkTime(Double.parseDouble(df.format(Double.valueOf(getExcelDataByIndex(i,13)))));
                        task.setStartWorkTime(Double.parseDouble(df.format(Double.valueOf(getExcelDataByIndex(i,12)))));

                        List<Double> producityList = new ArrayList<Double>();
                        List<Double> workTimeList = new ArrayList<Double>();
                        Double producityValue = Double.valueOf(getExcelDataByIndex(i, 7));
                        producityList.add(producityValue);
                        double workTime=task.getDemandTons() * 2.5 + 10 + task.getDemandTons() * 1000 / producityValue;
                        workTimeList.add(Double.valueOf(df.format(workTime)));

                        task.setMachineProductivityList(producityList);
                        task.setMachineWorkTimeList(workTimeList);

                        int dueDayColumn = 6;//交期时间

                        double dueMinute = Double.parseDouble(df.format(Double.valueOf(getExcelDataByIndex(i,dueDayColumn))));
                        double standbyMinute =Double.parseDouble(df.format(Double.valueOf(getExcelDataByIndex(i,dueDayColumn-1))));
                        task.setDueMinute(dueMinute);
                        task.setStandByMinute(standbyMinute);
                        taskList.add(task);
                        if (taskMap.containsKey(task.getTaskSKU())) {
                            List<TaskMix> tasks = taskMap.get(task.getTaskSKU());
                            tasks.add(task);
                            taskMap.put(task.getTaskSKU(), tasks);
                        } else {
                            List<TaskMix> tasks = new ArrayList<>();
                            tasks.add(task);
                            taskMap.put(task.getTaskSKU(), tasks);
                        }
                        taskId5++;
                    }
                }
            }




            taskTransTime=new double[taskList.size()][taskList.size()];

            for (int i = 0; i < taskList.size(); i++) {
                for (int j = 0; j < taskList.size(); j++) {
                    taskTransTime[i][j]=0;//默认都切换很大时间
                }
            }



            //获取第二个sheet页---TransTime
            sheet = sheets.getSheet("TransTime");
            int startRow2=1;//开始行
            int totalRows2 = sheet.getPhysicalNumberOfRows();
            for(int i=startRow2;i<totalRows2;i++) {
                String beforeSku = getExcelDataByIndex(i, 0);
                String afterSku = getExcelDataByIndex(i, 1);
                Double transTime = Double.valueOf(getExcelDataByIndex(i, 2));
                List<TaskMix> taskBeforeList = taskMap.get(beforeSku);
                List<TaskMix> taskAfterList = taskMap.get(afterSku);
                if (taskBeforeList != null && taskAfterList != null) {
                    for (TaskMix task1 : taskBeforeList) {
                        int task1Index = taskList.indexOf(task1);
                        for (TaskMix task2 : taskAfterList) {
                            int task2Index = taskList.indexOf(task2);
                            taskTransTime[task1Index][task2Index] = transTime;
                        }
                    }
                }
            }
////
//            for (int i = 0; i < taskList.size(); i++) {
//                for (int j = 0; j < taskList.size(); j++) {
//                    if (taskTransTime[i][j]>0)
//                    {
//                        System.out.println("*************");
//                        System.out.println(taskList.get(i).getSkuColor());
//                        System.out.println(taskTransTime[i][j]);
//                        System.out.println(taskList.get(j).getSkuColor());
//                        System.out.println("*************");
//                    }
////                        System.out.println("\t"+taskList.get(i).getTaskSKU()+"\t"+taskList.get(j).getTaskSKU()+"\t"+taskTransTime[i][j]);
//
//                }
//            }









        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("totalTask:"+taskList.size());
    }




    /**
     * 根据行和列的索引获取单元格的数据
     *
     * @param row
     * @param column
     * @return
     */
    public String getExcelDataByIndex(int row, int column) {
        XSSFRow row1 = sheet.getRow(row);
        XSSFCell cell1 = row1.getCell(column);
        String cell=null;
        if (cell1!=null)
        {
            cell=cell1.toString();
        }
        return cell;
    }

    public Date getExcelTimeByIndex(int row,int column) throws ParseException {
        XSSFRow row1 = sheet.getRow(row);
        Date date=new Date();
        XSSFCell cell = row1.getCell(column);
        String str=cell.toString();
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            date  = new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            date  = cell.getDateCellValue();
        }
        date=date;
        return date;
    }

    /**
     * 根据某一列值为“******”的这一行，来获取该行第x列的值
     *
     * @param caseName
     * @param currentColumn 当前单元格列的索引
     * @param targetColumn  目标单元格列的索引
     * @return
     */
    public String getCellByCaseName(String caseName, int currentColumn, int targetColumn) {
        String operateSteps = "";
        //获取行数
        int rows = sheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            XSSFRow row = sheet.getRow(i);
            String cell = row.getCell(currentColumn).toString();
            if (cell.equals(caseName)) {
                operateSteps = row.getCell(targetColumn).toString();
                break;
            }
        }
        return operateSteps;
    }

    //打印excel数据
    public void readExcelData() {
        //获取行数
        int rows = sheet.getPhysicalNumberOfRows();
        for (int i = 0; i < rows; i++) {
            //获取列数
            XSSFRow row = sheet.getRow(i);
            int columns = row.getPhysicalNumberOfCells();
            for (int j = 0; j < columns; j++) {
                String cell = row.getCell(j).toString();
                System.out.println(cell);
            }
        }
    }




    // 计算两个时间差，返回为分钟。
    public  Integer calTime(Date  firstDate, Date secondDate) throws ParseException {
        //通过字符串创建两个日期对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm");


        //TODO 由于excel日期标准的是当天，所以需+1天处理
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(secondDate);
        calendar.add(Calendar.DATE, 1);
        secondDate=calendar.getTime();

        //得到两个日期对象的总毫秒数
        long firstDateMilliSeconds = firstDate.getTime();
        long secondDateMilliSeconds = secondDate.getTime();
        //得到两者之差
        long firstMinusSecond = secondDateMilliSeconds -firstDateMilliSeconds;

        //毫秒转为秒
        long milliSeconds = firstMinusSecond;
        int totalSeconds = (int)(milliSeconds / 1000);
        Integer minutes=totalSeconds / 60;
        return minutes;
    }







    public List<TaskMix> getTaskList() {
        return taskList;
    }


    public XSSFSheet getSheet() {
        return sheet;
    }

    public void setSheet(XSSFSheet sheet) {
        this.sheet = sheet;
    }

    public void setTaskList(List<TaskMix> taskList) {
        this.taskList = taskList;
    }

    public double[][] getTaskTransTime() {
        return taskTransTime;
    }

    public void setTaskTransTime(double[][] taskTransTime) {
        this.taskTransTime = taskTransTime;
    }

    public List<String> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<String> machineList) {
        this.machineList = machineList;
    }

    public Map<String, List<TaskMix>> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<String, List<TaskMix>> taskMap) {
        this.taskMap = taskMap;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Map<String, Map<Integer, List<TaskMix>>> getTaskSequenceListMap() {
        return taskSequenceListMap;
    }

    public void setTaskSequenceListMap(Map<String, Map<Integer, List<TaskMix>>> taskSequenceListMap) {
        this.taskSequenceListMap = taskSequenceListMap;
    }


}


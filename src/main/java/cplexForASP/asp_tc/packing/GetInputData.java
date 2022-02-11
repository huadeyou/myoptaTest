package cplexForASP.asp_tc.packing;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetInputData {
    private XSSFSheet sheet;
    private List<Task> taskList;
    private int[][][] taskTransTime;
    private List<String> machineList;
    private Map<String,List<Task>> taskMap;
    private Date startDate;
    private Map<String,Map<Integer,List<Task>>> taskSequenceListMap;
    private List<Group> groupList;

    public void initialData(String filePath,String dateStart) {
        startDate=new Date(dateStart);
        taskList=new ArrayList<Task>();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            XSSFWorkbook sheets = new XSSFWorkbook(fileInputStream);
            //获取sheet
            machineList=new ArrayList<String>();
            machineList.add("BD");
            machineList.add("Gram");
            machineList.add("HS");
            machineList.add("R1");
            machineList.add("R2");
            machineList.add("SL1");
            machineList.add("SL2");
            machineList.add("SL3");
            machineList.add("SL4");
            machineList.add("V1");
            machineList.add("V2");
            machineList.add("V3");
            machineList.add("ILF");






            //获取第一个sheet页---任务sheet
            sheet = sheets.getSheet("Task");
            int startRow=2;//开始行
            int totalRows = sheet.getPhysicalNumberOfRows();
            taskMap=new HashMap<>();
            int taskId=0;
            for(int i=startRow;i<totalRows;i++) {
                int demandCases = Double.valueOf(getExcelDataByIndex(i, 2)).intValue();
                if (demandCases!=0) {//仅当需求为0时加入到模型中
                    Task task = new Task();
                    task.setTaskId(taskId);
                    task.setTaskSKU(getExcelDataByIndex(i, 0));
                    task.setDemandCases(Double.valueOf(getExcelDataByIndex(i, 2)).intValue());
                    List<Double> producityList = new ArrayList<Double>();
                    List<Double> workTimeList = new ArrayList<Double>();
                    for (int j = 5; j < machineList.size() + 5; j++) {
                        Double producityValue = Double.valueOf(getExcelDataByIndex(i, j));
                        producityList.add(producityValue);
                        workTimeList.add(task.getDemandCases() / producityValue);
                    }
                    task.setMachineProductivityList(producityList);
                    task.setMachineWorkTimeList(workTimeList);
                    List<Integer> humandList = new ArrayList<Integer>();
                    for (int j = machineList.size() + 5; j < 2 * machineList.size() + 5; j++) {
                        humandList.add(Double.valueOf(getExcelDataByIndex(i, j)).intValue());
                    }
                    task.setMachineHumanList(humandList);
                    int dueDayColumn = 4;//交期时间
                    task.setDueDay(getExcelTimeByIndex(i, dueDayColumn));
                    Integer dueMinute = calTime(startDate, getExcelTimeByIndex(i, dueDayColumn));
                    task.setDueMinute(dueMinute);
                    task.setStandByMinute(0);//TODO 暂时默认所有任务包材准备好时间为0
                    taskList.add(task);
                    if (taskMap.containsKey(task.getTaskSKU())) {
                        List<Task> tasks = taskMap.get(task.getTaskSKU());
                        tasks.add(task);
                        taskMap.put(task.getTaskSKU(), tasks);
                    } else {
                        List<Task> tasks = new ArrayList<>();
                        tasks.add(task);
                        taskMap.put(task.getTaskSKU(), tasks);
                    }
                    taskId++;
                }
            }


            taskTransTime=new int[taskList.size()][taskList.size()][machineList.size()];
            for (int m = 0; m < machineList.size(); m++) {
                for (int i = 0; i < taskList.size(); i++) {
                    for (int j = 0; j < taskList.size(); j++) {
                        taskTransTime[i][j][m]=-1;//默认都切换很大时间
                    }
                }
            }


            //获取第二个sheet页---TransTime
            sheet = sheets.getSheet("TransTime");
            int startRow2=1;//开始行
            int totalRows2 = sheet.getPhysicalNumberOfRows();
            for(int i=startRow2;i<totalRows2;i++) {
                String machine = getExcelDataByIndex(i, 0);
                String beforeSku = getExcelDataByIndex(i, 1);
                String afterSku = getExcelDataByIndex(i, 2);
                Integer transTime = Double.valueOf(getExcelDataByIndex(i, 3)).intValue();
                if (machineList.contains(machine)) {
                    int machineIndex = machineList.indexOf(machine);
                    List<Task> taskBeforeList = taskMap.get(beforeSku);
                    List<Task> taskAfterList = taskMap.get(afterSku);
                    if (taskBeforeList != null && taskAfterList != null) {
                        for (Task task1 : taskBeforeList) {
                            int task1Index = taskList.indexOf(task1);
                            for (Task task2 : taskAfterList) {
                                int task2Index = taskList.indexOf(task2);
                                taskTransTime[task1Index][task2Index][machineIndex] = transTime;
                            }
                        }
                    }
                }
            }

//            for (int i = 0; i < taskList.size(); i++) {
//                for (int j = 0; j < taskList.size(); j++) {
//                    for (int m = 0; m < machineList.size(); m++) {
//                        System.out.println(machineList.get(m)+"\t"+taskList.get(i).getTaskSKU()+"\t"+taskList.get(j).getTaskSKU()+"\t"+taskTransTime[i][j][m]);
//                    }
//                }
//            }



//            System.out.println(taskMap);
            //读取排序表
            sheet=sheets.getSheet("sequence");
            taskSequenceListMap=new HashMap<>();
            int startRow3=1;
            int totalRows3 = sheet.getPhysicalNumberOfRows();
            for(int i=startRow3;i<totalRows3;i++) {
                String machine = getExcelDataByIndex(i, 0);
                String sku = getExcelDataByIndex(i, 1);
                List<Task> tasks = taskMap.get(sku);
                Integer sequence = Double.valueOf(getExcelDataByIndex(i, 2)).intValue();
                if (tasks!=null && !sequence.equals(-1)) {
                    if (taskSequenceListMap.containsKey(machine))//如果包含机器
                    {
                        Map<Integer, List<Task>> integerListMap = taskSequenceListMap.get(machine);
                        if (integerListMap.containsKey(sequence)) {
                            for (Task task : tasks) {
                                List<Task> tasks1 = integerListMap.get(sequence);
                                integerListMap.get(sequence).add(task);
                            }

                        } else {
                            List<Task> taskNewList=new ArrayList<>();
                            for (Task task : tasks) {
                                taskNewList.add(task);
                            }
                            integerListMap.put(sequence, taskNewList);
                        }
                        taskSequenceListMap.put(machine,integerListMap);
                    } else {
                        Map<Integer, List<Task>> integerListMap = new HashMap<>();
                        List<Task> taskNewList=new ArrayList<>();
                        for (Task task : tasks) {
                            taskNewList.add(task);
                        }
                        integerListMap.put(sequence,taskNewList);
                        taskSequenceListMap.put(machine, integerListMap);
                    }
                }
                else
                {
//                    System.out.println(sku);
                }
            }


            sheet=sheets.getSheet("group");
            groupList=new ArrayList<>();
            int startRow4=1;
            int totalRow4=sheet.getPhysicalNumberOfRows();
            int maxColumn=14;
            int groupIndex=0;
            for(int i=startRow4;i<totalRow4;i++) {

                String machine = getExcelDataByIndex(i, 1);
                List<Task> taskList=new ArrayList<>();
                for(int j=2;j<maxColumn;j++)
                {

                    String sku = getExcelDataByIndex(i, j);
                    if (sku!=null && !sku.equals("") )
                    {
                        List<Task> tasks = taskMap.get(sku);
                        if (tasks!=null) {
                            taskList.addAll(tasks);
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                if (taskList.size()>0)
                {
                    Group group=new Group();
                    group.setGroupId(groupIndex);
                    group.setGroupTaskList(taskList);
                    group.setMachine(machine);
                    groupList.add(group);
                    groupIndex++;
                }
            }







        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("totalTask:"+taskList.size());
        System.out.println("totalMachine:"+machineList.size());
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
//                System.out.println(cell);
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







    public List<Task> getTaskList() {
        return taskList;
    }


    public XSSFSheet getSheet() {
        return sheet;
    }

    public void setSheet(XSSFSheet sheet) {
        this.sheet = sheet;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public int[][][] getTaskTransTime() {
        return taskTransTime;
    }

    public void setTaskTransTime(int[][][] taskTransTime) {
        this.taskTransTime = taskTransTime;
    }

    public List<String> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<String> machineList) {
        this.machineList = machineList;
    }

    public Map<String, List<Task>> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<String, List<Task>> taskMap) {
        this.taskMap = taskMap;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Map<String, Map<Integer, List<Task>>> getTaskSequenceListMap() {
        return taskSequenceListMap;
    }

    public void setTaskSequenceListMap(Map<String, Map<Integer, List<Task>>> taskSequenceListMap) {
        this.taskSequenceListMap = taskSequenceListMap;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }
}


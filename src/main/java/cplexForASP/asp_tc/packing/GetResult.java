package cplexForASP.asp_tc.packing;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行前需先引入cplex jar包
 */
public class GetResult {
    private Map<String, List<Task>> totalMachineTaskList;
    private Map<String,List<List<Integer>>> totalMachineTransTimeMap;
    private SolveModel solveModel;
    private IloCplex model ;
    private IloNumVar[][][] varIJK;
    private IloNumVar[][] varTaskOnMachine;
    private IloNumVar[][] varMachineStartTask;
    private IloNumVar[][] varMachineEndTask;
    private IloNumVar[] varStartTime;
    private IloNumVar[] varEndTime;
    private List<Task> taskList;
    private List<String> machineList;
    private DecimalFormat df = new DecimalFormat("#####0");
    private String dataInput;
    private String dataOutput;
    private String dateStart;

    public void initialResult() throws IloException {
        String fileName="天津APS-POC基础数据";
//        dataInput="C:\\Users\\HuaDeyou\\Desktop\\asp\\"+fileName+".xlsx";
        dataInput="C:\\Users\\HuaDeyou\\Desktop\\aps-TJ\\"+fileName+".xlsx";
        dataOutput ="C:\\Users\\HuaDeyou\\Desktop\\aps-TJ\\"+fileName+"_output.xls";
        dateStart="2020/4/6 00:00";
        totalMachineTaskList=new HashMap<>();
        totalMachineTransTimeMap=new HashMap<>();
        solveModel =new SolveModel();
        solveModel.initialParameter(dataInput,dateStart);
        solveModel.modelBuild();
        model = solveModel.getModel();
        varIJK = solveModel.getVarIJK();
        varTaskOnMachine = solveModel.getVarTaskOnMachine();
        varMachineStartTask = solveModel.getVarMachineStartTask();
        varMachineEndTask = solveModel.getVarMachineEndTask();
        varStartTime = solveModel.getVarStartTime();
        varEndTime = solveModel.getVarEndTime();
        taskList=solveModel.getTaskList();
        machineList=solveModel.getMachineList();
        model.setParam(IloCplex.Param.TimeLimit, 100);
        model.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.05);
        if (model.solve())
        {
            getTotalMachineTaskList();
            getTaskWorkTime();
            getMachineTransTime();
            System.out.println("success!");
            for (String machine : machineList) {
                System.out.print(machine+":");
                List<Task> tasks = totalMachineTaskList.get(machine);
                for (Task task : tasks) {
                    System.out.print(task.getTaskSKU()+",");
                }
                System.out.println();
            }
        }
        outputResult(dataOutput);
    }

    public static void main(String[] args) throws IloException {
        GetResult getResult=new GetResult();
        getResult.initialResult();

    }


    public void getTotalMachineTaskList() throws IloException {
        for (int k = 0; k < machineList.size(); k++) {
            List<Task> temTaskList=new ArrayList<Task>();
            Task startTask=new Task();
            Task endTask=new Task();
            //先取到该机器所有task，且找到第一个task
            for (int i = 0; i < taskList.size(); i++) {
                if (Integer.parseInt(df.format(model.getValue(varTaskOnMachine[i][k])))==1)
                {
                    temTaskList.add(taskList.get(i));
                }
            }
            //任务分配机器
            for (Task task : temTaskList) {
                task.setAppointMachine(machineList.get(k));
            }

//            for (ast_tc.packing.Task task : temTaskList) {
//                int i = taskList.indexOf(task);
//                System.out.println("任务"+i);
//                for (int j = 0; j < taskList.size(); j++) {
//                    for (int m = 0; m < machineList.size(); m++) {
//                        if (Integer.parseInt(df.format(model.getValue(varIJK[i][j][m])))==1)
//                        {
//                            System.out.println("紧后任务："+j+"-"+m);
//                        }
//                        if (Integer.parseInt(df.format(model.getValue(varIJK[j][i][m])))==1)
//                        {
//                            System.out.println("紧前任务："+j+"-"+m);
//                        }
//                    }
//
//                }
//            }


            for (int t = 0; t < temTaskList.size(); t++) {
                int i = taskList.indexOf(temTaskList.get(t));
                if (Integer.parseInt(df.format(model.getValue(varMachineStartTask[i][k])))==1)
                {
                    startTask=taskList.get(i);
//                    System.out.println("taskStart:"+startTask.getTaskSKU());
                }
                if (Integer.parseInt(df.format(model.getValue(varMachineEndTask[i][k])))==1)
                {
                    endTask=taskList.get(i);
//                    System.out.println("taskEnd:"+endTask.getTaskSKU());
                }
            }

            Task currentTask=startTask;
            List<Task> machineOrderTaskList=new ArrayList<Task>();
            machineOrderTaskList.add(startTask);
            temTaskList.remove(startTask);

            while (temTaskList.size()>1) {
                Task nextTask = getNextTaskOnMachine(currentTask, temTaskList, k);
                currentTask=nextTask;
                machineOrderTaskList.add(nextTask);
                temTaskList.remove(nextTask);
            }
            if (temTaskList.size()!=0) {
                machineOrderTaskList.add(endTask);
            }
            totalMachineTaskList.put(machineList.get(k),machineOrderTaskList);
        }
    }


    /**
     * 获取每个任务的开始和结束时间
     * @throws IloException
     */
    public void  getTaskWorkTime() throws IloException {
        for (Task task : taskList) {
            int i = taskList.indexOf(task);
            int t1 = Integer.parseInt(df.format(model.getValue(varStartTime[i])));
            int t2 = Integer.parseInt(df.format(model.getValue(varEndTime[i])));
            task.setStartWorkTime(t1);
            task.setEndWorkTime(t2);
        }
    }



    public void getMachineTransTime()
    {
        for (String machine : machineList) {
            List<Task> tasks = totalMachineTaskList.get(machine);
            List<List<Integer>> temTransTimeList=new ArrayList<List<Integer>>();
            List<Integer> timeFlagList=new ArrayList<Integer>();
            for (int i = 0; i < tasks.size(); i++) {
                if (i==0)
                {
                    timeFlagList.add(tasks.get(i).getEndWorkTime());
                }else if (i==tasks.size()-1)
                {
                    timeFlagList.add(tasks.get(i).getStartWorkTime());
                }
                else
                {
                    timeFlagList.add(tasks.get(i).getStartWorkTime());
                    timeFlagList.add(tasks.get(i).getEndWorkTime());
                }
            }
            if (timeFlagList.size()>1)//当机器只有一个任务则无需计算切换时间
            {
                for (int i = 0; i < timeFlagList.size(); i=i+2) {
                    if (!timeFlagList.get(i).equals(timeFlagList.get(i+1)))
                    {
                        List<Integer> temList=new ArrayList<>();
                        temList.add(timeFlagList.get(i));
                        temList.add(timeFlagList.get(i+1));
                        temTransTimeList.add(temList);
                    }
                }
            }

            totalMachineTransTimeMap.put(machine,temTransTimeList);
        }
    }





    /**
     * 获取当前任务的下一个任务
     * @param taskBefor
     * @param machineTaskList
     * @param machineIndex
     * @return
     * @throws IloException
     */
    public Task getNextTaskOnMachine(Task taskBefor,List<Task> machineTaskList,int machineIndex) throws IloException {
        Task nextTask=new Task();
        int i = taskList.indexOf(taskBefor);
        for (Task task : machineTaskList) {
            int j = taskList.indexOf(task);
            if (Integer.parseInt(df.format(model.getValue(varIJK[i][j][machineIndex])))==1)
            {
                nextTask=taskList.get(j);
            }
        }
        return nextTask;
    }


    public void outputResult(String  dateOutput)
    {
        CreateExcelFile createExcelFile=new CreateExcelFile();
        String outPutPath=dateOutput;
        createExcelFile.outPutResult(outPutPath,taskList,totalMachineTransTimeMap);
    }

    public void setTotalMachineTaskList(Map<String, List<Task>> totalMachineTaskList) {
        this.totalMachineTaskList = totalMachineTaskList;
    }

    public Map<String, List<List<Integer>>> getTotalMachineTransTimeMap() {
        return totalMachineTransTimeMap;
    }

    public void setTotalMachineTransTimeMap(Map<String, List<List<Integer>>> totalMachineTransTimeMap) {
        this.totalMachineTransTimeMap = totalMachineTransTimeMap;
    }

    public SolveModel getSolveModel() {
        return solveModel;
    }

    public void setSolveModel(SolveModel solveModel) {
        this.solveModel = solveModel;
    }

    public IloCplex getModel() {
        return model;
    }

    public void setModel(IloCplex model) {
        this.model = model;
    }

    public IloNumVar[][][] getVarIJK() {
        return varIJK;
    }

    public void setVarIJK(IloNumVar[][][] varIJK) {
        this.varIJK = varIJK;
    }

    public IloNumVar[][] getVarTaskOnMachine() {
        return varTaskOnMachine;
    }

    public void setVarTaskOnMachine(IloNumVar[][] varTaskOnMachine) {
        this.varTaskOnMachine = varTaskOnMachine;
    }

    public IloNumVar[][] getVarMachineStartTask() {
        return varMachineStartTask;
    }

    public void setVarMachineStartTask(IloNumVar[][] varMachineStartTask) {
        this.varMachineStartTask = varMachineStartTask;
    }

    public IloNumVar[][] getVarMachineEndTask() {
        return varMachineEndTask;
    }

    public void setVarMachineEndTask(IloNumVar[][] varMachineEndTask) {
        this.varMachineEndTask = varMachineEndTask;
    }

    public IloNumVar[] getVarStartTime() {
        return varStartTime;
    }

    public void setVarStartTime(IloNumVar[] varStartTime) {
        this.varStartTime = varStartTime;
    }

    public IloNumVar[] getVarEndTime() {
        return varEndTime;
    }

    public void setVarEndTime(IloNumVar[] varEndTime) {
        this.varEndTime = varEndTime;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public List<String> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<String> machineList) {
        this.machineList = machineList;
    }

    public DecimalFormat getDf() {
        return df;
    }

    public void setDf(DecimalFormat df) {
        this.df = df;
    }
}

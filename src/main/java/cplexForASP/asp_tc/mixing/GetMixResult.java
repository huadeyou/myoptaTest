package cplexForASP.asp_tc.mixing; /**
 * .@author HuaDeyou
 * .@date 9-3
 */
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMixResult {
    private Map<String, List<TaskMix>> totalMachineTaskList;
    private Map<String,List<List<Integer>>> totalMachineTransTimeMap;
    private SolveMixModel solveModel;
    private IloCplex model ;
    private IloNumVar[][] varIJK;
    private IloNumVar[] varMachineStartTask;
    private IloNumVar[] varMachineEndTask;
    private IloNumVar[] varStartTime;
    private IloNumVar[] varEndTime;
    private List<TaskMix> taskList;
    private List<String> machineList;
    private DecimalFormat df = new DecimalFormat("#####0");
    private String dataInput;
    private String dataOutput;
    private String dateStart;

    public void initialResult() throws IloException {
        String fileName="wk15 mix-v5";
//        String pathName="/home/ctu01/algorithm/xxl-job-2.1/executor2/";
        String pathName="C:\\Users\\HuaDeyou\\Desktop\\asp\\MIX\\";
        dataInput=pathName+fileName+".xlsx";
        dataOutput =pathName+fileName+"_output87.xls";
        dateStart="2020/4/6 16:00";
        totalMachineTaskList=new HashMap<>();
        totalMachineTransTimeMap=new HashMap<>();
        solveModel =new SolveMixModel();
        solveModel.initialParameter(dataInput,dateStart);
        solveModel.modelBuild();
        model = solveModel.getModel();
        varIJK = solveModel.getVarIJK();
        varMachineStartTask = solveModel.getVarMachineStartTask();
        varMachineEndTask = solveModel.getVarMachineEndTask();
        varStartTime = solveModel.getVarStartTime();
        varEndTime = solveModel.getVarEndTime();
        taskList=solveModel.getTaskList();
        machineList=solveModel.getMachineList();
        model.setParam(IloCplex.Param.TimeLimit, 10000);
        model.exportModel("C:\\Users\\HuaDeyou\\Desktop\\asp\\MIX\\model.lp");
        model.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.05);
        model.setParam(IloCplex.Param.Emphasis.MIP,1);
        model.setParam(IloCplex.Param.MIP.Strategy.RINSHeur	,10);
//        model.setParam(IloCplex.Param.MIP.Strategy.HeuristicFreq	,10);
//        model.setParam(IloCplex.Param.MIP.Strategy.NodeSelect	,0);
        model.setParam(IloCplex.Param.TimeLimit,22500);
        solveModel.addMipStart();
        if (model.solve())
        {
            System.out.println("success");
            getTaskWorkTime();
//            getMachineTransTime();

        }
        outputResult(dataOutput);
    }

    public static void main(String[] args) throws IloException {
        GetMixResult getResult=new GetMixResult();
        getResult.initialResult();

    }





    /**
     * 获取每个任务的开始和结束时间
     * @throws IloException
     */
    public void  getTaskWorkTime() throws IloException {
        for (TaskMix task : taskList) {
            int i = taskList.indexOf(task);
            int t1 = Integer.parseInt(df.format(model.getValue(varStartTime[i])));
            int t2 = Integer.parseInt(df.format(model.getValue(varEndTime[i])));
            task.setStartWorkTime(t1);
            task.setEndWorkTime(t2);
        }
    }



    public void getMachineTransTime()
    {
            List<List<Double>> temTransTimeList=new ArrayList<List<Double>>();
            List<Double> timeFlagList=new ArrayList<Double>();
            for (int i = 0; i < taskList.size(); i++) {
                if (i==0)
                {
                    timeFlagList.add(taskList.get(i).getEndWorkTime());
                }else if (i==taskList.size()-1)
                {
                    timeFlagList.add(taskList.get(i).getStartWorkTime());
                }
                else
                {
                    timeFlagList.add(taskList.get(i).getStartWorkTime());
                    timeFlagList.add(taskList.get(i).getEndWorkTime());
                }
            }
            if (timeFlagList.size()>1)//当机器只有一个任务则无需计算切换时间
            {
                for (int i = 0; i < timeFlagList.size(); i=i+2) {
                    if (!timeFlagList.get(i).equals(timeFlagList.get(i+1)))
                    {
                        List<Double> temList=new ArrayList<>();
                        temList.add(timeFlagList.get(i));
                        temList.add(timeFlagList.get(i+1));
                        temTransTimeList.add(temList);
                    }
                }
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
    public TaskMix getNextTaskOnMachine(TaskMix taskBefor, List<TaskMix> machineTaskList, int machineIndex) throws IloException {
        TaskMix nextTask=new TaskMix();
        int i = taskList.indexOf(taskBefor);
        for (TaskMix task : machineTaskList) {
            int j = taskList.indexOf(task);
            if (Integer.parseInt(df.format(model.getValue(varIJK[i][j])))==1)
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

    public void setTotalMachineTaskList(Map<String, List<TaskMix>> totalMachineTaskList) {
        this.totalMachineTaskList = totalMachineTaskList;
    }

    public Map<String, List<List<Integer>>> getTotalMachineTransTimeMap() {
        return totalMachineTransTimeMap;
    }

    public void setTotalMachineTransTimeMap(Map<String, List<List<Integer>>> totalMachineTransTimeMap) {
        this.totalMachineTransTimeMap = totalMachineTransTimeMap;
    }


    public IloCplex getModel() {
        return model;
    }

    public void setModel(IloCplex model) {
        this.model = model;
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

    public List<TaskMix> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TaskMix> taskList) {
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

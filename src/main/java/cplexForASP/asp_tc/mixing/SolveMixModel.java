package cplexForASP.asp_tc.mixing;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolveMixModel {
    private List<TaskMix> taskList;
    private List<String> machineList;
    private double[][] taskTransTime;
    private Integer k;
    private IloCplex model;
    private IloNumVar varIJK[][];//任务i与j是否是k机器上紧前紧后任务
    private IloNumVar varMachineStartTask[];//任务i是否是机器k上开始任务
    private IloNumVar varMachineEndTask[];//任务i是否是机器k上结束任务
    private IloNumVar varTaskOnMachine[];//任务i是否是机器k上任务
    private IloNumVar varStartTime[];//开始加工时间
    private IloNumVar varEndTime[];//完成加工时间
    private Integer MaxFinishTime;
    private List<List<TaskMix>> taskGroupList;

    private Map<String, Map<Integer, List<TaskMix>>> taskSequenceListMap;


    public void initialParameter(String inputData, String dateStart) {
        GetInputDataMix getInputDataMix = new GetInputDataMix();
        getInputDataMix.initialData(inputData, dateStart);
        taskList = getInputDataMix.getTaskList();
        machineList = getInputDataMix.getMachineList();
        taskTransTime = getInputDataMix.getTaskTransTime();
        MaxFinishTime = 12000;//TODO 暂定为一周
        taskGroupList = new ArrayList<>();
        taskSequenceListMap = getInputDataMix.getTaskSequenceListMap();
    }


    public void modelBuild() throws IloException {
        model = new IloCplex();
        varIJK = new IloNumVar[taskList.size()][taskList.size()];
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                varIJK[i][j] = model.boolVar();
                varIJK[i][j].setName("varIJK_"+i+"_"+j);
            }
        }

        varMachineStartTask = new IloNumVar[taskList.size()];//任务i是否是机器k上开始任务
        varMachineEndTask = new IloNumVar[taskList.size()];//任务i是否是机器k上结束任务
        for (int i = 0; i < taskList.size(); i++) {
            varMachineStartTask[i] = model.boolVar();
            varMachineStartTask[i].setName("varStartTask_"+i);
            varMachineEndTask[i] = model.boolVar();
            varMachineEndTask[i].setName("varEndTask_"+i);
        }


        varStartTime = new IloNumVar[taskList.size()];//开始加工时间
        varEndTime = new IloNumVar[taskList.size()];//完成加工时间
        for (int i = 0; i < taskList.size(); i++) {
//            varStartTime[i] = model.numVar(taskList.get(i).getStandByMinute(), taskList.get(i).getDueMinute());
//            varEndTime[i] = model.numVar(taskList.get(i).getStandByMinute(), taskList.get(i).getDueMinute());
            varStartTime[i] = model.numVar(0,MaxFinishTime);
            varEndTime[i] = model.numVar(0,MaxFinishTime);
            varStartTime[i].setName("varStart_"+i);
            varEndTime[i].setName("varEnd_"+i);
        }


        //每个任务仅有一个紧前任务，或者它是某个机器上的开始任务
        for (int j = 0; j < taskList.size(); j++) {
            IloNumExpr tem = model.linearNumExpr();
            for (int i = 0; i < taskList.size(); i++) {
                tem = model.sum(tem, varIJK[i][j]);
            }
            tem = model.sum(tem, varMachineStartTask[j]);
            model.addEq(tem, 1).setName("only before"+"_"+j);
        }

        //每个任务仅有一个紧后任务，或者它是某个机器上的结束任务
        for (int i = 0; i < taskList.size(); i++) {
            IloNumExpr tem = model.linearNumExpr();
            for (int j = 0; j < taskList.size(); j++) {
                    tem = model.sum(tem, varIJK[i][j]);
                }
            tem = model.sum(tem, varMachineEndTask[i]);
            model.addEq(tem, 1).setName("only after"+"_"+i);
        }

        //每个机器上最多仅有一个开始任务和结束任务
        IloNumExpr temStart = model.linearNumExpr();
        IloNumExpr temEnd = model.linearNumExpr();
        for (int i = 0; i < taskList.size(); i++) {
            temStart = model.sum(temStart, varMachineStartTask[i]);
            temEnd = model.sum(temEnd, varMachineEndTask[i]);
        }
        model.addEq(temStart, 1).setName("only firstTask");
        model.addEq(temEnd, 1).setName("only lastTask");


        //任一个任务自己不能紧前/紧后本身
        for (int i = 0; i < taskList.size(); i++) {
            model.addEq(varIJK[i][i], 0).setName("no myself"+"_"+i);
        }


        //任务i的开始结束时间关系
        for (int i = 0; i < taskList.size(); i++) {
            double workTime = taskList.get(i).getMachineWorkTimeList().get(0);
            model.addLe(model.sum(varStartTime[i], workTime), varEndTime[i]).setName("star to end_"+i);
        }


        //每个任务的开始时间<=紧前任务的结束时间+切换时间
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                if(i!=j )
                    model.addLe(model.diff(model.sum(varEndTime[i], taskTransTime[i][j]), model.prod(model.diff(1, varIJK[i][j]), MaxFinishTime)), varStartTime[j]).setName("before to next_"+i);
            }
        }
//        model.addEq(varMachineStartTask[taskList.size()-1],1);

        //任务最早开始时间,最晚结束时间
        IloNumExpr ob=model.linearNumExpr();
        IloNumVar yx[]=new IloNumVar[taskList.size()];
        for (int i = 0; i < taskList.size(); i++) {
            yx[i]=model.boolVar();
//                model.addGe(varStartTime[i], model.diff(taskList.get(i).getStandByMinute(),model.prod(80,yx[i])));
////            model.addLe(varEndTime[i], model.sum(taskList.get(i).getDueMinute(),model.prod(yx[i],MaxFinishTime))).setName("endTimeLimit_"+i);
//            model.addLe(varEndTime[i], model.sum(taskList.get(i).getDueMinute(),model.prod(45,yx[i]))).setName("endTimeLimit_"+i);
            model.addGe(varStartTime[i],taskList.get(i).getStandByMinute());
            model.addLe(varEndTime[i],taskList.get(i).getDueMinute());
            ob=model.sum(ob,yx[i]);
        }

        //总时长定义
        IloNumVar makespan = model.numVar(0, MaxFinishTime);
        for (int i = 0; i < taskList.size(); i++) {
//            model.addGe(makespan, varEndTime[i]);
        }


        //任务是否能切换
//        for (int i = 0; i < taskList.size(); i++) {
//            for (int j = 0; j < taskList.size(); j++) {
//                if (taskList.get(i).getDueMinute()<=taskList.get(j).getStandByMinute())
//                {
//                    model.addEq(varIJK[j][i],0);
//                    model.addGe(varStartTime[j],varEndTime[i]);
//                    if (taskList.get(i).getDueMinute()<=taskList.get(j).getStandByMinute())
//                    {
//                        model.addEq(varIJK[i][j],0);
//                    }
//                }
//
//            }
//            if (taskList.get(i).getStandByMinute()>0)//开始不是为0的肯定不是开始任务
//            {
//                model.addEq(varMachineStartTask[i],0);
//            }
//            if (taskList.get(i).getDueMinute()<=4000)//结束太早的任务肯定不是结束任务
//            {
//                model.addEq(varMachineEndTask[i],0);
//            }
//        }

        for (int i = 0; i < taskList.size()-1; i++) {
//            model.addEq(varIJK[i][i+1],1);
        }


//
        //区域的任务集合成group
        List<TaskMix> taskGroup1 = new ArrayList<>();
        List<TaskMix> taskGroup2 = new ArrayList<>();
        for (TaskMix taskMix : taskList) {
            if (taskMix.getSection().equals(5)) {
                taskGroup1.add(taskMix);
            }
            if (taskMix.getSection().equals(6)) {
                taskGroup2.add(taskMix);
            }
        }
        taskGroupList.add(taskGroup1);
        taskGroupList.add(taskGroup2);
//
//
//        //料体颜色的group
        List<List<List<TaskMix>>> taskColorGroupList = new ArrayList<>();
        Map<Integer, Map<String, List<TaskMix>>> taskMixGroupMap = new HashMap<>();
        List<String> colorList = new ArrayList<>();
        colorList.add("水冰料");
        colorList.add("白料");
        colorList.add("黑料-树莓汁");
        colorList.add("黑料-抹茶");
        colorList.add("黑料-可可粉");
        colorList.add("黑料-榛子酱");
        colorList.add("黑料-咖啡酱");
        for (TaskMix taskMix : taskList) {
            Integer section = taskMix.getSection();
            String skuColor = taskMix.getSkuColor();
            if (taskMixGroupMap.containsKey(section)) {
                Map<String, List<TaskMix>> stringListMap = taskMixGroupMap.get(section);
                if (stringListMap.containsKey(skuColor)) {
                    List<TaskMix> taskMixes = stringListMap.get(skuColor);
                    taskMixes.add(taskMix);
                    stringListMap.put(skuColor, taskMixes);
                } else {
                    List<TaskMix> taskMixes = new ArrayList<>();
                    taskMixes.add(taskMix);
                    stringListMap.put(skuColor, taskMixes);
                }
                taskMixGroupMap.put(section, stringListMap);
            } else {
                Map<String, List<TaskMix>> stringListMap = new HashMap<>();
                List<TaskMix> temList = new ArrayList<>();
                temList.add(taskMix);
                stringListMap.put(skuColor, temList);
                taskMixGroupMap.put(section, stringListMap);
            }
        }
//
//        System.out.println(taskMixGroupMap);
        for (Map.Entry<Integer, Map<String, List<TaskMix>>> integerMapEntry : taskMixGroupMap.entrySet()) {
            Integer section = integerMapEntry.getKey();
            Map<String, List<TaskMix>> value = integerMapEntry.getValue();
            List<List<TaskMix>> taskColorMixList = new ArrayList<>();
            List<String> temColorList=new ArrayList<>();
            for (Map.Entry<String, List<TaskMix>> stringListEntry : value.entrySet()) {
                String key = stringListEntry.getKey();
                int i = colorList.indexOf(key);
                List<TaskMix> value1 = stringListEntry.getValue();
                taskGroupList.add(value1);
                temColorList.add(key);
                taskColorMixList.add(value1);
            }
            List<List<TaskMix>> taskColorMixOrderList = new ArrayList<>();
            for (String s : colorList) {
                if (temColorList.contains(s))
                {
                    int i = temColorList.indexOf(s);
                    taskColorMixOrderList.add(taskColorMixList.get(i));
                }
            }
            taskColorGroupList.add(taskColorMixOrderList);
        }


//        for (String s : colorList) {
//            List<TaskMix> taskGroup=new ArrayList<>();
//            for (TaskMix taskMix : taskList) {
//                if (taskMix.getSkuColor().equals(s))
//                {
//                    taskGroup.add(taskMix);
//                }
//            }
//            taskGroupList.add(taskGroup);
//            taskColorGroupList.add(taskGroup);
//        }
        //  group2约束:该group可以拆分到多个机器，但在同一个机器上必须相邻
        IloNumExpr obj=model.linearNumExpr();
        for (int g = 0; g < taskGroupList.size(); g++) {
//            System.out.println("----------");
//            for (TaskMix taskMix : taskGroup) {
//                System.out.println(taskMix.getTaskId());
//            }
//            System.out.println("----------");
            IloNumExpr groupTrans = model.linearNumExpr();
            List<TaskMix> taskGroup = taskGroupList.get(g);
            for (TaskMix task1 : taskGroup) {
                int i = taskList.indexOf(task1);
                for (TaskMix task2 : taskGroup) {
                    int j = taskList.indexOf(task2);
                    groupTrans = model.sum(groupTrans, varIJK[i][j]);
                }
            }
            if (g<2) {
                obj = model.sum(obj, model.prod(model.diff(taskGroup.size() - 1, groupTrans), 10000));
            }else
            {
                obj = model.sum(obj, model.prod(model.diff(taskGroup.size() - 1, groupTrans), 10));
            }
        }

//
//        //料体颜色的优先级
        for (int i = 0; i < taskColorGroupList.size(); i++) {
            List<List<TaskMix>> taskColorList = taskColorGroupList.get(i);
            System.out.println(taskColorList);
            for (int j = 0; j < taskColorList.size(); j++) {
                List<TaskMix> taskBeforeList = taskColorList.get(j);
                List<TaskMix> taskAfterList = new ArrayList<>();
                if (taskBeforeList != null && taskBeforeList.size() > 0) {
                    for (int m = j + 1; m < taskColorList.size(); m++) {
                        List<TaskMix> tasks = taskColorList.get(m);
                        if (tasks != null && tasks.size() > 0) {
                            taskAfterList.addAll(tasks);
                        }
                    }
                    if (taskAfterList != null && taskAfterList.size() > 0) {
                        for (TaskMix task : taskBeforeList) {
                            System.out.println("start:" + task.getTaskSKU());
                        }
                        for (TaskMix task : taskAfterList) {
                            System.out.println("end:" + task.getTaskSKU());
                        }
                        IloNumVar maxBeforeEnd = model.numVar(0, MaxFinishTime);
                        IloNumVar minAfterStart = model.numVar(0, MaxFinishTime);
                        for (TaskMix task : taskBeforeList) {
                            int t1 = taskList.indexOf(task);
                            model.addGe(maxBeforeEnd, varEndTime[t1]);
                        }
                        for (TaskMix task : taskAfterList) {
                            int t2 = taskList.indexOf(task);
                            model.addLe(minAfterStart, varStartTime[t2]);
                        }
//                        model.addLe(maxBeforeEnd, minAfterStart);
                        obj=model.sum(obj,model.prod(model.diff(maxBeforeEnd,minAfterStart),10));

                    }
                }
            }
        }
//
//
//        //开始任务开始时间为0 TODO 非强制规则
//        for (TaskMix task : taskList) {
//            int i = taskList.indexOf(task);
////                model.addLe(varStartTime[i], model.prod(model.diff(1, varMachineStartTask[i]), MaxFinishTime)).setName("first as 0_"+i);
//        }

        //总时长约束
//        model.addLe(makespan,MaxFinishTime);


        //定义切换最小1
//        IloNumExpr obj = model.linearNumExpr();
//        for (int i = 0; i < taskList.size(); i++) {
//            for (int j = 0; j < taskList.size(); j++) {
//                for (int k = 0; k < machineList.size(); k++) {
//                    if(taskTransTime[i][j][k]!=-1) {
//                        obj = model.sum(obj, model.prod(varIJK[i][j][k], taskTransTime[i][j][k]));
//                    }
//                }
//            }
//        }

//        for (int i = 0; i < taskList.size(); i++) {
//            for (int j = 0; j < taskList.size(); j++) {
//                IloNumVar y = model.numVar(0, 14400);
//                IloNumExpr tem = model.linearNumExpr();
//                    tem = model.sum(tem, varIJK[i][j]);
////                model.addGe(y, model.diff(model.diff(varStartTime[j], varEndTime[i]), model.prod(MaxFinishTime, model.diff(1, tem))));
////                obj = model.sum(y, obj);
//            }
//        }

//        model.addMinimize(model.sum(model.prod(obj, 10), model.prod(makespan, 10)));

        //目标1：最小化（总时长）
        model.addMinimize(model.sum(obj,model.prod(ob,10000)));


    }



    //初始解读取
    public void addMipStart() throws IloException {
        IloNumVar [] varList=new IloNumVar[taskList.size()*taskList.size()+2*taskList.size()];
        double[] valueList=new double[taskList.size()*taskList.size()+2*taskList.size()];
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                varList[i*taskList.size()+j]=varIJK[i][j];
                if (j==i+1)
                {
                    valueList[i*taskList.size()+j]=1;
                }
                else
                {
                    valueList[i*taskList.size()+j]=0;
                }
            }
        }
        for (int i = 0; i < taskList.size(); i++) {
            varList[taskList.size()*taskList.size()+i]=varEndTime[i];
            valueList[taskList.size()*taskList.size()+i]=taskList.get(i).getEndWorkTime();
        }
        for (int i = 0; i < taskList.size(); i++) {
            varList[taskList.size()*taskList.size()+taskList.size()+i]=varStartTime[i];
            valueList[taskList.size()*taskList.size()+taskList.size()+i]=taskList.get(i).getStartWorkTime();
        }
        model.addMIPStart(varList,valueList);
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

    public double[][] getTaskTransTime() {
        return taskTransTime;
    }

    public void setTaskTransTime(double[][] taskTransTime) {
        this.taskTransTime = taskTransTime;
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

    public IloNumVar[] getVarEndTime() {
        return varEndTime;
    }

    public Integer getK() {
        return k;
    }

    public IloNumVar[][] getVarIJK() {
        return varIJK;
    }

    public IloNumVar[] getVarMachineStartTask() {
        return varMachineStartTask;
    }

    public IloNumVar[] getVarMachineEndTask() {
        return varMachineEndTask;
    }

    public IloNumVar[] getVarTaskOnMachine() {
        return varTaskOnMachine;
    }

    public Integer getMaxFinishTime() {
        return MaxFinishTime;
    }

    public List<List<TaskMix>> getTaskGroupList() {
        return taskGroupList;
    }

    public Map<String, Map<Integer, List<TaskMix>>> getTaskSequenceListMap() {
        return taskSequenceListMap;
    }
}

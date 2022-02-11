package cplexForASP.asp_tc.packing;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolveModel {
    private List<Task> taskList;
    private List<String> machineList;
    private int[][][] taskTransTime;
    private IloCplex model;
    private IloNumVar varIJK[][][];//任务i与j是否是k机器上紧前紧后任务
    private IloNumVar varMachineStartTask[][];//任务i是否是机器k上开始任务
    private IloNumVar varMachineEndTask[][];//任务i是否是机器k上结束任务
    private IloNumVar varTaskOnMachine[][];//任务i是否是机器k上任务
    private IloNumVar varStartTime[];//开始加工时间
    private IloNumVar varEndTime[];//完成加工时间
    private Integer MaxFinishTime;
    private List<List<Task>> taskGroupList;
    private List<Group> groupList;
    private Map<String, Map<Integer, List<Task>>> taskSequenceListMap;


    public void initialParameter(String inputData, String dateStart) {
        GetInputData getInputData = new GetInputData();
        getInputData.initialData(inputData, dateStart);
        taskList = getInputData.getTaskList();
        machineList = getInputData.getMachineList();
        taskTransTime = getInputData.getTaskTransTime();
        MaxFinishTime = 20160;//TODO 暂定为一周
        taskGroupList = new ArrayList<>();
        taskSequenceListMap = getInputData.getTaskSequenceListMap();
        groupList = getInputData.getGroupList();
        //TODO 暂时认为固定两个group
        List<Task> temTaskList1 = new ArrayList<>();
        List<Task> temTaskList2 = new ArrayList<>();
        List<Task> temTaskList3 = new ArrayList<>();
//        temTaskList1.add(taskList.get(0));
//        temTaskList1.add(taskList.get(1));
//        temTaskList1.add(taskList.get(2));
//        temTaskList1.add(taskList.get(3));
//        temTaskList2.add(taskList.get(4));
//        temTaskList2.add(taskList.get(5));
//        temTaskList2.add(taskList.get(6));
//        temTaskList2.add(taskList.get(7));
//        temTaskList2.add(taskList.get(8));
//        temTaskList2.add(taskList.get(9));
//        temTaskList3.add(taskList.get(10));
//        temTaskList3.add(taskList.get(11));
//        temTaskList2.add(taskList.get(7));
//        temTaskList2.add(taskList.get(8));
//        temTaskList2.add(taskList.get(9));
//        temTaskList2.add(taskList.get(10));
        taskGroupList.add(temTaskList1);
        taskGroupList.add(temTaskList2);
    }


    public void modelBuild() throws IloException {
        model = new IloCplex();
        varIJK = new IloNumVar[taskList.size()][taskList.size()][machineList.size()];
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                for (int k = 0; k < machineList.size(); k++) {
                    varIJK[i][j][k] = model.boolVar();
                }
            }
        }

        varMachineStartTask = new IloNumVar[taskList.size()][machineList.size()];//任务i是否是机器k上开始任务
        varMachineEndTask = new IloNumVar[taskList.size()][machineList.size()];//任务i是否是机器k上结束任务
        varTaskOnMachine = new IloNumVar[taskList.size()][machineList.size()];//任务i是否是机器k上任务
        for (int i = 0; i < taskList.size(); i++) {
            for (int k = 0; k < machineList.size(); k++) {
                varMachineStartTask[i][k] = model.boolVar();
                varMachineEndTask[i][k] = model.boolVar();
                varTaskOnMachine[i][k] = model.boolVar();
            }
        }


        varStartTime = new IloNumVar[taskList.size()];//开始加工时间
        varEndTime = new IloNumVar[taskList.size()];//完成加工时间
        for (int i = 0; i < taskList.size(); i++) {
            varStartTime[i] = model.numVar(0, MaxFinishTime);
            varEndTime[i] = model.numVar(0,MaxFinishTime);
        }


        //每个任务仅有一个紧前任务，或者它是某个机器上的开始任务
        for (int j = 0; j < taskList.size(); j++) {
            IloNumExpr tem = model.linearNumExpr();
            for (int k = 0; k < machineList.size(); k++) {
                for (int i = 0; i < taskList.size(); i++) {
                    tem = model.sum(tem, varIJK[i][j][k]);
                }
                tem = model.sum(tem, varMachineStartTask[j][k]);
            }
            model.addEq(tem, 1);
        }

        //每个任务仅有一个紧后任务，或者它是某个机器上的结束任务
        for (int i = 0; i < taskList.size(); i++) {
            IloNumExpr tem = model.linearNumExpr();
            for (int k = 0; k < machineList.size(); k++) {
                for (int j = 0; j < taskList.size(); j++) {
                    tem = model.sum(tem, varIJK[i][j][k]);
                }
                tem = model.sum(tem, varMachineEndTask[i][k]);
            }
            model.addEq(tem, 1);
        }

        //每个机器上最多仅有一个开始任务和结束任务
        for (int k = 0; k < machineList.size(); k++) {
            IloNumExpr temStart = model.linearNumExpr();
            IloNumExpr temEnd = model.linearNumExpr();
            for (int i = 0; i < taskList.size(); i++) {
                temStart = model.sum(temStart, varMachineStartTask[i][k]);
                temEnd = model.sum(temEnd, varMachineEndTask[i][k]);
            }
            model.addLe(temStart, 1);
            model.addLe(temEnd, 1);
        }

        //任一个任务自己不能紧前/紧后本身
        for (int i = 0; i < taskList.size(); i++) {
            for (int k = 0; k < machineList.size(); k++) {
                model.addEq(varIJK[i][i][k], 0);
            }
        }


        //任一个任务只能在一个机器上生产
        for (int t = 0; t < taskList.size(); t++) {
            for (int k = 0; k < machineList.size(); k++) {
                IloNumExpr tem1 = model.linearNumExpr();
                IloNumExpr tem2 = model.linearNumExpr();
                for (int i = 0; i < taskList.size(); i++) {
                    tem1 = model.sum(tem1, varIJK[i][t][k]);
                    tem2 = model.sum(tem2, varIJK[t][i][k]);
                }
                tem1 = model.sum(tem1, varMachineStartTask[t][k]);
                tem2 = model.sum(tem2, varMachineEndTask[t][k]);
                model.addEq(tem1, tem2);
            }
        }

        //每个任务只能在部分机器上生产
        for (Task task : taskList) {
            int i = taskList.indexOf(task);
            List<Double> machineProductivityList = task.getMachineProductivityList();
            for (int k = 0; k < machineList.size(); k++) {
                Double producivity = machineProductivityList.get(k);
//                System.out.println(i+":"+k);
                if (producivity == -1) {
//                    System.out.println(i + ":" + k);
                    for (int j = 0; j < taskList.size(); j++) {
                        model.addEq(varIJK[i][j][k], 0);
                        model.addEq(varIJK[j][i][k], 0);
                    }
                    model.addEq(varMachineStartTask[i][k], 0);
                    model.addEq(varMachineEndTask[i][k], 0);
                }
            }
        }

        //任务i是否在机器k上生产
        for (int i = 0; i < taskList.size(); i++) {
            for (int k = 0; k < machineList.size(); k++) {
                IloNumExpr tem = model.linearNumExpr();
                for (int j = 0; j < taskList.size(); j++) {
                    tem = model.sum(tem, varIJK[i][j][k]);
                }
                tem = model.sum(tem, varMachineEndTask[i][k]);
                model.addEq(varTaskOnMachine[i][k], tem);
            }
        }

        //任务i的开始结束时间关系
        for (int i = 0; i < taskList.size(); i++) {
            IloNumExpr temWorkTime = model.linearNumExpr();
            for (int k = 0; k < machineList.size(); k++) {
                Double workTime = taskList.get(i).getMachineWorkTimeList().get(k);
                temWorkTime = model.sum(temWorkTime, model.prod(workTime, varTaskOnMachine[i][k]));
            }
            model.addEq(model.sum(varStartTime[i], temWorkTime), varEndTime[i]);
        }


        //每个任务的开始时间<=紧前任务的结束时间+切换时间
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                IloNumExpr tem = model.linearNumExpr();
                IloNumExpr tem2 = model.linearNumExpr();
                for (int k = 0; k < machineList.size(); k++) {
                    tem = model.sum(tem, model.prod(varIJK[i][j][k], taskTransTime[i][j][k]));
                    tem2 = model.sum(tem2, varIJK[i][j][k]);
                }
                model.addLe(model.diff(model.sum(varEndTime[i], tem), model.prod(model.diff(1, tem2), MaxFinishTime)), varStartTime[j]);
            }
        }

        //任务最早开始时间,最晚结束时间
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println(taskList.get(i).getDueMinute());

//            model.addGe(varStartTime[i], taskList.get(i).getStandByMinute());
            model.addLe(varEndTime[i], taskList.get(i).getDueMinute());
        }

        //总时长定义
        IloNumVar makespan = model.numVar(0, MaxFinishTime);
        for (int i = 0; i < taskList.size(); i++) {
            model.addGe(makespan, varEndTime[i]);
        }


        //group1约束:该group必须在同一个机器
//        for (List<ast_tc.packing.Task> taskGroup : taskGroupList) {
//            IloNumExpr groupTrans=model.linearNumExpr();
//            for (ast_tc.packing.Task task1 : taskGroup) {
//                int i = taskList.indexOf(task1);
//                for (ast_tc.packing.Task task2 : taskGroup) {
//                    int j = taskList.indexOf(task2);
//                    for (int k = 0; k < machineList.size(); k++) {
//                        groupTrans=model.sum(groupTrans,varIJK[i][j][k]);
//                    }
//                }
//            }
//            model.addEq(groupTrans,taskGroup.size()-1);
//        }


        //group2约束:该group可以拆分到多个机器，但在同一个机器上必须相邻
//        for (List<ast_tc.packing.Task> taskGroup : taskGroupList) {
//            for (int k = 0; k < machineList.size(); k++) {
//                IloNumExpr groupTrans = model.linearNumExpr();
//                IloNumExpr groupTransMachine = model.linearNumExpr();
//                for (ast_tc.packing.Task task1 : taskGroup) {
//                    int i = taskList.indexOf(task1);
//                    for (ast_tc.packing.Task task2 : taskGroup) {
//                        int j = taskList.indexOf(task2);
//                        groupTrans = model.sum(groupTrans, varIJK[i][j][k]);
//                    }
//                    groupTransMachine = model.sum(groupTransMachine, varTaskOnMachine[i][k]);
//                }
//                model.addGe(groupTrans, model.diff(groupTransMachine, 1));
//            }
//        }



        //当任务a和任务b在同一个机器时，任务a必须必任务b早
        for (Map.Entry<String, Map<Integer, List<Task>>> stringMapEntry : taskSequenceListMap.entrySet()) {
            String machine = stringMapEntry.getKey();
            int k = machineList.indexOf(machine);

            Map<Integer, List<Task>> taskSequenceList = stringMapEntry.getValue();
            for (int i = 1; i < 7; i++) {
                List<Task> taskBeforeList = taskSequenceList.get(i);
                List<Task> taskAfterList = new ArrayList<>();
                if (taskBeforeList != null && taskBeforeList.size() > 0) {
                    for (int j = i + 1; j < 7; j++) {
                        List<Task> tasks = taskSequenceList.get(j);
                        if (tasks != null && tasks.size() > 0) {
                            taskAfterList.addAll(tasks);
                        }
                    }
                    if (taskAfterList != null && taskAfterList.size() > 0) {
//                        for (Task task : taskBeforeList) {
//                            System.out.println(machine+"start:"+task.getTaskSKU());
//                        }
//                        for (Task task : taskAfterList) {
//                            System.out.println(machine+"end:"+task.getTaskSKU());
//                        }

                        IloNumVar maxBeforeEnd = model.numVar(0, MaxFinishTime);
                        IloNumVar minAfterStart = model.numVar(0, MaxFinishTime);
                        for (Task task : taskBeforeList) {
                            int t1 = taskList.indexOf(task);
                            model.addGe(maxBeforeEnd, model.diff(varEndTime[t1], model.prod(MaxFinishTime, model.diff(1, varTaskOnMachine[t1][k]))));
                        }
                        for (Task task : taskAfterList) {
                            int t2 = taskList.indexOf(task);
                            model.addLe(minAfterStart, model.sum(varStartTime[t2], model.prod(MaxFinishTime, model.diff(1, varTaskOnMachine[t2][k]))));
                        }
                        model.addLe(maxBeforeEnd, minAfterStart);
                    }
                }
            }
        }


        //针对特定机器，有相应Group规则
        for (Group group : groupList) {
            String machine = group.getMachine();
            int k = machineList.indexOf(machine);
            List<Task> groupTaskList = group.getGroupTaskList();
//            System.out.println();
            for (Task task : groupTaskList) {
//                System.out.print(task.getTaskSKU());
            }
            IloNumExpr groupTrans = model.linearNumExpr();
            IloNumExpr groupTransMachine = model.linearNumExpr();
            for (Task task1 : groupTaskList) {
                int i = taskList.indexOf(task1);
                for (Task task2 : groupTaskList) {
                    int j = taskList.indexOf(task2);
                    groupTrans = model.sum(groupTrans, varIJK[i][j][k]);
                }
                groupTransMachine = model.sum(groupTransMachine, varTaskOnMachine[i][k]);
            }
            model.addGe(groupTrans, model.diff(groupTransMachine, 1));
        }


        //开始任务开始时间为0 TODO 非强制规则
        for (Task task : taskList) {
            int i = taskList.indexOf(task);
            for (int k = 0; k < machineList.size(); k++) {
                model.addLe(varStartTime[i],model.prod(model.diff(1,varMachineStartTask[i][k]),14400));
            }
        }

        //总时长约束
//        model.addLe(makespan,MaxFinishTime);


        //定义切换最小1
        IloNumExpr obj = model.linearNumExpr();
        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                for (int k = 0; k < machineList.size(); k++) {
                    if(taskTransTime[i][j][k]!=-1) {
                        obj = model.sum(obj, model.prod(varIJK[i][j][k], taskTransTime[i][j][k]));
                    }
                }
            }
        }

        for (int i = 0; i < taskList.size(); i++) {
            for (int j = 0; j < taskList.size(); j++) {
                IloNumVar y = model.numVar(0, 14400);
                IloNumExpr tem = model.linearNumExpr();
                for (int k = 0; k < machineList.size(); k++) {
                    tem = model.sum(tem, varIJK[i][j][k]);
                }
                model.addGe(y, model.diff(model.diff(varStartTime[j], varEndTime[i]), model.prod(MaxFinishTime, model.diff(1, tem))));
                obj = model.sum(y, obj);
            }
        }

        model.addMinimize(model.sum(model.prod(obj, 10), model.prod(makespan,1000)));

        //目标1：最小化（总时长）
//        model.addMinimize(makespan);


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

    public int[][][] getTaskTransTime() {
        return taskTransTime;
    }

    public void setTaskTransTime(int[][][] taskTransTime) {
        this.taskTransTime = taskTransTime;
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

    public IloNumVar[][] getVarMachineStartTask() {
        return varMachineStartTask;
    }

    public IloNumVar[][] getVarMachineEndTask() {
        return varMachineEndTask;
    }

    public IloNumVar[][] getVarTaskOnMachine() {
        return varTaskOnMachine;
    }

    public IloNumVar[] getVarStartTime() {
        return varStartTime;
    }

    public IloNumVar[] getVarEndTime() {
        return varEndTime;
    }
}

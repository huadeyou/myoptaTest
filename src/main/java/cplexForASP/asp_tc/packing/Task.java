package cplexForASP.asp_tc.packing;

import java.util.Date;
import java.util.List;

public class Task {
    private Integer taskId;
    private String taskSKU;
    private Integer demandCases;
    private List<Double> machineProductivityList;
    private List<Double> machineWorkTimeList;
    private List<Integer> machineHumanList;
    private Date dueDay;
    private Integer dueMinute;
    private Integer standByMinute;
    private String appointMachine;
    private int startWorkTime;
    private int endWorkTime;
    private Integer section;
    private String skuColor;
    private Double tons;


    public String getTaskSKU() {
        return taskSKU;
    }

    public void setTaskSKU(String taskSKU) {
        this.taskSKU = taskSKU;
    }

    public Integer getDemandCases() {
        return demandCases;
    }

    public void setDemandCases(Integer demandCases) {
        this.demandCases = demandCases;
    }

    public List<Double> getMachineProductivityList() {
        return machineProductivityList;
    }

    public void setMachineProductivityList(List<Double> machineProductivityList) {
        this.machineProductivityList = machineProductivityList;
    }

    public List<Integer> getMachineHumanList() {
        return machineHumanList;
    }

    public void setMachineHumanList(List<Integer> machineHumanList) {
        this.machineHumanList = machineHumanList;
    }

    public Date getDueDay() {
        return dueDay;
    }

    public void setDueDay(Date dueDay) {
        this.dueDay = dueDay;
    }

    @Override
    public String toString() {
        return "ast_tc.packing.Task{" +
                "taskId=" + taskId +
                ", taskSKU='" + taskSKU + '\'' +
                ", demandCases=" + demandCases +
                ", machineProductivityList=" + machineProductivityList +
                ", machineWorkTimeList=" + machineWorkTimeList +
                ", machineHumanList=" + machineHumanList +
                ", dueDay='" + dueDay + '\'' +
                ", dueMinute=" + dueMinute +
                ", appointMachine='" + appointMachine + '\'' +
                ", startWorkTime=" + startWorkTime +
                ", endWorkTime=" + endWorkTime +
                '}';
    }

    public List<Double> getMachineWorkTimeList() {
        return machineWorkTimeList;
    }

    public void setMachineWorkTimeList(List<Double> machineWorkTimeList) {
        this.machineWorkTimeList = machineWorkTimeList;
    }

    public String getAppointMachine() {
        return appointMachine;
    }

    public void setAppointMachine(String appointMachine) {
        this.appointMachine = appointMachine;
    }

    public int getStartWorkTime() {
        return startWorkTime;
    }

    public void setStartWorkTime(int startWorkTime) {
        this.startWorkTime = startWorkTime;
    }

    public int getEndWorkTime() {
        return endWorkTime;
    }

    public void setEndWorkTime(int endWorkTime) {
        this.endWorkTime = endWorkTime;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getDueMinute() {
        return dueMinute;
    }

    public void setDueMinute(Integer dueMinute) {
        this.dueMinute = dueMinute;
    }

    public Integer getStandByMinute() {
        return standByMinute;
    }

    public void setStandByMinute(Integer standByMinute) {
        this.standByMinute = standByMinute;
    }


    public Integer getSection() {
        return section;
    }

    public void setSection(Integer section) {
        this.section = section;
    }

    public String getSkuColor() {
        return skuColor;
    }

    public void setSkuColor(String skuColor) {
        this.skuColor = skuColor;
    }

    public Double getTons() {
        return tons;
    }

    public void setTons(Double tons) {
        this.tons = tons;
    }
}

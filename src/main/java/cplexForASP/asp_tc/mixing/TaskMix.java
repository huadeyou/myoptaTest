package cplexForASP.asp_tc.mixing;

import java.util.Date;
import java.util.List;

public class TaskMix {
    private Integer taskId;
    private String taskSKU;
    private String productSku;
    private Double demandTons;
    private List<Double> machineProductivityList;
    private List<Double> machineWorkTimeList;
    private Date dueDay;
    private double dueMinute;
    private double standByMinute;
    private String appointMachine;
    private double startWorkTime;
    private double endWorkTime;
    private Integer section;
    private String skuColor;



    public String getTaskSKU() {
        return taskSKU;
    }

    public void setTaskSKU(String taskSKU) {
        this.taskSKU = taskSKU;
    }


    public List<Double> getMachineProductivityList() {
        return machineProductivityList;
    }

    public void setMachineProductivityList(List<Double> machineProductivityList) {
        this.machineProductivityList = machineProductivityList;
    }



    public Date getDueDay() {
        return dueDay;
    }

    public void setDueDay(Date dueDay) {
        this.dueDay = dueDay;
    }

    @Override
    public String toString() {
        return "TaskMix{" +
                "taskId=" + taskId +
                ", taskSKU='" + taskSKU + '\'' +
                '}';
    }


    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Double getDemandTons() {
        return demandTons;
    }

    public void setDemandTons(Double demandTons) {
        this.demandTons = demandTons;
    }

    public List<Double> getMachineWorkTimeList() {
        return machineWorkTimeList;
    }

    public void setMachineWorkTimeList(List<Double> machineWorkTimeList) {
        this.machineWorkTimeList = machineWorkTimeList;
    }

    public double getDueMinute() {
        return dueMinute;
    }

    public void setDueMinute(double dueMinute) {
        this.dueMinute = dueMinute;
    }

    public double getStandByMinute() {
        return standByMinute;
    }

    public void setStandByMinute(double standByMinute) {
        this.standByMinute = standByMinute;
    }

    public String getAppointMachine() {
        return appointMachine;
    }

    public void setAppointMachine(String appointMachine) {
        this.appointMachine = appointMachine;
    }

    public double getStartWorkTime() {
        return startWorkTime;
    }

    public void setStartWorkTime(double startWorkTime) {
        this.startWorkTime = startWorkTime;
    }

    public double getEndWorkTime() {
        return endWorkTime;
    }

    public void setEndWorkTime(double endWorkTime) {
        this.endWorkTime = endWorkTime;
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
}

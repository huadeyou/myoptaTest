package cloudBalance;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * .@author HuaDeyou
 * .@date
 */
@PlanningEntity
public class Process extends AbstractPersistable{

    private Integer memoryDemand;

    private Integer cpuDemand;

    private Integer netWidthDemand;

    private Computer computer;

    public Integer getMemoryDemand() {
        return memoryDemand;
    }

    public void setMemoryDemand(Integer memoryDemand) {
        this.memoryDemand = memoryDemand;
    }

    public Integer getCpuDemand() {
        return cpuDemand;
    }

    public void setCpuDemand(Integer cpuDemand) {
        this.cpuDemand = cpuDemand;
    }

    public Integer getNetWidthDemand() {
        return netWidthDemand;
    }

    public void setNetWidthDemand(Integer netWidthDemand) {
        this.netWidthDemand = netWidthDemand;
    }

    @PlanningVariable(valueRangeProviderRefs = {"computerRange"},nullable = false)
    public Computer getComputer() {
        return computer;
    }

    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    @Override
    public String toString() {
        return "Process{" +
                "memoryDemand=" + memoryDemand +
                ", cpuDemand=" + cpuDemand +
                ", netWidthDemand=" + netWidthDemand +
                ", computer=" + computer +
                '}';
    }
}

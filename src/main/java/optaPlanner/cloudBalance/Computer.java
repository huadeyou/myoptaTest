package optaPlanner.cloudBalance;

/**
 * .@author HuaDeyou
 * .@date
 */

public class Computer extends AbstractPersistable{
    private Integer totalMemory;

    private Integer totalCpu;

    private Integer totalNetWidth;

    public Integer getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Integer totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Integer getTotalCpu() {
        return totalCpu;
    }

    public void setTotalCpu(Integer totalCpu) {
        this.totalCpu = totalCpu;
    }

    public Integer getTotalNetWidth() {
        return totalNetWidth;
    }

    public void setTotalNetWidth(Integer totalNetWidth) {
        this.totalNetWidth = totalNetWidth;
    }

    @Override
    public String toString() {
        return "Computer{" +
                "totalMemory=" + totalMemory +
                ", totalCpu=" + totalCpu +
                ", totalNetWidth=" + totalNetWidth +
                '}';
    }
}

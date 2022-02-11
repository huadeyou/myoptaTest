package optaPlanner.cloudBalance;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.IncrementalScoreCalculator;

import java.util.HashMap;
import java.util.Map;

/**
 * .@author HuaDeyou
 * .@date
 */
public class CloudBalanceCalculator implements IncrementalScoreCalculator<CloudBalance, HardSoftScore> {
    private Map<Computer, Integer> cpuPowerUsageMap;
    private Map<Computer, Integer> memoryUsageMap;
    private Map<Computer, Integer> networkBandwidthUsageMap;
    private Map<Computer, Integer> processCountMap;

    private int hardScore;
    private int softScore;

    @Override
    public void resetWorkingSolution(CloudBalance cloudBalance) {
        int computerListSize = cloudBalance.getComputerList().size();
        cpuPowerUsageMap = new HashMap<>(computerListSize);
        memoryUsageMap = new HashMap<>(computerListSize);
        networkBandwidthUsageMap = new HashMap<>(computerListSize);
        processCountMap = new HashMap<>(computerListSize);
        for (Computer computer : cloudBalance.getComputerList()) {
            cpuPowerUsageMap.put(computer, 0);
            memoryUsageMap.put(computer, 0);
            networkBandwidthUsageMap.put(computer, 0);
            processCountMap.put(computer, 0);
        }
        hardScore = 0;
        softScore = 0;
        for (Process process : cloudBalance.getProcessList()) {
            insert(process);
        }
    }

    @Override
    public void beforeEntityAdded(Object o) {

    }

    @Override
    public void afterEntityAdded(Object o) {

    }

    @Override
    public void beforeVariableChanged(Object o, String s) {
        retract((Process)o);
    }

    @Override
    public void afterVariableChanged(Object o, String s) {
        insert((Process)o);
    }

    @Override
    public void beforeEntityRemoved(Object o) {
        retract((Process)o);

    }

    @Override
    public void afterEntityRemoved(Object o) {
        insert((Process)o);
    }

    @Override
    public HardSoftScore calculateScore() {
        return HardSoftScore.of(hardScore, softScore);
    }


    private void insert(Process process) {
        Computer computer = process.getComputer();
        if (computer != null) {
            int cpuPower = computer.getTotalCpu();
            int oldCpuPowerUsage = cpuPowerUsageMap.get(computer);
            int oldCpuPowerAvailable = cpuPower - oldCpuPowerUsage;
            int newCpuPowerUsage = oldCpuPowerUsage + process.getCpuDemand();
            int newCpuPowerAvailable = cpuPower - newCpuPowerUsage;
            hardScore += Math.min(newCpuPowerAvailable, 0) - Math.min(oldCpuPowerAvailable, 0);
            cpuPowerUsageMap.put(computer, newCpuPowerUsage);

            int memory = computer.getTotalMemory();
            int oldMemoryUsage = memoryUsageMap.get(computer);
            int oldMemoryAvailable = memory - oldMemoryUsage;
            int newMemoryUsage = oldMemoryUsage + process.getMemoryDemand();
            int newMemoryAvailable = memory - newMemoryUsage;
            hardScore += Math.min(newMemoryAvailable, 0) - Math.min(oldMemoryAvailable, 0);
            memoryUsageMap.put(computer, newMemoryUsage);

            int networkBandwidth = computer.getTotalNetWidth();
            int oldNetworkBandwidthUsage = networkBandwidthUsageMap.get(computer);
            int oldNetworkBandwidthAvailable = networkBandwidth - oldNetworkBandwidthUsage;
            int newNetworkBandwidthUsage = oldNetworkBandwidthUsage + process.getNetWidthDemand();
            int newNetworkBandwidthAvailable = networkBandwidth - newNetworkBandwidthUsage;
            hardScore += Math.min(newNetworkBandwidthAvailable, 0) - Math.min(oldNetworkBandwidthAvailable, 0);
            networkBandwidthUsageMap.put(computer, newNetworkBandwidthUsage);

            int oldProcessCount = processCountMap.get(computer);
            int newProcessCount = oldProcessCount + 1;
            processCountMap.put(computer, newProcessCount);
        }
    }

    private void retract(Process process) {
        Computer computer = process.getComputer();
        if (computer != null) {
            int cpuPower = computer.getTotalCpu();
            int oldCpuPowerUsage = cpuPowerUsageMap.get(computer);
            int oldCpuPowerAvailable = cpuPower - oldCpuPowerUsage;
            int newCpuPowerUsage = oldCpuPowerUsage - process.getCpuDemand();
            int newCpuPowerAvailable = cpuPower - newCpuPowerUsage;
            hardScore += Math.min(newCpuPowerAvailable, 0) - Math.min(oldCpuPowerAvailable, 0);
            cpuPowerUsageMap.put(computer, newCpuPowerUsage);

            int memory = computer.getTotalMemory();
            int oldMemoryUsage = memoryUsageMap.get(computer);
            int oldMemoryAvailable = memory - oldMemoryUsage;
            int newMemoryUsage = oldMemoryUsage - process.getMemoryDemand();
            int newMemoryAvailable = memory - newMemoryUsage;
            hardScore += Math.min(newMemoryAvailable, 0) - Math.min(oldMemoryAvailable, 0);
            memoryUsageMap.put(computer, newMemoryUsage);

            int networkBandwidth = computer.getTotalNetWidth();
            int oldNetworkBandwidthUsage = networkBandwidthUsageMap.get(computer);
            int oldNetworkBandwidthAvailable = networkBandwidth - oldNetworkBandwidthUsage;
            int newNetworkBandwidthUsage = oldNetworkBandwidthUsage - process.getNetWidthDemand();
            int newNetworkBandwidthAvailable = networkBandwidth - newNetworkBandwidthUsage;
            hardScore += Math.min(newNetworkBandwidthAvailable, 0) - Math.min(oldNetworkBandwidthAvailable, 0);
            networkBandwidthUsageMap.put(computer, newNetworkBandwidthUsage);

            int oldProcessCount = processCountMap.get(computer);
            int newProcessCount = oldProcessCount - 1;
            processCountMap.put(computer, newProcessCount);
        }
    }
}

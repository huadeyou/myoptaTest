package optaPlanner.cloudBalance;

import java.util.ArrayList;
import java.util.List;

/**
 * .@author HuaDeyou
 * .@date
 */
public class CloudBalanceGenerator {

    public  CloudBalance createCloudBalance()
    {
        CloudBalance cloudBalance=new CloudBalance();
        cloudBalance.setComputerList(initialComputerList(2));
        cloudBalance.setProcessList(initialProcessList(5,cloudBalance.getComputerList().get(0)));
        return  cloudBalance;

    }

    public List<Computer> initialComputerList(int size)
    {
        List<Computer> computerList=new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Computer computer=new Computer();
            computer.setId((long) i);
            computer.setTotalCpu((int) (Math.random()*16));
            computer.setTotalMemory((int) (Math.random()*64));
            computer.setTotalNetWidth((int) (Math.random()*1024));
            computerList.add(computer);
        }
        return computerList;
    }

    public List<Process> initialProcessList(int size,Computer defaultComputer)
    {
        List<Process> processList=new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Process process=new Process();
            process.setId((long) i);
            process.setCpuDemand((int) (Math.random()*4));
            process.setMemoryDemand((int) (Math.random()*8));
            process.setNetWidthDemand((int) (Math.random()*100));
            process.setComputer(defaultComputer);
            processList.add(process);
        }
        return processList;
    }

}

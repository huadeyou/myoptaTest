package cplexForASP.asp_tj.packing;

import java.util.List;

/**
 * .@author HuaDeyou
 * .@date 0901
 */
public class Group {
    private int groupId;
    private String machine;
    private List<Task> groupTaskList;



    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public List<Task> getGroupTaskList() {
        return groupTaskList;
    }

    public void setGroupTaskList(List<Task> groupTaskList) {
        this.groupTaskList = groupTaskList;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }
}

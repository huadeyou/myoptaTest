package heuristicAlgorithm.GA.GAforSimpleScheduling;

import java.util.ArrayList;
import java.util.List;

/**
 * .@author HuaDeyou
 * .@date
 */
public class MyGaForOneStaff {

    private List<Integer> workHourList=new ArrayList<>();
    private List<Integer> workDayList=new ArrayList<>();
    private Integer totalShift=10;
    private Integer totalDay=7;
    private Integer totalKinds =1000;
    private List<List<Integer>> allShiftTable=new ArrayList<>();
    private double rCross=0.6;
    private double rEvaluation=0.9;
    private int allowMinWorkDay=5;
    private int allowMaxWorkDay=5;
    private int allowMinWorkHour=40;
    private int allowMaxWorkHour=40;
    private int totalIter=1000;

    private  int bestScore=Integer.MAX_VALUE;
    private  int bestShift=0;
    private List<Integer> bestShiftList=new ArrayList<>();



    public void getBestWorkShiftList()
    {

        this.initialShiftStatus();
        this.initialAgentShiftList();
        for (int i = 0; i < totalIter; i++) {
            if (bestScore==0)
            {
                System.exit(0);
            }
            this.choose();
            this.cross();
            this.mutation();
            System.out.println("iter:"+i);
            System.out.println("best shiftList:"+bestShiftList);
            System.out.println("best score:"+bestScore);
            System.out.println("--------------------------------");
        }
        System.out.println("best shiftList:"+bestShiftList);
        System.out.println("best score:"+bestScore);

    }


    /**
     * 初始化各班次属性
     */
    public  void initialShiftStatus()
    {
        for (Integer s = 0; s < totalShift; s++) {
            if (s==0)
            {
                workDayList.add(0);
                workHourList.add(0);
            }
            else
            {
                workDayList.add(1);
//                workHourList.add((int) (Math.random()*3+6));
                workHourList.add((int) (s%2+7));
            }
        }
    }


    /**
     * 初始化种群
     */
    public void initialAgentShiftList()
    {
        for (Integer p = 0; p < totalKinds; p++) {
            List<Integer> dayShiftList=new ArrayList<>();
            for (Integer d = 0; d < totalDay; d++) {
                dayShiftList.add((int) (Math.random()*totalShift));
            }
            allShiftTable.add(dayShiftList);
        }
    }


    /**
     * 计算一条染色体质量，分数越高质量越差
     */
    public int caculateShiftScore(List<Integer> dayShiftList)
    {
        int sumWday=0;
        int sumWorkHour=0;
        int conWork=0;
        int conRest=0;
        int cost=0;
        for (int d = 0; d < dayShiftList.size(); d++) {
            if (workDayList.get(dayShiftList.get(d))==1)
            {
                conWork++;
                conRest=0;
            }
            else {
                conRest++;
                conWork=0;
            }
            if (conWork>3)
            {
                cost+=(conWork-3)*10;
            }
            if (conRest>1)
            {
                cost+=(conRest-1)*10;
            }
            sumWday+=workDayList.get(dayShiftList.get(d));
            sumWorkHour+=workHourList.get(dayShiftList.get(d));

        }

        if (sumWday>allowMaxWorkDay)
        {
            cost+=(sumWday-allowMaxWorkDay)*100;
        }
        if (sumWday<allowMinWorkDay)
        {
            cost+=(allowMinWorkDay-sumWday)*100;
        }
        if (sumWorkHour>allowMaxWorkHour)
        {
            cost+=(sumWorkHour-allowMaxWorkHour)*50;
        }
        if (sumWorkHour<allowMinWorkHour)
        {
            cost+=(allowMinWorkHour-sumWorkHour)*50;
        }



        return cost;
    }


    /**
     * 选择,
     * 本质上是按规则去除一些染色体
     */
    public void choose()
    {
        int totalScore=0;
        List<Integer> scoreList=new ArrayList<>();
        for (int i = 0; i < allShiftTable.size(); i++) {
            List<Integer> integers = allShiftTable.get(i);
            int currentScore = caculateShiftScore(integers);
            scoreList.add(currentScore);
            totalScore+=currentScore;
            if (currentScore<bestScore)
            {
                bestScore=currentScore;
                bestShift=i;
                bestShiftList=new ArrayList<>();
                for (Integer integer : allShiftTable.get(i)) {
                    bestShiftList.add(integer);
                }

            }
        }
        List<Double> rateList=new ArrayList<>();
        for (int i = 0; i < allShiftTable.size(); i++) {
            double pi=(double)scoreList.get(i)/totalScore;
            rateList.add(pi);
        }

        for (int i = 0; i < allShiftTable.size(); i++) {
            double r= Math.random();
            if (r<rateList.get(0))
            {
//                allShiftTable.set(i,allShiftTable.get(0));
                if (i!=0) {
                    allShiftTable.get(i).clear();
                    allShiftTable.get(i).addAll(allShiftTable.get(0));
                }
            }
            else
            {
                double sumRate=0;
                for (int j = 1; j < allShiftTable.size(); j++) {
                    sumRate+=rateList.get(j);
                    if (sumRate>r)
                    {
                        break;
                    }
//                    allShiftTable.set(i,allShiftTable.get(j));
                    if (i!=j) {
                        allShiftTable.get(i).clear();
                        allShiftTable.get(i).addAll(allShiftTable.get(j));
                    }
                }
            }
        }

        List<String> collectList=new ArrayList<>();
        for (int i = 0; i < allShiftTable.size(); i++) {
            String str="";
            for (int p = 0; p < allShiftTable.get(i).size(); p++) {
                str=str+allShiftTable.get(i).get(p).toString();
            }
            if (!collectList.contains(str))
            {
                collectList.add(str);
            }
        }
        System.out.println("current kinds:"+collectList.size());
        if (collectList.size()==1)
        {
//            System.exit(0);
        }
    }


    /**
     * 交叉
     */
    public void cross()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            double r = Math.random();
            if (r<rCross)
            {
                int pos = (int)(Math.random()*totalDay)+1;     //pos位点前后二进制串交叉
                List<Integer> integers1 = allShiftTable.get(i);
                List<Integer> integers2 = allShiftTable.get(((i + 1)% totalKinds));
                for (int n=0;n<pos;n++)
                {
                    int t=integers1.get(n);
                    System.out.println("corss:"+n);
                    integers1.set(n,integers2.get(n));
                    integers2.set(n,t);
                }

            }
        }
    }


    /**
     * 变异
     */
    public void mutation()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            double r=Math.random();
            if (r<rEvaluation)
            {
                int pos = (int)(Math.random()*totalDay);
                allShiftTable.get(i).set(pos,(int )(Math.random()*totalShift));
            }
        }
    }

}

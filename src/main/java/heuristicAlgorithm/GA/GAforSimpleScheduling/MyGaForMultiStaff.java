package heuristicAlgorithm.GA.GAforSimpleScheduling;

import java.util.ArrayList;
import java.util.List;

/**
 * .@author HuaDeyou
 * .@date
 */
public class MyGaForMultiStaff {

    private List<Integer> workHourList=new ArrayList<>();
    private List<Integer> workDayList=new ArrayList<>();
    private Integer totalShift=10;
    private Integer totalDay=7;
    private Integer totalPerson=10;
    private Integer totalKinds=100;
    private List<List<List<Integer>>> allShiftTable=new ArrayList<>();
    private double rCross=0.7;
    private double rEvaluation=0.1;
    private int allowMinWorkDay=5;
    private int allowMaxWorkDay=5;
    private int allowMinWorkHour=40;
    private int allowMaxWorkHour=40;
    private int totalIter=10000;

    private  int bestScore=Integer.MAX_VALUE;
    private  int bestShift=0;
    private List<List<Integer>> bestShiftList=new ArrayList<>();



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
//            this.crossInLocal();
//            this.cross();
            this.crossPerson();
//            this.mutationInLocal();
//            this.mutation();
            this.mutationPerson();
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
        for (Integer k = 0; k < totalKinds; k++) {
            List<List<Integer>> theAgentShiftList = new ArrayList<>();
            for (Integer p = 0; p < totalPerson; p++) {
                List<Integer> dayShiftList = new ArrayList<>();
                for (Integer d = 0; d < totalDay; d++) {
                    dayShiftList.add((int) (Math.random() * totalShift));
                }
                theAgentShiftList.add(dayShiftList);
            }
            allShiftTable.add(theAgentShiftList);
        }
    }


    /**
     * 计算一条染色体质量，分数越高质量越差
     */
    public int caculateShiftScore(List<List<Integer>> dayShiftList)
    {
        int cost=0;
        for (int p = 0; p < dayShiftList.size(); p++) {
            int sumWday=0;
            int sumWorkHour=0;
            int conWork=0;
            int conRest=0;
            for (int d = 0; d < dayShiftList.get(p).size(); d++) {
                if (workDayList.get(dayShiftList.get(p).get(d)) == 1) {
                    conWork++;
                    conRest = 0;
                } else {
                    conRest++;
                    conWork = 0;
                }
                if (conWork > 3) {
                    cost += (conWork - 3) * 10;
                }
                if (conRest > 1) {
                    cost += (conRest - 1) * 10;
                }
                sumWday += workDayList.get(dayShiftList.get(p).get(d));
                sumWorkHour += workHourList.get(dayShiftList.get(p).get(d));

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
                cost+=(sumWorkHour-allowMaxWorkHour)*50000;
            }
            if (sumWorkHour<allowMinWorkHour)
            {
                cost+=(allowMinWorkHour-sumWorkHour)*50000;
            }
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
            List<List<Integer>> theAgentShiftList = allShiftTable.get(i);
            int currentScore = caculateShiftScore(theAgentShiftList);
            scoreList.add(currentScore);
            totalScore+=currentScore;
            if (currentScore<bestScore)
            {
                bestScore=currentScore;
                bestShift=i;
                List<List<Integer>> bestList = allShiftTable.get(bestShift);
                bestShiftList=new ArrayList<>();
                for (int m = 0; m < bestList.size(); m++) {
                    List<Integer> temList = new ArrayList<>(bestList.get(m));
                    bestShiftList.add(temList);
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
                if (i!=0) {
                    allShiftTable.get(i).clear();
                    List<List<Integer>> lists = allShiftTable.get(0);
                    List<List<Integer>> temLists=new ArrayList<>();
                    for (int p = 0; p < lists.size(); p++) {
                        List<Integer> tList = new ArrayList<>(lists.get(p));
                        temLists.add(tList);
                    }

                    allShiftTable.get(i).addAll(temLists);
                }
            }
            else
            {
                double sumRate=0;
                for (int j = 0; j < allShiftTable.size(); j++) {
                    sumRate+=rateList.get(j);
                    if (sumRate>r)
                    {
                        break;
                    }
                    if (i!=j) {
                        allShiftTable.get(i).clear();
                        List<List<Integer>> lists = allShiftTable.get(j);
                        List<List<Integer>> temLists=new ArrayList<>();
                        for (int p = 0; p < lists.size(); p++) {
                            List<Integer> tList = new ArrayList<>(lists.get(p));
                            temLists.add(tList);
                        }
                        allShiftTable.get(i).addAll(temLists);
                    }
                }
            }
        }

        List<String> collectList=new ArrayList<>();
        for (int i = 0; i < allShiftTable.size(); i++) {
            StringBuilder str= new StringBuilder();
            for (int p = 0; p < allShiftTable.get(i).size(); p++) {
                for (int d = 0; d < allShiftTable.get(i).get(p).size(); d++) {
                    str.append(allShiftTable.get(i).get(p).get(d).toString());
                }
            }
            if (!collectList.contains(str.toString()))
            {
                collectList.add(str.toString());
            }
        }
        System.out.println("current kinds:"+collectList.size());
        if (collectList.size()==1)
        {
//            System.exit(0);
        }
    }



    /**
     * 选择,
     * 本质上是按规则去除一些染色体
     */
    public void chooseInLocal()
    {
        int totalScore=0;
        List<Integer> scoreList=new ArrayList<>();
        for (int i = 0; i < allShiftTable.size(); i++) {
            List<List<Integer>> theAgentShiftList = allShiftTable.get(i);
            int currentScore = caculateShiftScore(theAgentShiftList);
            scoreList.add(currentScore);
            totalScore+=currentScore;
            if (currentScore<bestScore)
            {
                bestScore=currentScore;
                bestShift=i;
                List<List<Integer>> bestList = allShiftTable.get(bestShift);
                bestShiftList=new ArrayList<>();
                for (int m = 0; m < bestList.size(); m++) {
                    List<Integer> temList=new ArrayList<>();
                    for (Integer integer : bestList.get(m)) {
                        temList.add(integer);
                    }
                    bestShiftList.add(temList);
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
            if (r<rateList.get(i))
            {
                allShiftTable.set(i,allShiftTable.get(0));
            }
            else
            {
                double sumRate=0;
                for (int j = 0; j < allShiftTable.size(); j++) {
                    sumRate+=rateList.get(j);
                    if (sumRate>r)
                    {
                        break;
                    }
                    allShiftTable.set(i,allShiftTable.get(j));
                }
            }
        }
    }


    /**
     * 交叉
     */
    public void cross()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            double r = Math.random();
            if (r < rCross) {
                int pos = (int) (Math.random() * totalDay * totalPerson) + 1;     //pos位点前后二进制串交叉
                List<List<Integer>> integers1 = allShiftTable.get(i);
                List<List<Integer>> integers2 = allShiftTable.get(((i + 1) % totalKinds));
                for (int n = 0; n < pos; n++) {
                    int p = n / totalDay;
                    int d = n % totalDay;
                    int t = integers1.get(p).get(d);
//                    System.out.println("cross:"+i+"-"+p+"-"+d);
                    integers1.get(p).set(d, integers2.get(p).get(d));
                    integers2.get(p).set(d, t);
                }
            }
        }
    }

    /**
     * 交叉定人呢
     */
    public void crossPerson()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            double r = Math.random();
            if (r < rCross) {
                int p=(int) (Math.random()  * totalPerson) ;
                int pos = (int) (Math.random() * totalDay ) + 1;     //pos位点前后二进制串交叉
                List<List<Integer>> integers1 = allShiftTable.get(i);
                List<List<Integer>> integers2 = allShiftTable.get(((i + 1) % totalKinds));
                for (int n = 0; n < pos; n++) {
                    int d = n % totalDay;
                    int t = integers1.get(p).get(d);
//                    System.out.println("cross:"+i+"-"+p+"-"+d);
                    integers1.get(p).set(d, integers2.get(p).get(d));
                    integers2.get(p).set(d, t);
                }
            }
        }
    }


    /**
     * 交叉
     */
    public void crossInLocal()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            for (int p = 0; p < allShiftTable.get(i).size(); p++) {
                double r = Math.random();
                if (r < rCross) {
                    int pos = (int) (Math.random() * totalDay ) + 1;     //pos位点前后二进制串交叉
                    List<Integer> integers1 = allShiftTable.get(i).get(p);
                    List<Integer> integers2 = allShiftTable.get(i).get((p + 1)%totalPerson);
                    for (int n = 0; n < pos; n++) {
                        int t = integers1.get(n);
                        integers1.set(n, integers2.get(n));
                        integers2.set(n, t);
                    }

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
                int pos = (int)(Math.random()*totalDay*totalPerson);
                int p=pos/totalDay;
                int d=pos%totalDay;
//                System.out.println("fix:"+p+"-"+d);
                allShiftTable.get(i).get(p).set(d,(int )(Math.random()*totalShift));
            }
        }
    }

    /**
     * 变异
     */
    public void mutationPerson()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            double r=Math.random();
            if (r<rEvaluation)
            {
                for(int m=0;m<10;m++) {
                    int p = (int) (Math.random() * totalPerson);
                    int d = (int) (Math.random() * totalDay);
//                System.out.println("fix:"+p+"-"+d);
                    allShiftTable.get(i).get(p).set(d, (int) (Math.random() * totalShift));
                }
            }
        }
    }

    /**
     * 变异
     */
    public void mutationInLocal()
    {
        for (int i = 0; i < allShiftTable.size(); i++) {
            for (int p = 0; p < allShiftTable.get(i).size(); p++) {
                double r = Math.random();
                if (r < rEvaluation) {
                    int d = (int) (Math.random() * totalDay );
                    allShiftTable.get(i).get(p).set(d, (int) (Math.random() * totalShift));
                }
            }
        }
    }

}

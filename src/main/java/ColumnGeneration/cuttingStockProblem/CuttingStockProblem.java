package ColumnGeneration.cuttingStockProblem;

import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

public class CuttingStockProblem {

    static double RC_EPS = 1.0e-6;
    int orignLength=17;
    int productKinds=3;
    int [] productLength=new int[productKinds];
    int [] productDemand=new int[productKinds];
    List<CutPattern> totalUsefulPatternList;
    IloCplex mpModel;
    IloRange[] fillDemand;
    IloObjective minObj;




    public static void main(String[] args) throws IloException {
        CuttingStockProblem cuttingStockProblem=new CuttingStockProblem();
        cuttingStockProblem.initialProduct();
        double[] duals = cuttingStockProblem.initialMasterProblem();
        do {
            for (int i = 0; i < duals.length; i++) {
                System.out.println("dual_"+i+":"+duals[i]);
            }
            CutPattern cutPattern = cuttingStockProblem.solveSubProblem(duals);
            if (cutPattern != null) {
                duals = cuttingStockProblem.solveMasterProblem(cutPattern);
            }
            else
            {
                duals=null;
            }
        }while (duals!=null);



    }


    public void initialProduct()
    {
        productLength[0]=3;
        productLength[1]=5;
        productLength[2]=9;
        productDemand[0]=25;
        productDemand[1]=20;
        productDemand[2]=15;
    }


    public double[] initialMasterProblem() throws IloException {
        mpModel=new IloCplex();
        fillDemand=new IloRange[productKinds];
        minObj= mpModel.addMinimize();
        List<IloNumVar> iloNumVarList=new ArrayList<>();
        for (int i = 0; i < productKinds; i++) {
            fillDemand[i]=mpModel.addRange(productDemand[i],Double.MAX_VALUE);
            double a=orignLength/ productLength[i];
            System.out.println(a);
            IloColumn col=mpModel.column(minObj,1).and(mpModel.column(fillDemand[i],orignLength/productLength[i]));
            mpModel.numVar(col, 0, Double.MAX_VALUE);
        }
        mpModel.exportModel("modelMp.lp");
        if (mpModel.solve())
        {
            double[] duals =new double[3];
            for (int i = 0; i < fillDemand.length; i++) {
                duals[i] = mpModel.getDual(fillDemand[i]);
            }
            return duals;

        }
        return null;
    }


    public double[]  solveMasterProblem(CutPattern cutPattern) throws IloException {
        if (cutPattern!=null)
        {
            double[] targetNum = cutPattern.getTargetNum();
            IloColumn newCol=mpModel.column(minObj, 1);
            for (int i = 0; i < productKinds; i++) {
                newCol = newCol.and(mpModel.column(fillDemand[i],targetNum[i]));
            }
            IloNumVar iloNumVar=mpModel.numVar(newCol,0,Double.MAX_VALUE);
        }
        mpModel.exportModel("modelMp.lp");
        if (mpModel.solve())
        {
            double[] duals =new double[3];
            for (int i = 0; i < fillDemand.length; i++) {
                duals[i] = mpModel.getDual(fillDemand[i]);
                System.out.println("dual_"+i+":"+duals[i]);
            }
            return duals;

        }
        return null;
    }


    /**
     * 求解子问题
     */
    public CutPattern solveSubProblem(double[] duals) throws IloException {
        IloCplex subModel=new IloCplex();
        IloObjective objective=subModel.addMinimize();
        IloLinearNumExpr tem=subModel.linearNumExpr();
        IloLinearNumExpr obj=subModel.linearNumExpr();
        IloNumVar y[]=new IloNumVar[duals.length];
        for (int j = 0; j < duals.length; j++) {
            y[j]=subModel.intVar(0,Integer.MAX_VALUE);
            tem.addTerm(productLength[j],y[j]);
            obj.addTerm(y[j],duals[j]);
        }
        objective.setExpr(subModel.diff(1,obj));
        subModel.addLe(tem,orignLength);
//        subModel.exportModel("submodel.lp");
        if (subModel.solve()) {
            if (subModel.getObjValue() > -RC_EPS)
            {
                return  null;
            }
            double objValue = subModel.getObjValue();
            if (objValue<=-RC_EPS) {
                double[] target = new double[duals.length];
                for (int j = 0; j < duals.length; j++) {
                    target[j] = subModel.getValue(y[j]);
                    System.out.println("target_:"+j+":"+target[j]);
                }
                CutPattern cutPattern=new CutPattern();
                cutPattern.setTargetNum(target);

                return cutPattern;
            }
            else
            {
                return  null;
            }

        }
        else
        {
            return null;
        }
    }




    public class CutPattern
    {
        double[] targetNum;

        public double[] getTargetNum() {
            return targetNum;
        }

        public void setTargetNum(double[] targetNum) {
            this.targetNum = targetNum;
        }
    }




}
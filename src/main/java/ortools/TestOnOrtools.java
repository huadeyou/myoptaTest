package ortools;

import com.google.ortools.linearsolver.*;

/**
 * .@author HuaDeyou
 * .@date
 */
public class TestOnOrtools {


    /**
     * 执行前需先配置VM OPTIONS=-Djava.library.path=D:\ortools
     * @param args
     */
    public static void main(String[] args) {

        System.loadLibrary("jniortools");
//        MPSolver mpSolver=MPSolver.createSolver("GLPK");
        MPSolver mpSolver=new MPSolver("test", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);

        MPVariable x1 = mpSolver.makeVar(0, 10.5, true, "x1");
        MPVariable x2 = mpSolver.makeVar(0.5, 10, true, "x2");

        MPConstraint ilorange1 = mpSolver.makeConstraint();
        ilorange1.setCoefficient(x1,1);
        ilorange1.setCoefficient(x2,1);



        MPConstraint ilorange2= mpSolver.makeConstraint();
        ilorange2.setCoefficient(x1,1);
        ilorange2.setCoefficient(x2,1);
        ilorange2.setUb(10);

        MPObjective mpObjective=mpSolver.objective();
        mpObjective.setCoefficient(x1,3);
        mpObjective.setCoefficient(x2,4);
        mpObjective.setMinimization();




        MPSolver.ResultStatus solve = mpSolver.solve();

        System.out.println("obj:"+mpObjective.value());
        System.out.println("x1:"+x1.solutionValue());
        System.out.println("x2:"+x2.solutionValue());
        System.out.println("x1_dual:"+ilorange1.dualValue());
        System.out.println("x2_dual:"+ilorange2.dualValue());
        System.out.println("x1_cost:"+x1.reducedCost());
        System.out.println("x2_cost:"+x2.reducedCost());

    }


}

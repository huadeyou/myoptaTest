package optaPlanner.bagProblem;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * .@author HuaDeyou
 * .@date
 */
public class solveBagProblem {
    public static void main(String[] args) {
        SolverFactory<BagLoadProblem> solverFactory=SolverFactory.createFromXmlResource("optaPlanner/bagSolverConfig.xml");
        Solver<BagLoadProblem> bagLoadProblemSolver = solverFactory.buildSolver();
        BagLoadProblem bagLoadProblem=new BagLoadProblem();
        createProblem(bagLoadProblem);
        BagLoadProblem solve = bagLoadProblemSolver.solve(bagLoadProblem);
        SimpleScore simpleScore = solve.getSimpleScore();
        System.out.println(simpleScore.getScore());
        getSol(solve);
    }


    public static  void createProblem(BagLoadProblem bagLoadProblem)
    {


        List<Goods> totalGoods=new ArrayList<>();
        for (int i=0;i<10;i++)
        {
            Goods goods=new Goods();
            goods.setId((long) i);
            goods.setPicked(0);
            goods.setPrice((int) (Math.random()*15));
            goods.setWeight((int) (Math.random()*25));
            goods.setAvaliPickOptList(new ArrayList<>());
            goods.getAvaliPickOptList().add(0);
            goods.getAvaliPickOptList().add(1);
            totalGoods.add(goods);
        }
        bagLoadProblem.setGoodsList(totalGoods);
    }

    public static void getSol(BagLoadProblem bagLoadProblem)
    {
        List<Goods> goodsList = bagLoadProblem.getGoodsList();
        System.out.println("id"+"\t"+"weight"+"\t"+"price"+"\t"+"picked");
        for (Goods goods : goodsList) {
            System.out.println(goods.getId()+"\t"+goods.getWeight()+"\t"+goods.getPrice()+"\t"+goods.getPicked());
        }
    }

}

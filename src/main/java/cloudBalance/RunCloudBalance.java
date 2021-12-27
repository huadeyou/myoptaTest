package cloudBalance;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

/**
 * .@author HuaDeyou
 * .@date
 */
public class RunCloudBalance {

    public static void main(String[] args) {
        SolverFactory<CloudBalance> solverFactory=SolverFactory.createFromXmlResource("cloudBalanceConfig.xml");
        Solver<CloudBalance> solver=solverFactory.buildSolver();
        CloudBalance unsolverCloudBalance=new CloudBalanceGenerator().createCloudBalance();
        CloudBalance solvedCloudBalance = solver.solve(unsolverCloudBalance);
        System.out.println(solvedCloudBalance.toString());
    }
}

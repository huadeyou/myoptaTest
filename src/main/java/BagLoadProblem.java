import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

import java.util.List;

/**
 * .@author HuaDeyou
 * .@date
 */
@PlanningSolution
public class BagLoadProblem extends AbstractPersistable {

    private List<Goods> goodsList;
    private SimpleScore simpleScore;
    private int totalAllowWeight=50;

    @PlanningEntityCollectionProperty
    public List<Goods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<Goods> goodsList) {
        this.goodsList = goodsList;
    }

    @PlanningScore
    public SimpleScore getSimpleScore() {
        return simpleScore;
    }

    public void setSimpleScore(SimpleScore simpleScore) {
        this.simpleScore = simpleScore;
    }

    public int getTotalAllowWeight() {
        return totalAllowWeight;
    }

    public void setTotalAllowWeight(int totalAllowWeight) {
        this.totalAllowWeight = totalAllowWeight;
    }
}

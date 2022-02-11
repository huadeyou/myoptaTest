package optaPlanner.bagProblem;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import java.util.List;

/**
 * .@author HuaDeyou
 * .@date
 */
public class BagLoadConstraint implements EasyScoreCalculator<BagLoadProblem, SimpleScore> {

    private HardSoftLongScore hardSoftLongScore;
    private HardSoftLongScore softLongScore;

    @Override
    public SimpleScore calculateScore(BagLoadProblem bagLoadProblem) {
        List<Goods> goodsList = bagLoadProblem.getGoodsList();
        int score=0;
        int totalWeight=0;
        int totalPrice=0;
        for (Goods goods : goodsList) {
            totalWeight+=goods.getPicked()*goods.getWeight();
            totalPrice+=goods.getPicked()*goods.getPrice();
        }

        if (totalWeight>bagLoadProblem.getTotalAllowWeight())
        {
            score=bagLoadProblem.getTotalAllowWeight()-totalWeight;
        }
        else {
            score = totalPrice;
        }
        return  SimpleScore.of(score);
    }

}

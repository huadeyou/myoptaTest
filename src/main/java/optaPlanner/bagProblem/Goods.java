package optaPlanner.bagProblem;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.List;

/**
 * .@author HuaDeyou
 * .@date 9-28
 */
@PlanningEntity
public class Goods extends  AbstractPersistable{

    private int weight;
    private int price;


//    @PlanningVariableReference(variableName = "pick")
    @PlanningVariable(valueRangeProviderRefs = {"pickList"})
    private Integer picked;
    private List<Integer> avaliPickOptList;
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Integer getPicked() {
        return picked;
    }

    public void setPicked(Integer picked) {
        this.picked = picked;
    }

    @ValueRangeProvider(id="pickList")
    public List<Integer> getAvaliPickOptList() {
        return avaliPickOptList;
    }

    public void setAvaliPickOptList(List<Integer> avaliPickOptList) {
        this.avaliPickOptList = avaliPickOptList;
    }
}

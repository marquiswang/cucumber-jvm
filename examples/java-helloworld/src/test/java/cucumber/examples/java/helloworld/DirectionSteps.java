package cucumber.examples.java.helloworld;

import cucumber.Delimiter;
import cucumber.annotation.en.And;
import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.formatter.JUnitFormatter;
import junit.framework.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DirectionSteps {
    private enum Direction {
        EAST, WEST, NORTH, SOUTH
    }

    private final Set<Direction> blocked = new HashSet<Direction>();
    private final Set<Direction> moveableDirections = new HashSet<Direction>();

    @Given("^there are walls to the ([A-Z]+(?:,[A-Z]+)*)$")
    public void there_are_walls_to_the(List<Direction> dirs) {
        blocked.addAll(dirs);
    }

    @And("^I look for directions to move$")
    public void I_look_for_directions_to_move() {
        for (Direction direction : Direction.values()) {
            if (!blocked.contains(direction)) {
               moveableDirections.add(direction);
            }
        }
    }

    @Then("^I will be able to move to the ([A-Z]+(?: and [A-Z]+)*)$")
    public void I_will_be_able_to_move_to_the(@Delimiter(" and ") List<Direction> directions) {
        for (Direction direction : directions) {
            Assert.assertTrue(moveableDirections.contains(direction));
        }
    }
}

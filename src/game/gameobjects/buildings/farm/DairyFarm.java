package game.gameobjects.buildings.farm;

import game.AssetManager;
import game.gameobjects.Building;
import game.gameobjects.GameObject;
import game.gameobjects.buildings.castle.Castle;
import game.gameobjects.buildings.castle.ResourceName;
import game.map.Map;
import game.map.Vector2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.concurrent.atomic.AtomicInteger;

public class DairyFarm extends Building {

    private final static int price = 10;
    //per second
    private final static int productionRate = 10;
    private DairyFarm(Vector2D location)
    {
        super("building-farm-2" , location);
        HBox toolbar = new HBox();
        Label label = new Label("Dairy Farm");
        label.setStyle("-fx-font-size: 50");
        toolbar.getChildren().add(label);
        toolbar.setAlignment(Pos.CENTER);

        super.setToolbar(toolbar);
    }


    public void produce()
    {
        if(Castle.getCurrentGranaryCapacity().get() + productionRate <= Castle.getMaxGranaryCapacity().get())
        {
            Castle.setResouce(ResourceName.CHEESE , productionRate);
            Castle.getCurrentGranaryCapacity().addAndGet(productionRate);
        }
        else
            Map.playSound(AssetManager.sounds.get("full-granary"));
    }


    public static GameObject create(Vector2D location)
    {
        if(Castle.resourceAmount(ResourceName.WOOD) - price >= 0 ) {
            Castle.setResouce(ResourceName.WOOD , -price);
            return new DairyFarm(location);
        }
        else {
            Map.playSound(AssetManager.sounds.get("need-wood"));
            return null;
        }
    }

    public static int getPrice() {
        return price;
    }
}

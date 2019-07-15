package game.map;

import game.GV;
import game.gameobjects.GameObject;
import game.gameobjects.buildings.Castle;
import game.gameobjects.buildings.Food.Granary;
import game.gameobjects.buildings.defense.Armory;
import game.gameobjects.buildings.industry.StockPile;
import game.map.components.DragComponent;
import game.ui.inGame.Toolbar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.Optional;

public class Map extends Application
{
    private String name;
    private int width;
    private int height;
    private int[][] tilesNumber;
    private transient Toolbar toolbar;
    private transient DragComponent dragComponent;
    private transient Tile[][] tiles;
    private transient java.util.Map<Integer,GameObject> gameObjects;
    private transient ScrollPane scrollPane;
    private transient BorderPane borderPane;
    private transient Pane pane;
    private transient Thread applyRules = new Thread(() -> {
        while (true)
        {
            Castle.caculate();
            Platform.runLater(() -> {
                toolbar.getLblGold().setText(Castle.getGold().get() + "");
                toolbar.getLblPopularity().setText(Castle.getPopularity().get() + "");
                toolbar.getLblPopulation().setText(Castle.getCurrentPopulation() +"/"+Castle.getMaxPopulationSize());
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    private transient Thread scrollOnMouseMove = new Thread(() -> {

        borderPane.addEventHandler(MouseEvent.ANY , event -> {
            GV.mousePosition.set(event.getX() , event.getY());
            if(event.getTarget() instanceof Tile)
            {
                int j = (int)((Tile) event.getTarget()).getCoordinate().getX();
                int i = (int)((Tile) event.getTarget()).getCoordinate().getY();
                double x = Math.abs(tiles[i][j].getX() - GV.tileSize.getX());
                double y = tiles[i][j].getY() - GV.tileSize.getY();
                GV.mapPos.set(x, y);
                if(Optional.ofNullable(toolbar.getCurrentGameObject()).isPresent() && toolbar.isWaitingToBePlaced())
                {
                    toolbar.getCurrentGameObject().setLayoutX(tiles[i][j].getX());
                    toolbar.getCurrentGameObject().setLayoutY(tiles[i][j].getY());
                }
                if(event.getButton() == MouseButton.SECONDARY && Optional.ofNullable(toolbar.getCurrentGameObject()).isPresent())
                {
                    if(toolbar.getCurrentGameObject() instanceof StockPile)
                        Castle.getMaxStockPileCapacity().addAndGet(-GV.stockPileCapacity);
                    if(toolbar.getCurrentGameObject() instanceof Granary)
                        Castle.getMaxGranaryCapacity().addAndGet(-GV.GranaryCapacity);
                    if(toolbar.getCurrentGameObject() instanceof Armory)
                        Castle.getMaxArmoryCapacity().addAndGet(-GV.armoryCapacity);
                    toolbar.setWaitingToBePlaced(false);
                    pane.getChildren().remove(toolbar.getCurrentGameObject());
                    toolbar.setCurrentGameObject(null);
                    toolbar.setCurrentIndex(-1);
                    toolbar.setCurrentKey("");
                }else if(event.getButton() == MouseButton.PRIMARY && Optional.ofNullable(toolbar.getCurrentGameObject()).isPresent())
                {

                    if(i % 2 == 0)
                    {
                        tiles[i][j].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                        tiles[i][j+1].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                        tiles[i+1][j].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                        tiles[i-1][j].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                    }else
                    {
                        tiles[i][j].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                        tiles[i][j+1].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                        tiles[i-1][j+1].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                        tiles[i+1][j+1].setPlacedGameobject(toolbar.getCurrentGameObject().getGameObjectHelper());
                    }
                    toolbar.getCurrentGameObject().setLayoutX(tiles[i][j].getX());
                    toolbar.getCurrentGameObject().setLayoutY(tiles[i][j].getY());
                    toolbar.setCurrentGameObject(null);

                }
            }
        });
        pane.addEventHandler(MouseEvent.MOUSE_MOVED , event -> {


        });
        while (true)
        {
            if(GV.mousePosition.getX() > Screen.getPrimary().getBounds().getWidth()-30)
                scrollPane.setHvalue(scrollPane.getHvalue()+ GV.scrollOffset);

            if(GV.mousePosition.getX() <15)
                scrollPane.setHvalue(scrollPane.getHvalue()-GV.scrollOffset);

            if(GV.mousePosition.getY() > Screen.getPrimary().getBounds().getHeight()-10 )
                scrollPane.setVvalue(scrollPane.getVvalue()+GV.scrollOffset*4);

            if (GV.mousePosition.getY() < 15)
                scrollPane.setVvalue(scrollPane.getVvalue()-GV.scrollOffset*4);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });
    public Map() { }
    //TODO fix this so you can load this from a file
    public Map(String name , int width , int height)
    {
        System.out.println("Map created");
        this.name = name;
        this.width = width;
        this.height = height;
        tilesNumber = new int[width][height];
        tiles = new Tile[width][height];
    }

    //TODO later make this so we can load map from a file
    public void loadMap()
    {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tilesNumber[i][j] = (int)(Math.random()*3);
            }
        }
    }
    public void loadMap(int[][] tilesNumber)
    {
        tiles = new Tile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //tiles[i][j] = tiles[i][j] = new Tile(TileType.valueOf(tilesNumber[i][j]).get(),new Vector2D(j,i));
            }
        }
    }



    public void initialize()
    {
        gameObjects = new LinkedHashMap<>();
        pane = new Pane();
        //dragComponent = new DragComponent(pane);
        HBox hBox;
        scrollPane = new ScrollPane(pane);
        toolbar = new Toolbar(pane , gameObjects);
        borderPane = new BorderPane();
        StackPane stackPane = new StackPane(scrollPane , toolbar);
        stackPane.setAlignment(Pos.BOTTOM_LEFT);
        borderPane.setCenter(stackPane);
        tiles = new Tile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double x = j * GV.tileSize.getX() + (i % 2 == 0 ?  0:GV.tileSize.getX()/2);
                double y = (i)*GV.tileSize.getY()/2;
                tiles[i][j] = new Tile(TileType.valueOf(0).get(),new Vector2D(j,i) , x , y );
                pane.getChildren().add(tiles[i][j]);
            }

        }
        System.out.println("hello");
        GameObject castle = Castle.createCastle(new Vector2D(500 , 500));
        if(Optional.ofNullable(castle).isPresent())
        {
            gameObjects.put(castle.getObjectId() , castle);
            castle.addEventHandler(MouseEvent.MOUSE_CLICKED , event -> {
                toolbar.getMainContent().getChildren().clear();
                toolbar.getMainContent().getChildren().add(castle.getToolbar());
            });
            GameObject stockPile = new StockPile(new Vector2D(600 , 600));
            stockPile.addEventHandler(MouseEvent.MOUSE_CLICKED , event -> {
                toolbar.getMainContent().getChildren().clear();
                toolbar.getMainContent().getChildren().add(stockPile.getToolbar());
            });
            pane.getChildren().add(castle);
            pane.getChildren().add(stockPile);

        }
        else
            System.out.println("gameobject is null");
        applyRules.start();
        handleEvents();
    }

    private void handleEvents()
    {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                event.consume();
            }
        });

        scrollOnMouseMove.start();
    }
    public BorderPane getPane() {
        return borderPane;
    }


    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getTilesNumber() {
        return tilesNumber;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(Tile[] tile : tiles)
        {
            for (Tile t: tile)
            {
                stringBuilder.append(t.getTileType()).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
       launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Map map = new Map("Sting" , 150 , 150 );
        map.initialize();
        primaryStage.setScene(new Scene(map.getPane()));
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Stack;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

/**
 *
 * @author stini
 */
public class PaintApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Stack<Shape> undoHistory = new Stack();
        Stack<Shape> redoHistory = new Stack();
        
        // Create toggle buttons for every tool
        ToggleButton drawBtn = new ToggleButton("Draw");
        ToggleButton rubberBtn = new ToggleButton("Rubber");
        ToggleButton lineBtn = new ToggleButton("Line");
        ToggleButton rectBtn = new ToggleButton("Rectangle");
        ToggleButton cirlceBtn = new ToggleButton("Circle");
        ToggleButton ellipseBtn = new ToggleButton("Ellipse");
        ToggleButton textBtn = new ToggleButton("Text");
        
        ToggleButton[] toolsArray = {drawBtn, rubberBtn, lineBtn, rectBtn, cirlceBtn, ellipseBtn, textBtn};
        ToggleGroup tools = new ToggleGroup();
        
        // Set toggle button's attributes
        for (ToggleButton tool : toolsArray) {
            tool.setMinWidth(90);
            tool.setToggleGroup(tools);
            tool.setCursor(Cursor.HAND);
        }
        
        // Create two color pickers, one for the line and one for the fill, and set a default color
        ColorPicker cpLine = new ColorPicker(Color.BLACK);
        ColorPicker cpFill = new ColorPicker(Color.TRANSPARENT);
        
        // Create a text you want to appear
        TextArea text = new TextArea();
        text.setPrefRowCount(1);
                
        Slider slider = new Slider(1, 50, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        
        // Labels for some tools
        Label line_color = new Label("Line Color");
        Label fill_color = new Label("Fill Color");
        Label line_width = new Label("3.0");
        
        // Basic tasks buttons
        Button undo = new Button("Undo");
        Button redo = new Button("Redo");
        Button save = new Button("Save");
        Button open = new Button("Open");
        
        Button[] basicArray = {undo, redo, save, open};
        
        // Set button's attributes and style
        for (Button btn : basicArray) {
            btn.setMinWidth(90);
            btn.setCursor(Cursor.HAND);
            btn.setTextFill(Color.WHITE);
            btn.setStyle("-fx-background-color: #666;");
        }        
        save.setStyle("-fx-background-color: #80334d;");
        open.setStyle("-fx-background-color: #80334d;");
        
        // Create a vertical box to use it as a pallete and put everything in here
        VBox btns = new VBox(10);
        btns.getChildren().addAll(drawBtn, rubberBtn, lineBtn, rectBtn, cirlceBtn, ellipseBtn,
                                  textBtn, text, line_color, cpLine, fill_color, cpFill, line_width, 
                                  slider, undo, redo, open, save);
        btns.setPadding(new Insets(5));
        btns.setStyle("-fx-background-color: #999;");
        btns.setPrefWidth(100);
        
        // Create the canvas you will be drawing to
        Canvas canvas = new Canvas(1080, 790);
        GraphicsContext gc;
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1);
        
        Line line = new Line();
        Rectangle rect = new Rectangle();
        Circle circle = new Circle();
        Ellipse ellipse = new Ellipse();
        
        // When you press your mouse click this event triggers
        canvas.setOnMousePressed((event) -> {
            // If the draw button is selected then begin a path with the cpLine color at x, y
            if (drawBtn.isSelected()){
                gc.setStroke(cpLine.getValue());
                gc.beginPath();
                gc.lineTo(event.getX(), event.getY());
            }
//            else if (rubberBtn.isSelected()) {
//                double lineWidth = gc.getLineWidth();
//                gc.clearRect(event.getX() - lineWidth / 2, event.getY() - lineWidth / 2, lineWidth, lineWidth);
//            }
//            else if (lineBtn.isSelected()){
//                
//            }
        });
        
        // When you drag your mouse with pressed click this event triggers
        canvas.setOnMouseDragged((event) -> {
            // If the draw button is selected then draw at x, y
            if (drawBtn.isSelected()) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
        });
        
        // When you finally release your mouse click this event triggers
        canvas.setOnMouseReleased((event) -> {
            // If the draw button is selected then close the path
            if (drawBtn.isSelected()) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath();
            }
        });
        
        // When you press the color picker and you pick a color this event changes the default color for the line
        cpLine.setOnAction((event) -> {
            gc.setStroke(cpLine.getValue());
        });
        // When you press the color picker and you pick a color this event changes the default color for the fill
        cpFill.setOnAction((event) -> {
            gc.setFill(cpFill.getValue());
        });
        
        // Create a new border pane and set at the center the canvas and at the left your pallete
        BorderPane pane = new BorderPane();
        pane.setLeft(btns);
        pane.setCenter(canvas);
        
        // Create a new scene and put inside the pane
        Scene scene = new Scene(pane, 1200, 800);
        
        // Set a title for your application, set your scene, show the final app
        primaryStage.setTitle("Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Inspired by abdelaziz321
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

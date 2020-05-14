/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author stini
 */
public class PaintApp extends Application {
    final static int CANVAS_WIDTH = 1080;
    final static int CANVAS_HEIGHT = 790;
    
    @Override
    public void start(Stage primaryStage) {
        Stack<Shape> undoHistory = new Stack();
        Stack<Shape> redoHistory = new Stack();
        
        // Create toggle buttons for every tool
        ToggleButton drawBtn = new ToggleButton("Draw");
        ToggleButton rubberBtn = new ToggleButton("Rubber");
        ToggleButton lineBtn = new ToggleButton("Line");
        ToggleButton rectBtn = new ToggleButton("Rectangle");
        ToggleButton circleBtn = new ToggleButton("Circle");
        ToggleButton ellipseBtn = new ToggleButton("Ellipse");
        ToggleButton textBtn = new ToggleButton("Text");
        
        ToggleButton[] toolsArray = {drawBtn, rubberBtn, lineBtn, rectBtn, circleBtn, ellipseBtn, textBtn};
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
        btns.getChildren().addAll(drawBtn, rubberBtn, lineBtn, rectBtn, circleBtn, ellipseBtn,
                                  textBtn, text, line_color, cpLine, fill_color, cpFill, line_width, 
                                  slider, undo, redo, open, save);
        btns.setPadding(new Insets(5));
        btns.setStyle("-fx-background-color: #999;");
        btns.setPrefWidth(100);
        
        // Create the canvas you will be drawing to
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
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
            else if (rubberBtn.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(event.getX() - lineWidth / 2, event.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
            else if (lineBtn.isSelected()){
                gc.setStroke(cpLine.getValue());
                line.setStartX(event.getX());
                line.setStartY(event.getY());
            }
            else if (rectBtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                rect.setX(event.getX());
                rect.setY(event.getY());
            }
            else if (circleBtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                circle.setCenterX(event.getX());
                circle.setCenterY(event.getY());
            }
            else if (ellipseBtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                ellipse.setCenterX(event.getX());
                ellipse.setCenterY(event.getY());
            }
        });
        
        // When you drag your mouse with pressed click this event triggers
        canvas.setOnMouseDragged((event) -> {
            // If the draw button is selected then draw at x, y
            if (drawBtn.isSelected()) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
            else if (rubberBtn.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(event.getX() - lineWidth / 2, event.getY() - lineWidth / 2, lineWidth, lineWidth);
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
            else if (lineBtn.isSelected()) {
                line.setEndX(event.getX());
                line.setEndY(event.getY());
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            }
            else if (rectBtn.isSelected()) {
                rect.setWidth(Math.abs((event.getX() - rect.getX())));
                rect.setHeight(Math.abs((event.getY()- rect.getY())));
                rect.setX((rect.getX() > event.getX()) ? event.getX() : rect.getX());
                rect.setY((rect.getY() > event.getY()) ? event.getY() : rect.getY());
                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            }
            else if (circleBtn.isSelected()) {
                circle.setRadius(Math.abs(event.getX() - circle.getCenterX()));
                circle.setCenterX((circle.getCenterX() > event.getX()) ? event.getX() : circle.getCenterX());
                circle.setCenterY((circle.getCenterY() > event.getX()) ? event.getY() : circle.getCenterY());
                gc.fillOval(circle.getCenterX() - circle.getRadius() / 2, 
                            circle.getCenterY() - circle.getRadius() / 2,
                            circle.getRadius() * 2, circle.getRadius() * 2);
                gc.strokeOval(circle.getCenterX() - circle.getRadius() / 2,
                              circle.getCenterY() - circle.getRadius() / 2,
                              circle.getRadius() * 2, circle.getRadius() * 2);
            }
            else if (ellipseBtn.isSelected()) {
                ellipse.setRadiusX(Math.abs(event.getX() - ellipse.getCenterX()));
                ellipse.setRadiusY(Math.abs(event.getY() - ellipse.getCenterY()));
                ellipse.setCenterX((ellipse.getCenterX() > event.getX()) ? event.getX() : ellipse.getCenterX());
                ellipse.setCenterY((ellipse.getCenterY() > event.getX()) ? event.getY() : ellipse.getCenterY());
                gc.fillOval(ellipse.getCenterX() - ellipse.getRadiusX() / 2, 
                            ellipse.getCenterY() - ellipse.getRadiusY() / 2,
                            ellipse.getRadiusX() * 2, ellipse.getRadiusY() * 2);
                gc.strokeOval(ellipse.getCenterX() - ellipse.getRadiusX() / 2,
                              ellipse.getCenterY() - ellipse.getRadiusY() / 2,
                              ellipse.getRadiusX() * 2, ellipse.getRadiusY() * 2);
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
        
        slider.valueProperty().addListener((event) -> {
            double width = slider.getValue();
            line_width.setText(String.format("%.1f", width));
            gc.setLineWidth(width);
        });
        
        // Open a file and draw it
        open.setOnAction((event) -> {
           FileChooser openFile = new FileChooser();
           openFile.setTitle("Open File");
            File file = openFile.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    InputStream io = new FileInputStream(file);
                    Image img = new Image(io);
                    gc.drawImage(img, 0, 0);
                } catch (IOException ex) {
                   Logger.getLogger(PaintApp.class.getName()).log(Level.SEVERE, "Error", ex);
               }
            }
        });
        
        save.setOnAction((event) -> {
            FileChooser saveFile = new FileChooser();
            saveFile.setTitle("Save File");

            // Set extension filter
            FileChooser.ExtensionFilter extFilter = 
                    new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            saveFile.getExtensionFilters().add(extFilter);

            // Show save file dialog
            File file = saveFile.showSaveDialog(primaryStage);

            if(file != null){
                try {
                    WritableImage writableImage = new WritableImage(CANVAS_WIDTH, CANVAS_HEIGHT);
                    canvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException ex) {
                    Logger.getLogger(PaintApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
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

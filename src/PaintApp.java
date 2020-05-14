/*
 * - Undo & Redo : not working with free draw and rubber
 * - Line & Rect & Circ ... can't be updated while drawing
 */

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
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
        Button clear = new Button("Clear");

        Button[] basicArray = {undo, redo, save, open, clear};
        
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
                                  slider, undo, redo, clear, open, save);
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
            // If the rubber button is selected then clear at the specific location with the line width as rubber size
            else if (rubberBtn.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(event.getX() - lineWidth / 2, event.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
            // If the line button is selected then set a stroke color and set the starting x, y
            else if (lineBtn.isSelected()){
                gc.setStroke(cpLine.getValue());
                line.setStartX(event.getX());
                line.setStartY(event.getY());
            }
            // If the rectangle button is selected then set a stroke color and a fill color and set the x, y
            else if (rectBtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                rect.setX(event.getX());
                rect.setY(event.getY());
            }
            // If the circle button is selected then set a stroke color and a fill color and set the center x, y
            else if (circleBtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                circle.setCenterX(event.getX());
                circle.setCenterY(event.getY());
            }
            // If the ellipse button is selected then set a stroke color and a fill color and set the center x, y
            else if (ellipseBtn.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                ellipse.setCenterX(event.getX());
                ellipse.setCenterY(event.getY());
            }
            else if (textBtn.isSelected()){
                gc.setLineWidth(1);
                gc.setFont(Font.font(slider.getValue()));
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                gc.fillText(text.getText(), event.getX(), event.getY());
                gc.strokeText(text.getText(), event.getX(), event.getY());
            }
        });
        
        // When you drag your mouse with pressed click this event triggers
        canvas.setOnMouseDragged((event) -> {
            // If the draw button is selected then draw at x, y
            if (drawBtn.isSelected()) {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
            // If the rubber button is selected then clear at the specific location with the line width as rubber size
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
            // If the line button is selected set the end x, y and draw a line from start x, y to end x, y
            else if (lineBtn.isSelected()) {
                line.setEndX(event.getX());
                line.setEndY(event.getY());
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
                // Add to the stack for undo
                undoHistory.push(new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
            }
            // If the rectangle button is selected set the width and the height, the x, y and draw the shape
            else if (rectBtn.isSelected()) {
                rect.setWidth(Math.abs((event.getX() - rect.getX())));
                rect.setHeight(Math.abs((event.getY()- rect.getY())));
                rect.setX((rect.getX() > event.getX()) ? event.getX() : rect.getX());
                rect.setY((rect.getY() > event.getY()) ? event.getY() : rect.getY());
                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                // Add to the stack for undo
                undoHistory.push(new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
            }
            // If the circle button is selected set the radius and the center x, y then draw the shape
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
                // Add to the stack for undo
                undoHistory.push(new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius()));                
            }
            // If the ellipse button is selected set the radius x, y and the center x, y then draw the shape
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
                // Add to the stack for undo
                undoHistory.push(new Ellipse(ellipse.getCenterX(), ellipse.getCenterY(), ellipse.getRadiusX(), ellipse.getRadiusY()));
            }
            redoHistory.clear();
            Shape lastUndo = undoHistory.lastElement();
            lastUndo.setFill(gc.getFill());
            lastUndo.setStroke(gc.getStroke());
            lastUndo.setStrokeWidth(gc.getLineWidth());            
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
        
        // When the clear button is pressed the canvas reset and clear the stack
        clear.setOnAction((event) -> {
            gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            redoHistory.clear();
            undoHistory.clear();
        });
        
        // When the undo button is pressed this function triggers
        undo.setOnAction((event) -> {
            if (!undoHistory.isEmpty()){
                // First clear the canvas
                gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                // Get the element you want to remove
                Shape removedShape = undoHistory.lastElement();
                // Check it's class and push it to redoHistory
                if (removedShape.getClass() == Line.class){
                    Line tempLine = (Line)removedShape;
                    redoHistory.push(tempLine);
                }
                else if (removedShape.getClass() == Rectangle.class){
                    Rectangle tempRect = (Rectangle)removedShape;
                    redoHistory.push(tempRect);
                }
                else if (removedShape.getClass() == Circle.class){
                    Circle tempCircle = (Circle)removedShape;
                    redoHistory.push(tempCircle);
                }
                else if (removedShape.getClass() == Ellipse.class){
                    Ellipse tempEllipse = (Ellipse)removedShape;
                    redoHistory.push(tempEllipse);
                }
                
                // Remove the element from undoHistory
                undoHistory.pop();
                // Run a for loop to draw the remaining shapes
                for(int i=0; i < undoHistory.size(); i++) {
                    Shape shape = undoHistory.elementAt(i);
                    if(shape.getClass() == Line.class) {
                        Line temp = (Line) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.strokeLine(temp.getStartX(), temp.getStartY(), temp.getEndX(), temp.getEndY());
                    }
                    else if(shape.getClass() == Rectangle.class) {
                        Rectangle temp = (Rectangle) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.fillRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                        gc.strokeRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                    }
                    else if(shape.getClass() == Circle.class) {
                        Circle temp = (Circle) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.fillOval(temp.getCenterX(), temp.getCenterY(), temp.getRadius(), temp.getRadius());
                        gc.strokeOval(temp.getCenterX(), temp.getCenterY(), temp.getRadius(), temp.getRadius());
                    }
                    else if(shape.getClass() == Ellipse.class) {
                        Ellipse temp = (Ellipse) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.fillOval(temp.getCenterX(), temp.getCenterY(), temp.getRadiusX(), temp.getRadiusY());
                        gc.strokeOval(temp.getCenterX(), temp.getCenterY(), temp.getRadiusX(), temp.getRadiusY());
                    }
                }
            }
            else {
                System.out.println("there is no action to undo");
            }
        });
        
        // When the redo button is pressed this function triggers
        redo.setOnAction((event) -> {
            if (!redoHistory.isEmpty()) {
                // Get the last element
                Shape shape = redoHistory.lastElement();
                // Set the drawing attributes
                gc.setLineWidth(shape.getStrokeWidth());
                gc.setStroke(shape.getStroke());
                gc.setFill(shape.getFill());
                // Remove from the stack
                redoHistory.pop();
                
                // Check it's class, draw it and push it to undoHistory
                if (shape.getClass() == Line.class) {
                    Line tempLine = (Line)shape;
                    gc.strokeLine(tempLine.getStartX(), tempLine.getStartY(), tempLine.getEndX(), tempLine.getEndY());
                    undoHistory.push(tempLine);
                }
                else if (shape.getClass() == Rectangle.class) {
                    Rectangle tempRect = (Rectangle)shape;
                    gc.strokeRect(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight());
                    gc.fillRect(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight());
                    undoHistory.push(tempRect);
                }
                else if (shape.getClass() == Circle.class) {
                    Circle tempCircle = (Circle)shape;
                    gc.strokeOval(tempCircle.getCenterX(), tempCircle.getCenterY(), tempCircle.getRadius(), tempCircle.getRadius());
                    gc.fillOval(tempCircle.getCenterX(), tempCircle.getCenterY(), tempCircle.getRadius(), tempCircle.getRadius());
                    undoHistory.push(tempCircle);
                }
                else if (shape.getClass() == Ellipse.class) {
                    Ellipse tempEllipse = (Ellipse)shape;
                    gc.strokeOval(tempEllipse.getCenterX(), tempEllipse.getCenterY(), tempEllipse.getRadiusX(), tempEllipse.getRadiusY());
                    gc.fillOval(tempEllipse.getCenterX(), tempEllipse.getCenterY(), tempEllipse.getRadiusX(), tempEllipse.getRadiusY());
                    undoHistory.push(tempEllipse);
                }                
            }
            else {
                System.out.println("there is no action to redo");
            }
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

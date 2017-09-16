package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.util.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Editor extends Application {

    private static final String MESSAGE_PREFIX = "command";

    private static final int STARTING_FONT_SIZE = 12;
    private static final int STARTING_X = 5;
    private static final int STARTING_Y = 0;
    private static final int MARGIN = 19;
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    private String fontName = "Verdana";

    private int width;
    private int height;
    private int fontSize;
    private int lineSize;

    private UndoRedo undoredo;
    private Group textRoot;
    private Group root;
    private final Rectangle cursorRectangle;

    private LinkedList<Text> text;


    public Editor() {
        cursorRectangle = new Rectangle(1, STARTING_FONT_SIZE);
        text = new LinkedList<Text>();
        width = WINDOW_WIDTH;
        height = WINDOW_HEIGHT;
        fontSize = STARTING_FONT_SIZE;
        lineSize = getLineSize();
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        KeyEventHandler(Group root){

        }
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.isShortcutDown()) {
                if (keyEvent.getCode() == KeyCode.A) {
                    System.out.println(MESSAGE_PREFIX + " in addition to \"a\"");
                }
                else if (keyEvent.getCode() == KeyCode.Z) {
                    Text temp = undoredo.undo();
                    if (temp != null) {
                        temp.setFont(Font.font(fontName, fontSize));
                    }
                    updateDisplay();

                } else if (keyEvent.getCode() == KeyCode.Y) {
                    Text temp = undoredo.redo();
                    if (temp != null) {
                        temp.setFont(Font.font(fontName, fontSize));
                    }
                    updateDisplay();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8 &&
                        characterTyped.charAt(0) != 13 && characterTyped.charAt(0) != 127) {
                    Text t = new Text(0, 0, characterTyped);
                    t.setTextOrigin(VPos.TOP);
                    t.setFont(Font.font(fontName, fontSize));
                    textRoot.getChildren().add(t);
                    t.toFront();
                    text.add(t);
                    undoredo.add(text.cursor.prev, UndoRedo.INSERT, text.cursor);
                    keyEvent.consume();
                    updateDisplay();
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    fontSize += 5;
                    for (Text t : text) {
                        t.setFont(Font.font(fontName, fontSize));
                    }
                    updateDisplay();
                } if (code == KeyCode.DOWN) {
                    fontSize = Math.max(0, fontSize - 5);
                    for (Text t : text) {
                        t.setFont(Font.font(fontName, fontSize));
                    }
                    updateDisplay();
                } if (code == KeyCode.BACK_SPACE) {
                    LinkedList.Node tempNode = text.cursor.prev;
                    Text temp = text.remove();
                    if (temp != null) {
                        textRoot.getChildren().remove(temp);
                        undoredo.add(tempNode, UndoRedo.REMOVE, text.cursor);
                        updateDisplay();
                    }
                }
                if (code == KeyCode.ENTER){
                    Text t = new Text(0, 0, "\n");
                    text.add(t);
                    textRoot.getChildren().add(t);
                    updateDisplay();
                }

            }
        }
     }
    private int getLineSize() {
        Text temp = new Text(0, 0, " ");
        temp.setFont(Font.font(fontName, fontSize));
        return (int) (temp.getLayoutBounds().getHeight() + .5);
    }
    private void updateDisplay() {
        lineSize = getLineSize();

        Text cursorTextObject = new Text();

        int currentX = STARTING_X;
        int currentY = STARTING_Y;

        boolean drawCursorAtEnd = true;
        boolean currentWordStartsLine = true;

        Iterator<Text> textObjects = text.iterator();

        Text currentText = textObjects.next();



        ArrayList<Text> currentWordTextBoxes = new ArrayList<>();
        ArrayList<Integer> currentWordXDisplacements = new ArrayList<>();

        while (currentText != null) {
            currentText.setX(currentX);
            currentText.setY(currentY);



            int characterWidth = (int) (currentText.getLayoutBounds().getWidth() + .5);
            currentX += characterWidth;

            String currLetter = currentText.getText();
            if (currLetter.equals(" ") || currLetter.equals("\n")) {


                currentWordTextBoxes = new ArrayList<>();
                currentWordXDisplacements = new ArrayList<>();

                if (currLetter.equals(" ")) {
                    currentWordStartsLine = false;
                } else {
                    currentWordStartsLine = true;
                    currentX = STARTING_X;
                    currentY += lineSize;
                }

            } else {
                if (currentWordStartsLine) {
                    if (currentX > width - MARGIN) {
                        currentX = STARTING_X;
                        currentY += lineSize;
                        currentText.setX(currentX);
                        currentText.setY(currentY);

                        currentX += (int) (currentText.getLayoutBounds().getWidth() + .5);
                    }
                } else {

                    currentWordTextBoxes.add(currentText);
                    currentWordXDisplacements.add(characterWidth);
                    if (currentX > width - MARGIN) {
                        currentY += lineSize;
                        currentX = STARTING_X;

                        currentWordStartsLine = true;
                        for (int i = 0; i < currentWordTextBoxes.size(); i++) {
                            currentWordTextBoxes.get(i).setX(currentX);
                            currentWordTextBoxes.get(i).setY(currentY);
                            currentX += currentWordXDisplacements.get(i);
                        }
                    }
                }
            }

            if (((LinkedList.LinkedListIterator) textObjects).isNextCursorNode()) {
                drawCursorAtEnd = false;
                cursorTextObject = currentText;
            }

            currentText = textObjects.next();
        }

        if (drawCursorAtEnd) {
            cursorRectangle.setX(Math.min(currentX, width - MARGIN));
            cursorRectangle.setY(currentY);
            cursorRectangle.setHeight(lineSize);
        } else {
            int xVal = (int) cursorTextObject.getX();
            xVal = Math.min(xVal, width - MARGIN);
            cursorRectangle.setX(xVal);
            cursorRectangle.setY(cursorTextObject.getY());
            cursorRectangle.setHeight(lineSize);
        }
    }

    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.BLACK, Color.WHITE};

        RectangleBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }
        private void changeColor() {
            cursorRectangle.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    public void makeRectangleColorChange() {

        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        RectangleBlinkEventHandler cursorChange = new RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        Text positionText;

        MouseClickEventHandler(Group root) {
            positionText = new Text("");
            positionText.setTextOrigin(VPos.BOTTOM);
            root.getChildren().add(positionText);
        }

        public void handle(MouseEvent mouseEvent) {
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY();
            positionText.setText("(" + mousePressedX + ", " + mousePressedY + ")");
            positionText.setX(mousePressedX);
            positionText.setY(mousePressedY);
        }
    }

    @Override
    public void start(Stage stage) {
        root = new Group();


        textRoot = new Group();
        text = new LinkedList<Text>();
        undoredo = new UndoRedo(text, textRoot);


        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);


        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);

        textRoot.getChildren().add(cursorRectangle);

        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        root.getChildren().add(scrollBar);
        root.getChildren().add(textRoot);
        double usableScreenWidth = WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth();
        scrollBar.setLayoutX(usableScreenWidth);
        makeRectangleColorChange();
        scene.setOnMouseClicked(new MouseClickEventHandler(root));
        stage.setTitle("Single Letter Display Simple");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
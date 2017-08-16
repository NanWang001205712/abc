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
import java.util.LinkedList;

public class Editor extends Application {

    private final Rectangle textBoundingBox;
    private static final String MESSAGE_PREFIX = "command";
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;
    private static final int STARTING_FONT_SIZE = 12;
    private static final int STARTING_TEXT_POSITION_X = 5;
    private static final int STARTING_TEXT_POSITION_Y = 0;
    private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");

    double cursorX;
    double cursorY;

    public Editor() {
        textBoundingBox = new Rectangle(1, STARTING_FONT_SIZE);
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        private LinkedList<Character> list = new LinkedList<>();
        private String txt;
        private int cursorPos = 0;

        private int fontSize = STARTING_FONT_SIZE;
        private String fontName = "Verdana";
        public int line = 1;

        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            cursorX = 0;
            cursorY = (line - 1) * 1.22 * fontSize;
            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));
            textBoundingBox.setX(cursorX);
            textBoundingBox.setY(cursorY);
            root.getChildren().add(displayText);
            root.getChildren().add(textBoundingBox);
        }

        public void handle(KeyEvent keyEvent) {
            if (keyEvent.isShortcutDown()) {
                if (keyEvent.getCode() == KeyCode.A) {
                    System.out.println(MESSAGE_PREFIX + " in addition to \"a\"");
                } else if (keyEvent.getCode() == KeyCode.Z) {
                    System.out.println(MESSAGE_PREFIX + " in addition to \"z\"");
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    list.add(cursorPos, characterTyped.charAt(0));
                    txt = getText(list);
                    cursorPos++;
                    displayText.setText(txt);
                    centerText();
                    keyEvent.consume();
                }

            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    fontSize += 5;
                    displayText.setFont(Font.font(fontName, fontSize));

                } else if (code == KeyCode.DOWN) {
                    fontSize = Math.max(0, fontSize - 5);
                    displayText.setFont(Font.font(fontName, fontSize));

                } else if (code == KeyCode.BACK_SPACE) {
                    if(list.size()>0) {
                        list.removeLast();
                        txt = getText(list);
                        displayText.setText(txt);
                        cursorPos--;
                        centerText();
                    }
                }

            }
        }
        private String enterCursor(String t) {

            t = "";
            return t;
        }
        private void centerText() {
            // Figure out the size of the current text.
            String txt = "";
            line = 1;
            Text txtTemp = new Text("");
            txtTemp.setTextOrigin(VPos.TOP);
            txtTemp.setFont(Font.font(fontName, fontSize));

            for (int i = 0; i < cursorPos; i++) {
                txt = txt + list.get(i);
                if (list.get(i) == 13) {
                    txt = enterCursor(txt);
                    line++;
                    cursorY = (line - 1) * 1.22 * fontSize;
                    textBoundingBox.setY(cursorY);
                }
            }
            txtTemp.setText(txt);
            double textHeight = displayText.getLayoutBounds().getHeight();
            double textWidth = txtTemp.getLayoutBounds().getWidth();
            if(textHeight<=1.22*fontSize){
                cursorY=0;
                textBoundingBox.setY(cursorY);
            }
            if (textWidth <= WINDOW_WIDTH) {
                cursorX = 5 + textWidth;
                textBoundingBox.setX(cursorX);
            }
            // Make sure the text appears in front of any other objects you might add.
            displayText.toFront();
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
            textBoundingBox.setFill(boxColors[currentColorIndex]);
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
        Group root = new Group();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        root.getChildren().add(scrollBar);
        double usableScreenWidth = WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth();
        scrollBar.setLayoutX(usableScreenWidth);
        makeRectangleColorChange();
        scene.setOnMouseClicked(new MouseClickEventHandler(root));
        stage.setTitle("Single Letter Display Simple");
        stage.setScene(scene);
        stage.show();
    }

    private String getText(LinkedList<Character> l) {
        String res = "";
        for (int i = 0; i < l.size(); i++) {
            res = res + l.get(i);
        }
        return res;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
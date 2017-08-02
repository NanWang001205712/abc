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


public class Editor extends Application {

    private final Rectangle textBoundingBox;


    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 500;

    public Editor() {

        textBoundingBox = new Rectangle(0, 0);
    }


    private class KeyEventHandler implements EventHandler<KeyEvent>{

        int textCenterX;
        int textCenterY;

        private static final int STARTING_FONT_SIZE = 12;
        private static final int STARTING_TEXT_POSITION_X = 0;
        private static final int STARTING_TEXT_POSITION_Y = 0;

        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");

        private int fontSize = STARTING_FONT_SIZE;
        private String fontName = "Verdana";


        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = windowWidth / 2;
            textCenterY = windowHeight / 2;

            displayText = new Text(textCenterX, textCenterY, "");

            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));

            root.getChildren().add(displayText);

        }
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {

                String characterTyped = keyEvent.getCharacter();

                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {

                    displayText.setText(characterTyped);

                    keyEvent.consume();
                }
                centerText();
            }


            else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    fontSize += 5;
                    displayText.setFont(Font.font(fontName, fontSize));
                    centerText();
                } else if (code == KeyCode.DOWN) {
                    fontSize = Math.max(0, fontSize - 5);
                    displayText.setFont(Font.font(fontName, fontSize));
                    centerText();
                }
            }
        }

        private void centerText() {
            // Figure out the size of the current text.
            double textHeight = displayText.getLayoutBounds().getHeight();
            double textWidth = displayText.getLayoutBounds().getWidth();

            // Calculate the position so that the text will be centered on the screen.
            double textTop = textCenterY - textHeight / 2;
            double textLeft = textCenterX - textWidth / 2;

            // Re-position the text.
            displayText.setX(textLeft);
            displayText.setY(textTop);


            textBoundingBox.setHeight(textHeight);
            textBoundingBox.setWidth(1);

            textBoundingBox.setX(textWidth);
            textBoundingBox.setY(textTop);




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
        MouseClickEventHandler(Group root){
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
        root.getChildren().add(textBoundingBox);

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

    public static void main(String[] args) {
        launch(args);
    }
}
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class TicTacToe extends Application {
    private char whoseTurn = 'X';

    private final Cell[][] cell = new Cell[3][3];

    private final Label lblStatus = new Label("Enter player names and press Start");
    private final Label lblScore = new Label("Score: 0 - 0");

    private final TextField txtPlayer1 = new TextField("Player 1");
    private final TextField txtPlayer2 = new TextField("Player 2");

    private final ComboBox<String> modeBox = new ComboBox<>();

    private final Button btnStart = new Button("Start / New Match");
    private final Button btnPlayAgain = new Button("Play Again");

    private final GridPane gridPane = new GridPane();
    private final Pane effectsPane = new Pane();

    private final Player player1 = new Player("Player 1");
    private final Player player2 = new Player("Player 2");
    private final GameScore score = new GameScore(player1, player2);

    private Player xPlayer = player1;
    private Player oPlayer = player2;

    private boolean gameStarted = false;
    private boolean roundOver = false;
    private boolean matchOver = false;

    private int roundNumber = 1;
    private int winsNeeded = 1;

    private static final Color X_COLOR = Color.CRIMSON;
    private static final Color O_COLOR = Color.DODGERBLUE;
    private static final Color WIN_COLOR = Color.LIMEGREEN;

    @Override
    public void start(Stage primaryStage) {
        txtPlayer1.setPrefWidth(120);
        txtPlayer2.setPrefWidth(120);

        modeBox.getItems().addAll("Single game", "Best of 3", "Best of 5");
        modeBox.setValue("Single game");

        btnStart.setOnAction(_ -> startNewMatch());

        btnPlayAgain.setOnAction(_ -> {
            if (matchOver) {
                startNewMatch();
            } else {
                roundNumber++;
                startNewRound();
            }
        });

        btnPlayAgain.setDisable(true);

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.getChildren().addAll(
                new Label("Player 1:"), txtPlayer1,
                new Label("Player 2:"), txtPlayer2,
                new Label("Mode:"), modeBox,
                btnStart,
                btnPlayAgain
        );

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(10));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cell[i][j] = new Cell();
                gridPane.add(cell[i][j], j, i);
            }
        }

        effectsPane.setMouseTransparent(true);
        effectsPane.setPrefSize(470, 470);

        StackPane boardPane = new StackPane();
        boardPane.setPrefSize(470, 470);
        boardPane.getChildren().addAll(gridPane, effectsPane);

        lblStatus.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #102A43;");
        lblScore.setStyle("-fx-font-size: 16px; -fx-text-fill: #243B53;");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #E0F2FE, #F8FAFC);" +
                        "-fx-font-family: Arial;"
        );

        topBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.75);" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 15;"
        );

        btnStart.setStyle(buttonStyle());
        btnPlayAgain.setStyle(buttonStyle());

        root.getChildren().addAll(topBox, boardPane, lblStatus, lblScore);

        Scene scene = new Scene(root, 850, 650);

        primaryStage.setTitle("TicTacToe");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String buttonStyle() {
        return "-fx-background-color: #2563EB;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 8 14 8 14;";
    }

    private void startNewMatch() {
        player1.setName(readName(txtPlayer1, "Player 1"));
        player2.setName(readName(txtPlayer2, "Player 2"));

        String mode = modeBox.getValue();

        if (mode.equals("Best of 3")) {
            winsNeeded = 2;
        } else if (mode.equals("Best of 5")) {
            winsNeeded = 3;
        } else {
            winsNeeded = 1;
        }

        score.setWinsNeeded(winsNeeded);
        score.reset();

        roundNumber = 1;
        gameStarted = true;
        matchOver = false;

        startNewRound();
    }

    private String readName(TextField textField, String defaultName) {
        String text = textField.getText().trim();

        if (text.isEmpty()) {
            return defaultName;
        }

        return text;
    }

    private void startNewRound() {
        effectsPane.getChildren().clear();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cell[i][j].clear();
            }
        }

        assignPlayersForRound();

        whoseTurn = 'X';
        roundOver = false;

        btnPlayAgain.setText("Play Again");
        btnPlayAgain.setDisable(true);

        lblStatus.setText("Round " + roundNumber + ": " + xPlayer.getName() + " (X) starts");
        updateScore();
    }

    private void assignPlayersForRound() {
        if (roundNumber % 2 == 1) {
            xPlayer = player1;
            oPlayer = player2;
        } else {
            xPlayer = player2;
            oPlayer = player1;
        }
    }

    private Player getPlayerForToken(char token) {
        if (token == 'X') {
            return xPlayer;
        }

        return oPlayer;
    }

    private void updateScore() {
        lblScore.setText(score.getText(xPlayer, oPlayer, roundNumber));
    }

    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cell[i][j].getToken() == ' ') {
                    return false;
                }
            }
        }

        return true;
    }

    private Cell[] getWinningCells(char token) {
        for (int i = 0; i < 3; i++) {
            if (cell[i][0].getToken() == token &&
                    cell[i][1].getToken() == token &&
                    cell[i][2].getToken() == token) {
                return new Cell[] { cell[i][0], cell[i][1], cell[i][2] };
            }
        }

        for (int j = 0; j < 3; j++) {
            if (cell[0][j].getToken() == token &&
                    cell[1][j].getToken() == token &&
                    cell[2][j].getToken() == token) {
                return new Cell[] { cell[0][j], cell[1][j], cell[2][j] };
            }
        }

        if (cell[0][0].getToken() == token &&
                cell[1][1].getToken() == token &&
                cell[2][2].getToken() == token) {
            return new Cell[] { cell[0][0], cell[1][1], cell[2][2] };
        }

        if (cell[0][2].getToken() == token &&
                cell[1][1].getToken() == token &&
                cell[2][0].getToken() == token) {
            return new Cell[] { cell[0][2], cell[1][1], cell[2][0] };
        }

        return null;
    }

    private void finishWithWinner(Cell[] winningCells) {
        roundOver = true;

        for (int i = 0; i < 3; i++) {
            winningCells[i].highlightWin();
        }

        drawWinningLine(winningCells[0], winningCells[2]);

        GameEffect effect = new ConfettiEffect(effectsPane);
        effect.play();

        Player winner = getPlayerForToken(whoseTurn);
        score.addWin(winner);
        updateScore();

        if (score.hasMatchWinner(winner)) {
            matchOver = true;
            lblStatus.setText(winner.getName() + " won the match!");
            btnPlayAgain.setText("New Match");
            btnPlayAgain.setDisable(false);
        } else {
            lblStatus.setText(winner.getName() + " won this round!");
            btnPlayAgain.setText("Next Round");
            btnPlayAgain.setDisable(false);
        }
    }

    private void finishWithDraw() {
        roundOver = true;

        score.addDraw();
        updateScore();

        GameEffect effect = new HandshakeEffect(effectsPane);
        effect.play();

        if (winsNeeded == 1) {
            matchOver = true;
            lblStatus.setText("Draw! The game is over");
            btnPlayAgain.setText("New Match");
        } else {
            lblStatus.setText("Draw! Press Next Round");
            btnPlayAgain.setText("Next Round");
        }

        btnPlayAgain.setDisable(false);
    }

    private void switchTurn() {
        if (whoseTurn == 'X') {
            whoseTurn = 'O';
        } else {
            whoseTurn = 'X';
        }

        Player currentPlayer = getPlayerForToken(whoseTurn);
        lblStatus.setText(currentPlayer.getName() + " (" + whoseTurn + ")'s turn");
    }

    private void drawWinningLine(Cell first, Cell last) {
        Platform.runLater(() -> {
            Point2D startScene = first.localToScene(first.getWidth() / 2, first.getHeight() / 2);
            Point2D endScene = last.localToScene(last.getWidth() / 2, last.getHeight() / 2);

            Point2D start = effectsPane.sceneToLocal(startScene);
            Point2D end = effectsPane.sceneToLocal(endScene);

            Line line = new Line(start.getX(), start.getY(), start.getX(), start.getY());
            line.setStroke(WIN_COLOR);
            line.setStrokeWidth(10);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setOpacity(0.9);

            effectsPane.getChildren().add(line);

            Timeline animation = new Timeline(
                    new KeyFrame(
                            Duration.millis(400),
                            new KeyValue(line.endXProperty(), end.getX()),
                            new KeyValue(line.endYProperty(), end.getY())
                    )
            );

            animation.play();
        });
    }

    public class Cell extends Pane {
        private char token = ' ';
        private boolean winnerCell = false;

        public Cell() {
            setPrefSize(140, 140);
            setMinSize(140, 140);
            setMaxSize(140, 140);

            updateStyle(false);

            setOnMouseEntered(_ -> {
                if (token == ' ' && gameStarted && !roundOver && !matchOver) {
                    updateStyle(true);
                }
            });

            setOnMouseExited(_ -> {
                if (!winnerCell) {
                    updateStyle(false);
                }
            });

            setOnMouseClicked(_ -> handleMouseClick());
        }

        public char getToken() {
            return token;
        }

        public void clear() {
            token = ' ';
            winnerCell = false;
            getChildren().clear();
            updateStyle(false);
        }

        public void highlightWin() {
            winnerCell = true;
            setStyle(
                    "-fx-background-color: #DCFCE7;" +
                            "-fx-border-color: #22C55E;" +
                            "-fx-border-width: 4;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;"
            );
        }

        private void updateStyle(boolean hover) {
            if (hover) {
                setStyle(
                        "-fx-background-color: #DBEAFE;" +
                                "-fx-border-color: #2563EB;" +
                                "-fx-border-width: 3;" +
                                "-fx-background-radius: 18;" +
                                "-fx-border-radius: 18;"
                );
            } else {
                setStyle(
                        "-fx-background-color: rgba(255,255,255,0.95);" +
                                "-fx-border-color: #334155;" +
                                "-fx-border-width: 3;" +
                                "-fx-background-radius: 18;" +
                                "-fx-border-radius: 18;"
                );
            }
        }

        public void setToken(char c) {
            token = c;

            if (token == 'X') {
                Line line1 = new Line(25, 25, getWidth() - 25, getHeight() - 25);
                line1.endXProperty().bind(widthProperty().subtract(25));
                line1.endYProperty().bind(heightProperty().subtract(25));

                Line line2 = new Line(25, getHeight() - 25, getWidth() - 25, 25);
                line2.startYProperty().bind(heightProperty().subtract(25));
                line2.endXProperty().bind(widthProperty().subtract(25));

                line1.setStroke(X_COLOR);
                line2.setStroke(X_COLOR);

                line1.setStrokeWidth(9);
                line2.setStrokeWidth(9);

                line1.setStrokeLineCap(StrokeLineCap.ROUND);
                line2.setStrokeLineCap(StrokeLineCap.ROUND);

                getChildren().addAll(line1, line2);
            } else if (token == 'O') {
                Ellipse ellipse = new Ellipse(
                        getWidth() / 2,
                        getHeight() / 2,
                        getWidth() / 2 - 25,
                        getHeight() / 2 - 25
                );

                ellipse.centerXProperty().bind(widthProperty().divide(2));
                ellipse.centerYProperty().bind(heightProperty().divide(2));
                ellipse.radiusXProperty().bind(widthProperty().divide(2).subtract(25));
                ellipse.radiusYProperty().bind(heightProperty().divide(2).subtract(25));

                ellipse.setStroke(O_COLOR);
                ellipse.setStrokeWidth(9);
                ellipse.setFill(Color.TRANSPARENT);

                getChildren().add(ellipse);
            }
        }

        private void handleMouseClick() {
            if (!gameStarted) {
                lblStatus.setText("Press Start / New Match first");
                return;
            }

            if (roundOver || matchOver) {
                return;
            }

            if (token != ' ') {
                return;
            }

            setToken(whoseTurn);

            Cell[] winningCells = getWinningCells(whoseTurn);

            if (winningCells != null) {
                finishWithWinner(winningCells);
            } else if (isFull()) {
                finishWithDraw();
            } else {
                switchTurn();
            }
        }
    }

}

class Player {
    private String name;
    private int wins;

    public Player(String name) {
        this.name = name;
        this.wins = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void addWin() {
        wins++;
    }
}

class GameScore {
    private final Player player1;
    private final Player player2;
    private int draws;
    private int winsNeeded;

    public GameScore(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.draws = 0;
        this.winsNeeded = 1;
    }

    public void reset() {
        player1.setWins(0);
        player2.setWins(0);
        draws = 0;
    }

    public void addWin(Player player) {
        player.addWin();
    }

    public void addDraw() {
        draws++;
    }

    public void setWinsNeeded(int winsNeeded) {
        this.winsNeeded = winsNeeded;
    }

    public boolean hasMatchWinner(Player player) {
        return player.getWins() >= winsNeeded;
    }

    public String getText(Player xPlayer, Player oPlayer, int roundNumber) {
        return "Round: " + roundNumber +
                "    |    " + player1.getName() + ": " + player1.getWins() +
                "    |    " + player2.getName() + ": " + player2.getWins() +
                "    |    Draws: " + draws +
                "    |    Need: " + winsNeeded +
                "    |    X: " + xPlayer.getName() +
                "    |    O: " + oPlayer.getName();
    }
}

abstract class GameEffect {
    private final Pane effectsPane;

    public GameEffect(Pane effectsPane) {
        this.effectsPane = effectsPane;
    }

    protected Pane getEffectsPane() {
        return effectsPane;
    }

    public abstract void play();
}

class ConfettiEffect extends GameEffect {
    private final Random random = new Random();

    public ConfettiEffect(Pane effectsPane) {
        super(effectsPane);
    }

    @Override
    public void play() {
        Pane effectsPane = getEffectsPane();

        double width = effectsPane.getWidth();

        if (width <= 0) {
            width = 470;
        }

        double height = effectsPane.getHeight();

        if (height <= 0) {
            height = 470;
        }

        Color[] colors = {
                Color.CRIMSON,
                Color.DODGERBLUE,
                Color.GOLD,
                Color.LIMEGREEN,
                Color.ORANGE,
                Color.MEDIUMPURPLE,
                Color.DEEPPINK
        };

        for (int i = 0; i < 90; i++) {
            Rectangle piece = new Rectangle(
                    6 + random.nextInt(9),
                    8 + random.nextInt(12)
            );

            piece.setFill(colors[random.nextInt(colors.length)]);
            piece.setArcWidth(4);
            piece.setArcHeight(4);

            piece.setLayoutX(random.nextDouble() * width);
            piece.setLayoutY(-30 - random.nextDouble() * 150);

            effectsPane.getChildren().add(piece);

            TranslateTransition fall = new TranslateTransition(
                    Duration.seconds(1.5 + random.nextDouble() * 1.5),
                    piece
            );

            fall.setByY(height + 200);
            fall.setByX((random.nextDouble() - 0.5) * 160);

            RotateTransition rotate = new RotateTransition(
                    Duration.seconds(1.5 + random.nextDouble()),
                    piece
            );

            rotate.setByAngle(360 + random.nextInt(360));

            FadeTransition fade = new FadeTransition(
                    Duration.seconds(2.5),
                    piece
            );

            fade.setFromValue(1.0);
            fade.setToValue(0.15);

            ParallelTransition transition = new ParallelTransition(fall, rotate, fade);
            transition.setDelay(Duration.millis(random.nextInt(400)));

            transition.setOnFinished(_ -> effectsPane.getChildren().remove(piece));
            transition.play();
        }
    }
}

class HandshakeEffect extends GameEffect {
    public HandshakeEffect(Pane effectsPane) {
        super(effectsPane);
    }

    @Override
    public void play() {
        Pane effectsPane = getEffectsPane();

        Label label = new Label("DRAW  🤝");
        label.setStyle(
                "-fx-font-size: 52px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #334155;" +
                        "-fx-background-color: rgba(255,255,255,0.90);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 15;"
        );

        effectsPane.getChildren().add(label);

        Platform.runLater(() -> {
            label.setLayoutX(effectsPane.getWidth() / 2 - 135);
            label.setLayoutY(effectsPane.getHeight() / 2 - 55);
        });

        ScaleTransition scale = new ScaleTransition(Duration.millis(800), label);
        scale.setFromX(0.2);
        scale.setFromY(0.2);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.seconds(2.2), label);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1.2));

        ParallelTransition animation = new ParallelTransition(scale, fade);
        animation.setOnFinished(_ -> effectsPane.getChildren().remove(label));
        animation.play();
    }
}
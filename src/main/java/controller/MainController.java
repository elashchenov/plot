package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import utils.EditableCell;
import utils.Point;
import utils.StringDoubleConverter;

import java.io.IOException;
import java.text.DecimalFormat;


public class MainController {

    @Getter
    private static ObservableList<Point> points = FXCollections.observableArrayList();
    @Getter
    private static ObservableList<Point> ellipsePoints = FXCollections.observableArrayList(
            new Point(0d, 0d),
            new Point(0d, 0d)
    );

    @FXML
    private TableView<Point> ellipseTableView;
    @FXML
    private TableColumn<Point, Double> ellipseXColumn;
    @FXML
    private TableColumn<Point, Double> ellipseYColumn;

    @FXML
    private TableView<Point> pointsTableView;
    @FXML
    private TableColumn<Point, Double> pointsYColumn;
    @FXML
    private TableColumn<Point, Double> pointsXColumn;

    @FXML
    private void initialize() {
        pointsTableView.setContextMenu(getTVContextMenu());
        pointsTableView.setItems(points);

        ellipseTableView.getSelectionModel().setCellSelectionEnabled(true);
        ellipseTableView.setItems(ellipsePoints);
        initColumns();
    }

    /**
     * Устанавливаем содержимое и поведение столбцов обеих таблиц
     */
    private void initColumns() {
        pointsXColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        pointsXColumn.setCellFactory(EditableCell.forTableColumn(new StringDoubleConverter()));

        pointsYColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        pointsYColumn.setCellFactory(EditableCell.forTableColumn(new StringDoubleConverter()));

        ellipseXColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        ellipseXColumn.setCellFactory(EditableCell.forTableColumn(new StringDoubleConverter()));
        ellipseXColumn.setOnEditStart(
                event -> ellipseTableView.setMinHeight(ellipseTableView.getHeight() + 7)
        );
        ellipseXColumn.setOnEditCancel(
                event -> ellipseTableView.setMinHeight(ellipseTableView.getHeight() - 8)
        );

        ellipseYColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        ellipseYColumn.setCellFactory(EditableCell.forTableColumn(new StringDoubleConverter()));
        ellipseYColumn.setOnEditStart(
                event -> ellipseTableView.setMinHeight(ellipseTableView.getHeight() + 7)
        );
        ellipseYColumn.setOnEditCancel(
                event -> ellipseTableView.setMinHeight(ellipseTableView.getHeight() - 8)
        );
    }

    /**
     * Возвращает контекстное меню для таблицы "Множество точек"
     */
    private ContextMenu getTVContextMenu() {
        MenuItem addPointMenuItem = new MenuItem("Добавить точку");
        addPointMenuItem.setOnAction(event -> addPoint());
        MenuItem deletePointMenuItem = new MenuItem("Удалить точку");
        deletePointMenuItem.setOnAction(event -> deletePoint());
        ContextMenu contextMenu = new ContextMenu(addPointMenuItem, deletePointMenuItem);
        contextMenu.getItems().get(1).visibleProperty().bind(
                pointsTableView.getSelectionModel().selectedItemProperty().isNotNull()
        );
        return contextMenu;
    }

    private void deletePoint() {
        points.remove(pointsTableView.getSelectionModel().getSelectedItem());
    }

    private void addPoint() {
        Point newPoint = new Point(0d, 0d);
        points.add(newPoint);
    }

    /**
     * Загружает окно графика. Метод привязан к кнопке Рассчитать
     */
    @FXML
    private void showChart() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Chart.fxml"));
        AnchorPane root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Ellipse ellipse = getEllipse();
        Stage stage = new Stage();
        ChartController controller = fxmlLoader.getController();
        controller.initialize(ellipse, points);
        stage.setTitle("График");
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

        setTooltipsOnLineChart(controller.getLineChart());
        controller.refreshEllipseRadiusX(ellipse.getRadiusX());
        controller.refreshEllipseRadiusY(ellipse.getRadiusY());
    }

    /**
     * Инициализируем всплывающие подсказки всем объектам на lineChart
     * @param lineChart - поле графика
     */
    private void setTooltipsOnLineChart(LineChart<Number, Number> lineChart) {
        DecimalFormat df = new DecimalFormat("#0.0##");
        for (XYChart.Series<Number, Number> s : lineChart.getData()) {
            if (s.getData().size() > 1) {
                Point p1 = new Point(
                        s.getData().get(0).getXValue().doubleValue(),
                        s.getData().get(0).getYValue().doubleValue()
                );
                Point p2 = new Point(
                        s.getData().get(1).getXValue().doubleValue(),
                        s.getData().get(1).getYValue().doubleValue()
                );
                installTooltip("width: " + df.format(p1.distance(p2)), s.getNode());
            }
            for (XYChart.Data<Number, Number> data : s.getData()) {
                installTooltip(
                        "(" + df.format(data.getXValue()) + ", " + df.format(data.getYValue()) + ")",
                        data.getNode()
                );
            }
        }
    }

    /**
     * Привязка всплывающей подсказки к объекту на графике
     * @param title текст подсказки
     * @param node объект
     */
    private void installTooltip(String title, Node node) {
        Tooltip tooltip = new Tooltip(title);
        Tooltip.install(node, tooltip);
        tooltip.setShowDelay(Duration.seconds(0.4));
        tooltip.setStyle("-fx-font-size: 14");
    }

    /**
     * Инициализация эллипса
     */
    private Ellipse getEllipse() {
        Point leftBottomPoint = ellipsePoints.get(1);
        Point rightTopPoint = ellipsePoints.get(0);
        double radiusX = (rightTopPoint.getX() - leftBottomPoint.getX()) / 2;
        double radiusY = (rightTopPoint.getY() - leftBottomPoint.getY()) / 2;
        Ellipse ellipse = new Ellipse(
                leftBottomPoint.getX() + radiusX,
                leftBottomPoint.getY() + radiusY,
                Math.abs(radiusX),
                Math.abs(radiusY)
        );
        ellipse.setStrokeWidth(1);
        ellipse.setStroke(Color.BLACK);
        ellipse.setFill(Color.TRANSPARENT);
        return ellipse;

    }

}

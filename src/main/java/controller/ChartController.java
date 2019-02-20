package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.util.Pair;
import lombok.Getter;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import utils.Point;
import utils.manager.SolutionFindingManager;

import java.util.List;

public class ChartController {
    @FXML
    private AnchorPane layout;

    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Ellipse ellipse;
    private List<Point> points;
    @Getter
    private LineChart<Number, Number> lineChart;

    /**
     * Первичная инициализация данных окна
     */
    void initialize(Ellipse ellipse, List<Point> points) {
        this.ellipse = ellipse;
        this.points = points;
        ellipse.setMouseTransparent(true);
        initLineChart();
        addEllipseIntoChart();
        layout.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                addPointsIntoChart(points);
            }
        });
    }

    /**
     * Инициализация параметров полотна графика
     */
    private void initLineChart() {
        long boundsValue = getBoundsValue();
        this.xAxis = new NumberAxis(-boundsValue, boundsValue, 1);
        this.yAxis = new NumberAxis(-boundsValue, boundsValue, 1);
        this.lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        AnchorPane.setLeftAnchor(lineChart, 0d);
        AnchorPane.setRightAnchor(lineChart, 0d);
        AnchorPane.setBottomAnchor(lineChart, 0d);
        AnchorPane.setTopAnchor(lineChart, 0d);
        lineChart.setTitle("График");
        layout.getChildren().add(lineChart);

        ChartPanManager panner = new ChartPanManager(lineChart);
        //while pressing the left mouse button, you can drag to navigate
        panner.setMouseFilter(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            } else {
                mouseEvent.consume();
            }
        });
        panner.start();

        //holding the right mouse button will draw a rectangle to zoom to desired location
        JFXChartUtil.setupZooming(lineChart, mouseEvent -> {
            if (mouseEvent.getButton() != MouseButton.SECONDARY) {
                mouseEvent.consume();
            }
        });
    }

    /**
     * Рассчитать значение для установки начального масштаба полотна графика
     */
    private long getBoundsValue() {
        ObservableList<Point> allPoints = FXCollections.observableArrayList(points);
        //добавляем крайние точки эллипса в allPoints
        allPoints.addAll(
                new Point(ellipse.getCenterX() - ellipse.getRadiusX(), ellipse.getCenterY()),
                new Point(ellipse.getCenterX() + ellipse.getRadiusX(), ellipse.getCenterY()),
                new Point(ellipse.getCenterX(), ellipse.getCenterY() - ellipse.getRadiusY()),
                new Point(ellipse.getCenterX(), ellipse.getCenterY() + ellipse.getRadiusY())
        );
        double maxAbsValue = 0;
        //находим маскимальную абсолютную координату
        for (Point point : allPoints) {
            maxAbsValue = Math.abs(point.getX()) > maxAbsValue ? Math.abs(point.getX()) : maxAbsValue;
            maxAbsValue = Math.abs(point.getY()) > maxAbsValue ? Math.abs(point.getY()) : maxAbsValue;
        }
        return Math.round(Math.ceil(maxAbsValue)) + 1;
    }


    private void addPointsIntoChart(List<Point> points) {
        for (Point point : points) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            XYChart.Data<Number, Number> data = new XYChart.Data<>(point.getX(), point.getY());
            Circle circle = new Circle(4, Color.WHITE);
            circle.setStroke(Color.THISTLE);
            circle.setStrokeWidth(2);
            data.setNode(circle);
            series.getData().add(data);
            lineChart.getData().add(series);
        }
        selectSolutionOnChart();
    }

    /**
     * Выделение решения на графике
     */
    @SuppressWarnings("unchecked")
    private void selectSolutionOnChart() {
        Pair<Pair<Point, Point>, Pair<Point, Point>> solution =
                SolutionFindingManager.getSolution(points, ellipse);
        Pair<Point, Point> segment = solution.getKey();
        Pair<Point, Point> solvePoints = solution.getValue();

        if (segment == null) {
            return;
        }

        //создаем серию из двух точек(то есть линию), являющихся точками решения
        XYChart.Series<Number, Number> solvePointsSeries = new XYChart.Series<>();
        solvePointsSeries.getData().addAll(
                new XYChart.Data<>(solvePoints.getKey().getX(), solvePoints.getKey().getY()),
                new XYChart.Data<>(solvePoints.getValue().getX(), solvePoints.getValue().getY())
        );

        //создаем серию из двух точек(то есть линию), являющихся точками пересечения эллипса
        XYChart.Series<Number, Number> segmentSeries = new XYChart.Series<>();
        segmentSeries.getData().addAll(
                new XYChart.Data<>(segment.getKey().getX(), segment.getKey().getY()),
                new XYChart.Data<>(segment.getValue().getX(), segment.getValue().getY())
        );

        //добавляем эти линии на график
        lineChart.getData().addAll(solvePointsSeries, segmentSeries);
    }



    private void addEllipseIntoChart() {
        double initialRadiusX = ellipse.getRadiusX();
        double initialRadiusY = ellipse.getRadiusY();

        XYChart.Series<Number, Number> ellipseSeries = new XYChart.Series<>();
        XYChart.Data<Number, Number> ellipseCenter =
                new XYChart.Data<>(ellipse.getCenterX(), ellipse.getCenterY());
        ellipseCenter.setNode(ellipse);
        ellipseSeries.getData().add(ellipseCenter);
        lineChart.getData().add(ellipseSeries);

        //обновляем радиус эллипса при изменении масштаба или перемещении графика
        xAxis.lowerBoundProperty().addListener((observable) -> refreshEllipseRadiusX(initialRadiusX));
        xAxis.upperBoundProperty().addListener((observable) -> refreshEllipseRadiusX(initialRadiusX));
        xAxis.animatedProperty().addListener((observable) -> refreshEllipseRadiusX(initialRadiusX));

        //обновляем радиус эллипса при изменении масштаба или перемещении графика
        yAxis.animatedProperty().addListener((observable) -> refreshEllipseRadiusY(initialRadiusY));
        yAxis.lowerBoundProperty().addListener((observable) -> refreshEllipseRadiusY(initialRadiusY));
        yAxis.upperBoundProperty().addListener((observable) -> refreshEllipseRadiusY(initialRadiusY));
    }

    void refreshEllipseRadiusX(double initialRadiusX) {
        ellipse.setRadiusX(
                initialRadiusX * xAxis.getWidth() / (xAxis.getUpperBound() - xAxis.getLowerBound())
        );
    }

    void refreshEllipseRadiusY(double initialRadiusY) {
        ellipse.setRadiusY(
                initialRadiusY * yAxis.getHeight() / (yAxis.getUpperBound() - yAxis.getLowerBound())
        );
    }
}

package utils.manager;

import javafx.scene.shape.Ellipse;
import javafx.util.Pair;
import utils.Point;

import java.util.List;

public class SolutionFindingManager {
    private static Ellipse ellipse;

    /**
     * Метод, возвращающий решение поставленной задачи
     */
    public static Pair<Pair<Point, Point>, Pair<Point, Point>>
    getSolution(List<Point> points, Ellipse ellipse) {
        SolutionFindingManager.ellipse = ellipse;
        Pair<Point, Point> maxSegment = null;
        Pair<Point, Point> solvePoints = null;
        double maxLength = 0;

        //Перебор всех возможных комбинаций точек
        for (int i = 0; i < points.size() + 1; i++) {
            for (int j = i + 1; j < points.size(); j++) {
                Point p1 = points.get(i);
                Point p2 = points.get(j);
                double length; //длина прямой от двух точек р1 и р2 в эллипсе
                Pair<Point, Point> ellipseIntersect = computeEllipseIntersect(p1, p2);
                if (ellipseIntersect == null) {
                    continue;
                }
                length = ellipseIntersect.getKey().distance(ellipseIntersect.getValue());
                System.out.println("Points: " + p1 + " " + p2);
                System.out.println("Intersection points: " +
                        ellipseIntersect.getKey() + " "
                        + ellipseIntersect.getValue() + "\nlength: " + length + "\n");
                if (length > maxLength) {
                    solvePoints = new Pair<>(p1, p2);
                    maxSegment = ellipseIntersect;
                    maxLength = length;
                }
            }
        }
        return new Pair<>(maxSegment, solvePoints);
    }

    /**
     * Вычисляет пересечени эллипса прямой(не отрезка!!!) от двух точек р1 и р2.
     * О принадлежности отрезка отрезку от двух точек вычисляется после использования этого метода(выше)
     *
     * @param p1 - первая точка
     * @param p2 - вторая точка
     * @return - пара точек - пересечение прямой из двух входных точках и эллипса.
     * Возвращает null, если двух точек найдено не было(нет пересечения)
     */
    private static Pair<Point, Point> computeEllipseIntersect(Point p1, Point p2) {
        double radiusX = ellipse.getRadiusX();
        double radiusY = ellipse.getRadiusY();
        double centerX = ellipse.getCenterX();
        double centerY = ellipse.getCenterY();
        double a, b, c; // Эти три числа - коэффициенты квадратичного уравнения, получаемого на основе решение системы:
        //первое уравнение - уравнение эллипса, второе - уравнение прямой, образованного от точек p1 и p2
        Point resPoint1 = null;
        Point resPoint2 = null;
        boolean success = false;

        //Коэффициенты имеют разный вид в зависимости от расположения точек
        if (p1.getX() == p2.getX()) { //если отрезок от двух точек параллелен оси Oy
            double d = Math.pow(p1.getX() - centerX, 2) * Math.pow(radiusY, 2) / Math.pow(radiusX, 2);
            a = 1;
            b = -2 * centerY;
            c = Math.pow(centerY, 2) + d - Math.pow(radiusY, 2);
            Pair<Double, Double> ySolution = solveQuadraticEquation(a, b, c);
            if (ySolution.getKey() != null) {
                resPoint1 = new Point(p1.getX(), ySolution.getKey());
                resPoint2 = new Point(p1.getX(), ySolution.getValue());
                success = true;
            }
        } else if (p1.getY() == p2.getY()) {//если отрезок от двух точек параллелен оси Ox
            double d = Math.pow(p1.getY() - centerY, 2) * Math.pow(radiusX, 2) / Math.pow(radiusY, 2);
            a = 1;
            b = -2 * centerX;
            c = Math.pow(centerX, 2) + d - Math.pow(radiusX, 2);
            Pair<Double, Double> xSolution = solveQuadraticEquation(a, b, c);
            if (xSolution.getKey() != null) {
                resPoint1 = new Point(xSolution.getKey(), p1.getY());
                resPoint2 = new Point(xSolution.getValue(), p1.getY());
                success = true;
            }
        } else {//если отрезок от двух точек не параллелен осям
            double k = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
            double l = (p1.getY() * p2.getX() - p1.getX() * p2.getY()) / (p2.getX() - p1.getX());
            a = Math.pow(radiusY, 2) + Math.pow(radiusX * k, 2);
            b = -2 * Math.pow(radiusY, 2) * centerX + 2 * Math.pow(radiusX, 2) * k * l
                    - 2 * Math.pow(radiusX, 2) * k * centerY;
            c = Math.pow(radiusY * centerX, 2) + Math.pow(radiusX * centerY, 2)
                    - 2 * Math.pow(radiusX, 2) * l * centerY + Math.pow(radiusX * l, 2)
                    - Math.pow(radiusX * radiusY, 2);
            Pair<Double, Double> xSolution = solveQuadraticEquation(a, b, c);
            if (xSolution.getKey() != null) {
                resPoint1 = new Point(xSolution.getKey(), xSolution.getKey() * k + l);
                resPoint2 = new Point(xSolution.getValue(), xSolution.getValue() * k + l);
                success = true;
            }
        }

        return success ? new Pair<>(resPoint1, resPoint2) : null;
    }

    /**
     * Решение квадратного уравние
     *
     * @return - возвращает пару точек - решение. Возвращает null, если решение только одно
     * или не существует в области действительных чисел
     */
    private static Pair<Double, Double> solveQuadraticEquation(double a, double b, double c) {
        Double x1 = null;
        Double x2 = null;
        double discriminant = b * b - 4 * a * c;
        if (discriminant > 0) {
            x1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            x2 = (-b - Math.sqrt(discriminant)) / (2 * a);
        }

        return new Pair<>(x1, x2);
    }
}

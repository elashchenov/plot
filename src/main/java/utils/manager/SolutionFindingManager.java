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
                Pair<Point, Point> segment; //крайние точки отрезка прямой в эллипсе
                Pair<Point, Point> ellipseIntersect = computeEllipseIntersect(p1, p2);
                if (ellipseIntersect == null) {
                    continue;
                }
                //В зависимости от расположения точек - в эллипсе или нет, меняется способ определения отрезка
                //который лежит в эллипсе и на прямой от двух точек р1 и р2
                if (!isInEllipse(p1) && !isInEllipse(p2)) {//Если обе точки не в эллипсе
                    //Точки пересечения эллипса прямой могут не лежат на отрезке р1 и р2 - тогда пропускаем
                    if (!isPointLiesOnSegment(p1, p2, ellipseIntersect.getKey())
                            || !isPointLiesOnSegment(p1, p2, ellipseIntersect.getValue())) {
                        continue;
                    }
                    segment = ellipseIntersect;
                } else if (isInEllipse(p1) && isInEllipse(p2)) {//если обе точки в эллипсе
                    segment = new Pair<>(p1, p2);
                } else {//если одна из точек р1 или р2 не в эллипсе
                    Point pointInEllipse = isInEllipse(p1) ? p1 : p2;
                    //определяем какая из точек пересечения с эллипсом лежит на отрезке p1 и p2
                    segment = isPointLiesOnSegment(p1, p2, ellipseIntersect.getKey())
                            ? new Pair<>(ellipseIntersect.getKey(), pointInEllipse)
                            : new Pair<>(pointInEllipse, ellipseIntersect.getValue());
                }
                length = segment.getKey().distance(segment.getValue());
                System.out.println(p1 + " " + p2);
                System.out.println(segment.getKey() + " " + segment.getValue() + "\nlength: " + length + "\n");
                if (length > maxLength) {
                    solvePoints = new Pair<>(p1, p2);
                    maxSegment = segment;
                    maxLength = length;
                }
            }
        }
        return new Pair<>(maxSegment, solvePoints);
    }

    /**
     * Вычисляет пересечени эллипса прямой(не отрезка!!!) от двух точек р1 и р2.
     *      О принадлежности отрезка отрезку от двух точек вычисляется после использования этого метода(выше)
     * @param p1 - первая точка
     * @param p2 - вторая точка
     * @return - пара точек - пересечение прямой из двух входных точках и эллипса.
     *      Возвращает null, если двух точек найдено не было(нет пересечения)
     */
    private static Pair<Point, Point> computeEllipseIntersect(Point p1, Point p2) {
        double radiusX = ellipse.getRadiusX();
        double radiusY = ellipse.getRadiusY();
        double centerX = ellipse.getCenterX();
        double centerY = ellipse.getCenterY();
        double a, k, c; // Эти три числа - коэффициенты квадратичного уравнения, получаемого на основе решение системы:
        //первое уравнение - уравнение эллипса, второе - уравнение прямой, образованного от точек p1 и p2
        Point resPoint1 = null;
        Point resPoint2 = null;
        boolean success = false;

        //Коэффициенты имеют разный вид в зависимости от расположения точек
        if (p1.getX() == p2.getX()) { //если отрезок от двух точек параллелен оси Oy
            double d = Math.pow(p1.getX() - centerX, 2) * Math.pow(radiusY, 2) / Math.pow(radiusX, 2);
            a = 1;
            k = -centerY;
            c = Math.pow(centerY, 2) + d - Math.pow(radiusY, 2);
            Pair<Double, Double> ySolution = solveQuadraticEquation(a, k, c);
            if (ySolution.getKey() != null) {
                resPoint1 = new Point(p1.getX(), ySolution.getKey());
                resPoint2 = new Point(p1.getX(), ySolution.getValue());
                success = true;
            }
        } else if (p1.getY() == p2.getY()) {//если отрезок от двух точек параллелен оси Ox
            double d = Math.pow(p1.getY() - centerY, 2) * Math.pow(radiusX, 2) / Math.pow(radiusY, 2);
            a = 1;
            k = -centerX;
            c = Math.pow(centerX, 2) + d - Math.pow(radiusX, 2);
            Pair<Double, Double> xSolution = solveQuadraticEquation(a, k, c);
            if (xSolution.getKey() != null) {
                resPoint1 = new Point(xSolution.getKey(), p1.getY());
                resPoint2 = new Point(xSolution.getValue(), p1.getY());
                success = true;
            }
        } else {//если отрезок от двух точек не параллелен осям
            double b = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
            double d = p1.getY() - p1.getX() * b;
            a = 1 / Math.pow(radiusX, 2) + Math.pow(b / radiusY, 2);
            k = b * (d - centerY) / Math.pow(radiusY, 2) - centerX / Math.pow(radiusX, 2);
            c = Math.pow(centerX * radiusX, 2) + Math.pow((d - centerY) / radiusY, 2) - 1;
            Pair<Double, Double> xSolution = solveQuadraticEquation(a, k, c);
            if (xSolution.getKey() != null) {
                resPoint1 = new Point(xSolution.getKey(), xSolution.getKey() * b + d);
                resPoint2 = new Point(xSolution.getValue(), xSolution.getValue() * b + d);
                success = true;
            }
        }

        return success ? new Pair<>(resPoint1, resPoint2) : null;
    }

    /**
     * Решение квадратного уравние
     * @return - возвращает пару точек - решение. Возвращает null, если решение только одно
     *      или не существует в области действительных чисел
     */
    private static Pair<Double, Double> solveQuadraticEquation(double a, double k, double c) {
        Double x1 = null;
        Double x2 = null;
        double discriminant = k * k - a * c;
        if (discriminant > 0) {
            x1 = (-k + Math.sqrt(discriminant)) / a;
            x2 = (-k - Math.sqrt(discriminant)) / a;
        }

        return new Pair<>(x1, x2);
    }

    /**
     * Определяет лежит ли точка в отрезке.
     * @param a - первая точка отрезка
     * @param b - вторая точка отрезка
     * @param p - проверяемая точка
     */
    private static boolean isPointLiesOnSegment(Point a, Point b, Point p) {
        //Определяем нижнюю и верхнюю точки отрезка
        Point leftBottomPoint = (a.getX() <= b.getX()) && (a.getY() <= b.getY()) ? a : b;
        Point rightTopPoint = leftBottomPoint == a ? b : a;

        return (leftBottomPoint.getX() <= p.getX()) && (leftBottomPoint.getY() <= p.getY())
                && (rightTopPoint.getX() >= p.getX()) && (rightTopPoint.getY() >= p.getY());
    }

    /**
     * Определить лежит ли точка в эллипсе
     */
    private static boolean isInEllipse(Point point) {
        return (Math.pow((point.getX() - ellipse.getCenterX()) / ellipse.getRadiusX(), 2)
                + Math.pow((point.getY() - ellipse.getCenterY()) / ellipse.getRadiusY(), 2)) <= 1;
    }
}

package com.hobot.sample.app.view;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * 形状抽象类
 * <p>
 * Created by lgw on 2017/8/31.
 */
public abstract class Shape {

    public int color;

    public static Line line(PointF startPoint, PointF endPoint, int color) {
        return new Line(startPoint.x, startPoint.y, endPoint.x, endPoint.y, color);
    }

    public static Line line(float startX, float startY, float endX, float endY, int color) {
        return new Line(startX, startY, endX, endY, color);
    }

    public static Rect rect(float left, float top, float right, float bottom, int color) {
        return new Rect(left, top, right, bottom, color);
    }

    public static Circle circle(float x, float y, float r, int color) {
        return new Circle(x, y, r, color);
    }

    public static Text text(String text, float x, float y, int color) {
        return new Text(text, x, y, color);
    }

    public static Text text(String text, float x, float y, int color, float textSize) {
        return new Text(text, x, y, color, textSize);
    }

    /**
     * 绘制圆
     */
    public static class Circle extends Shape {

        public float x;
        public float y;
        public float r;

        public Circle(float x, float y, float r, int color) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.color = color;
        }
    }

    /**
     * 绘制线
     */
    public static class Line extends Shape {

        public float startX, startY, endX, endY;

        public Line(float startX, float startY, float endX, float endY, int color) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.color = color;
        }

        @Override
        public String toString() {
            return "Line{" +
                    "startX=" + startX +
                    ", startY=" + startY +
                    ", endX=" + endX +
                    ", endY=" + endY +
                    '}';
        }
    }

    /**
     * 绘制矩形
     */
    public static class Rect extends Shape {

        public float left;
        public float top;
        public float right;
        public float bottom;
        private RectF rectF; // lazy object cache

        public Rect(float left, float top, float right, float bottom, int color) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.color = color;
        }

        public RectF rectf() {
            if (this.rectF == null) {
                this.rectF = new RectF(left, top, right, bottom);
            } else {
                this.rectF.left = left;
                this.rectF.top = top;
                this.rectF.right = right;
                this.rectF.bottom = bottom;
            }
            return this.rectF;
        }

        @Override
        public String toString() {
            return rectf() + "";
        }
    }

    /**
     * 绘制文字
     */
    public static class Text extends Shape {

        public String text;
        public float x;
        public float y;
        // public  font;
        public float textSize;

        public Text(String text, float x, float y, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public Text(String text, float x, float y, int color, float textSize) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
            this.textSize = textSize;
        }

    }
}



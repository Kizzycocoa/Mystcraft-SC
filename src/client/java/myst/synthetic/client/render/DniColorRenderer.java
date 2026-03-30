package myst.synthetic.client.render;

import net.minecraft.client.gui.GuiGraphics;

public final class DniColorRenderer {

    private DniColorRenderer() {
    }

    public static void render(GuiGraphics guiGraphics, int rgb, int alpha, int centerX, int centerY, int radius) {
        if (alpha <= 0) {
            return;
        }

        float[] hsb = rgbToHsb(rgb);
        int hueDegrees = (int)(hsb[0] * 360.0F);

        double eyelidRadius = radius;
        double eyeRadius = (1.0D + (1.0D - hsb[1]) * 3.0D) * radius / 20.0D;

        boolean outerEyelid = false;
        boolean innerEyelid = false;
        boolean eye = false;

        if (hsb[1] > 0.3F && hsb[2] > 0.5F) {
            if (hueDegrees <= 28 || hueDegrees > 274) {
                int temp = (hueDegrees + 85) % 360;
                eyelidRadius = Math.abs(cosQuarter(temp / 114.0D) * radius);
                outerEyelid = true;
                innerEyelid = true;
            } else if (hueDegrees <= 114) {
                int temp = hueDegrees - 30;
                outerEyelid = true;
                eyelidRadius = Math.abs(sinQuarter(temp / 86.0D) * radius);
                eye = true;
            } else if (hueDegrees <= 274) {
                int temp = hueDegrees - 114;
                innerEyelid = true;
                eyelidRadius = Math.abs(cosQuarter(temp / 160.0D) * radius);
                eye = true;
            }
        } else {
            float mod = Math.min(hsb[1], hsb[2]) / 0.3F;
            eyeRadius *= hsb[2] / 0.3F;

            if (hueDegrees <= 28 || hueDegrees > 274) {
                int temp = (hueDegrees + 85) % 360;
                if (mod < 0.3F) {
                    eyelidRadius = Math.abs(Math.max(cosQuarter(temp / 114.0D), 1.0D - mod / 0.3D) * radius);
                    eye = true;
                } else {
                    eyelidRadius = Math.abs(Math.min(cosQuarter(temp / 114.0D), (mod - 0.3D) / 0.7D) * radius);
                    innerEyelid = true;
                }
                outerEyelid = true;
            } else if (hueDegrees <= 114) {
                int temp = hueDegrees - 30;
                outerEyelid = true;
                eyelidRadius = Math.abs(Math.max(cosQuarter(temp / 86.0D), 1.0D - mod) * radius);
                eye = true;
            } else if (hueDegrees <= 274) {
                int temp = hueDegrees - 114;
                innerEyelid = true;
                eyelidRadius = Math.abs(Math.max(cosQuarter(temp / 160.0D), 1.0D - mod) * radius);
                eye = true;
            }
        }

        if (eyeRadius > eyelidRadius) {
            eyeRadius = eyelidRadius;
        }

        eyelidRadius *= 4.0D / 3.0D;

        int lineColor = ((clamp255(alpha)) << 24) | (rgb & 0xFFFFFF);
        int thickness = 2;
        int max = 20;

        Point top = new Point(centerX, centerY - radius);
        Point bottom = new Point(centerX, centerY + radius);
        Point left = new Point(centerX - radius, centerY);
        Point right = new Point(centerX + radius, centerY);

        if (eye) {
            drawCircle(guiGraphics, centerX, centerY, eyeRadius, lineColor, thickness);
        }

        if (outerEyelid) {
            Point vec1 = new Point(centerX - radius * 0.95D, centerY - eyelidRadius);
            Point vec2 = new Point(centerX + radius * 0.95D, centerY - eyelidRadius);

            drawBezier(guiGraphics, left, vec1, vec2, right, max, lineColor, thickness);
            top = bezier(left, vec1, vec2, right, 0.5D);

            vec1 = new Point(centerX - radius * 0.95D, centerY + eyelidRadius);
            vec2 = new Point(centerX + radius * 0.95D, centerY + eyelidRadius);

            drawBezier(guiGraphics, left, vec1, vec2, right, max, lineColor, thickness);
            bottom = bezier(left, vec1, vec2, right, 0.5D);
        }

        if (innerEyelid && !outerEyelid) {
            Point vec1 = new Point(top.x - eyelidRadius, centerY - radius * 0.95D);
            Point vec2 = new Point(bottom.x - eyelidRadius, centerY + radius * 0.95D);
            drawBezier(guiGraphics, top, vec1, vec2, bottom, max, lineColor, thickness);

            vec1 = new Point(top.x + eyelidRadius, centerY - radius * 0.95D);
            vec2 = new Point(bottom.x + eyelidRadius, centerY + radius * 0.95D);
            drawBezier(guiGraphics, top, vec1, vec2, bottom, max, lineColor, thickness);
        } else if (innerEyelid) {
            drawLine(guiGraphics, top.x, top.y, bottom.x, bottom.y, lineColor, thickness);
        }

        drawCircle(guiGraphics, centerX, centerY, radius, lineColor, thickness);
    }

    private static void drawBezier(
            GuiGraphics guiGraphics,
            Point p0,
            Point p1,
            Point p2,
            Point p3,
            int steps,
            int color,
            int thickness
    ) {
        Point previous = p0;
        for (int i = 1; i <= steps; i++) {
            double t = (double)i / (double)steps;
            Point current = bezier(p0, p1, p2, p3, t);
            drawLine(guiGraphics, previous.x, previous.y, current.x, current.y, color, thickness);
            previous = current;
        }
    }

    private static Point bezier(Point p0, Point p1, Point p2, Point p3, double t) {
        double inv = 1.0D - t;
        double x = inv * inv * inv * p0.x
                + 3.0D * inv * inv * t * p1.x
                + 3.0D * inv * t * t * p2.x
                + t * t * t * p3.x;
        double y = inv * inv * inv * p0.y
                + 3.0D * inv * inv * t * p1.y
                + 3.0D * inv * t * t * p2.y
                + t * t * t * p3.y;
        return new Point(x, y);
    }

    private static void drawCircle(GuiGraphics guiGraphics, int centerX, int centerY, double radius, int color, int thickness) {
        int steps = 40;
        Point previous = null;

        for (int i = 0; i <= steps; i++) {
            double angle = (Math.PI * 2.0D * i) / steps;
            Point current = new Point(
                    centerX + Math.cos(angle) * radius,
                    centerY + Math.sin(angle) * radius
            );

            if (previous != null) {
                drawLine(guiGraphics, previous.x, previous.y, current.x, current.y, color, thickness);
            }

            previous = current;
        }
    }

    private static void drawLine(
            GuiGraphics guiGraphics,
            double x1,
            double y1,
            double x2,
            double y2,
            int color,
            int thickness
    ) {
        int ix1 = (int)Math.round(x1);
        int iy1 = (int)Math.round(y1);
        int ix2 = (int)Math.round(x2);
        int iy2 = (int)Math.round(y2);

        int dx = Math.abs(ix2 - ix1);
        int dy = Math.abs(iy2 - iy1);
        int sx = ix1 < ix2 ? 1 : -1;
        int sy = iy1 < iy2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawBrush(guiGraphics, ix1, iy1, color, thickness);

            if (ix1 == ix2 && iy1 == iy2) {
                break;
            }

            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                ix1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                iy1 += sy;
            }
        }
    }

    private static void drawBrush(GuiGraphics guiGraphics, int x, int y, int color, int thickness) {
        int r = Math.max(0, thickness - 1);
        guiGraphics.fill(x - r, y - r, x + r + 1, y + r + 1, color);
    }

    private static double sinQuarter(double value) {
        return Math.sin(value * (Math.PI / 2.0D));
    }

    private static double cosQuarter(double value) {
        return Math.cos(value * (Math.PI / 2.0D));
    }

    private static float[] rgbToHsb(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue;
        if (delta == 0.0F) {
            hue = 0.0F;
        } else if (max == r) {
            hue = ((g - b) / delta) % 6.0F;
        } else if (max == g) {
            hue = ((b - r) / delta) + 2.0F;
        } else {
            hue = ((r - g) / delta) + 4.0F;
        }
        hue /= 6.0F;
        if (hue < 0.0F) {
            hue += 1.0F;
        }

        float saturation = max == 0.0F ? 0.0F : delta / max;
        float brightness = max;

        return new float[] { hue, saturation, brightness };
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private record Point(double x, double y) {
    }
}
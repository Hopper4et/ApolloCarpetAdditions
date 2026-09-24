package Hopper4et.apollocarpetadditions.utils;

public class MathUtil {
    public static long lfloor(double value) {
        long l = (long)value;
        return value < (double)l ? l - 1L : l;
    }

    public static int floor(double value) {
        int i = (int)value;
        return value < (double)i ? i - 1 : i;
    }
    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static int getSectionCoordFloored(double coord) {
        return floor(coord) >> 4;
    }

    public static int sign(double value) {
        if (value == (double)0.0F) {
            return 0;
        } else {
            return value > (double)0.0F ? 1 : -1;
        }
    }
}

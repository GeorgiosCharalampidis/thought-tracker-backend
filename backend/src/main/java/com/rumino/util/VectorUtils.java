package com.rumino.util;

public class VectorUtils {
    public static float dot(float[] a, float[] b) {
        float s = 0f;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    public static float norm(float[] a) {
        float s = 0f;
        for (float v : a) s += v * v;
        return (float) Math.sqrt(s);
    }

    public static float cosine(float[] a, float[] b) {
        float n = norm(a) * norm(b);
        if (n == 0f) return 0f;
        return dot(a, b) / n;
    }

    public static float[] add(float[] a, float[] b) {
        float[] r = new float[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] + b[i];
        return r;
    }

}



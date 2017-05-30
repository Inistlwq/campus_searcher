package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;

public class Tool {
    public static String[] fields = { "title", "h", "anchorIn", "content",
            "anchorOut" };
    public static float[] weight = { 1.0f, 0.25f, 0.2f, 0.2f, 0.05f };
    public static float[] avgLength = new float[5];
    public static Map<String, Float> weightMap = new HashMap<String, Float>();
    public static Map<String, Float> avgLengthMap = new HashMap<String, Float>();

    static {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("forIndex/global.txt")));
            for (int i = 0; i < fields.length; ++i) {
                avgLength[i] = Float.valueOf(reader.readLine());
                weightMap.put(fields[i], weight[i]);
                avgLengthMap.put(fields[i], avgLength[i]);
            }
            reader.close();
        } catch (Exception e) {
        }
    }
}

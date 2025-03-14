package xyz.joseyamut.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class ClickedStackArea {

    public static boolean updateMapIfActionIsValid(String key, Map<String, FixedStack> stackMap,
                                                int pointX, int pointY,
                                                ArrayList<Rectangle> areas,
                                                ArrayList<FixedStack> stacks) {
        int i = 0;
        for (Rectangle area : areas) {
            if (area.contains(pointX, pointY)) {
                stackMap.put(key, stacks.get(i));
                return true;
            }
            i++;
        }
        return false;
    }

}

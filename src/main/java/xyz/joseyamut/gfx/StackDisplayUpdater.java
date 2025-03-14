package xyz.joseyamut.gfx;

import xyz.joseyamut.util.FixedStack;

import java.awt.*;

public class StackDisplayUpdater {

    public static Rectangle draw(Graphics g,
                                 FixedStack stack,
                                 int elements,
                                 int xPointPole) {
        if (!stack.isEmpty()) {
            int diskPointX;
            int diskPointY;
            int diskWidth;
            int[] disks = new int[elements];
            int stackSize = stack.size();
            FixedStack shiftStack = new FixedStack(stackSize);

            for (int i = 0; i < stackSize; i++) {
                shiftStack.push(((stack.pop())));
            }

            for (int j = stackSize; j > 0; j--) {
                stack.push(shiftStack.top());
                disks[j] = shiftStack.pop();
                diskPointX = xPointPole - (disks[j] * 10);
                diskPointY = 240 - (stack.size() * Stage.diskHeight);
                diskWidth = disks[j] * Stage.diskHeight;

                g.setColor(Color.DARK_GRAY);
                g.drawRoundRect(diskPointX, diskPointY,
                        diskWidth, Stage.diskHeight,
                        Stage.diskArc, Stage.diskArc);
                g.setColor(getDiskColor(j));
                g.fillRoundRect(diskPointX, diskPointY,
                        diskWidth, Stage.diskHeight,
                        Stage.diskArc, Stage.diskArc);
            }
        }
        return setArea(g, xPointPole, elements);
    }

    private static Rectangle setArea(Graphics g, int xPointPole,
                                     int elements) {
        int widestDiskWidth = (elements - 1) * Stage.diskHeight;
        int xPointArea = xPointPole - (widestDiskWidth / 2);
        Rectangle stackArea = new Rectangle(xPointArea, yPointPole(elements, Stage.diskHeight),
                widestDiskWidth, elements * Stage.diskHeight);
        g.setColor(Color.BLACK);
        g.drawRect(stackArea.x, stackArea.y, stackArea.width, stackArea.height);
        return stackArea;
    }

    public static int yPointPole(int elements, int diskHeight) {
        return switch (elements - 1) {
            case 8 -> (elements - 6) * diskHeight;
            case 7 -> (elements - 4) * diskHeight;
            case 6 -> (elements - 2) * diskHeight;
            case 5 -> elements * diskHeight;
            default -> (elements + 2) * diskHeight; // 4
        };
    }

    private static Color getDiskColor(int diskWeight) {
        int modifier = (diskWeight + 1) * 14;
        return new Color(0,255 - modifier,0);
    }
}

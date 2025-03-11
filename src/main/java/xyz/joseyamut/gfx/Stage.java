package xyz.joseyamut.gfx;

import lombok.extern.slf4j.Slf4j;
import xyz.joseyamut.util.ClickedStackArea;
import xyz.joseyamut.util.FixedStack;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Stage extends Backstage {

    private int elements;
    // Stacks
    private FixedStack initialStack; // Stack at X location
    private FixedStack yStack;
    private FixedStack zStack;
    private FixedStack shiftStack1;
    private FixedStack shiftStack2;
    private FixedStack shiftStack3;
    // Mouse listener, stack clickable areas
    private MouseListener mouseListener;
    private Rectangle xStackArea;
    private Rectangle yStackArea;
    private Rectangle zStackArea;
    // Status text and duration
    private String status;
    private boolean started;
    private long timeStarted;

    // Disks, poles and base
    private final int diskHeight = 20;
    private final int xPointPoleX = 95;
    private final int xPointPoleY = 295;
    private final int xPointPoleZ = 495;
    // Mouse clicked stack container
    private final String SRC_STACK_KEY = "SRC";
    private final String DST_STACK_KEY = "DST";

    public Stage(FixedStack initialStack) {
        this.initialStack = initialStack;

        status = "";
        started = false;
        timeStarted = 0;
        elements = initialStack.size();

        yStack = new FixedStack(elements);
        zStack = new FixedStack(elements);
        shiftStack1 = new FixedStack(elements);
        shiftStack2 = new FixedStack(elements);
        shiftStack3 = new FixedStack(elements);

        elements += 1;

        setMouseListener();
    }

    private void setMouseListener() {
        Map<String, FixedStack> stackMap = new HashMap<>();
        stackMap.put(SRC_STACK_KEY, null);
        stackMap.put(DST_STACK_KEY, null);

        mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                ClickedStackArea.updateMapIfActionIsValid(SRC_STACK_KEY, stackMap,
                        e.getX(), e.getY(),
                        new ArrayList<>(Arrays.asList(xStackArea, yStackArea, zStackArea)),
                        new ArrayList<>(Arrays.asList(initialStack, yStack, zStack)));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                ClickedStackArea.updateMapIfActionIsValid(DST_STACK_KEY, stackMap,
                        e.getX(), e.getY(),
                        new ArrayList<>(Arrays.asList(xStackArea, yStackArea, zStackArea)),
                        new ArrayList<>(Arrays.asList(initialStack, yStack, zStack)));
                moveDisk(stackMap);
                repaint();
            }
        };

        addMouseListener(mouseListener);
    }

    private void moveDisk(Map<String, FixedStack> stackMap) {
        String validMoveMsg = "Valid move...";
        String invalidMoveMsg = "A bigger disk cannot be placed on top of a smaller one!";

        if (stackMap.get(SRC_STACK_KEY) != null
                && stackMap.get(DST_STACK_KEY) != null) {
            try {
                if (!stackMap.get(DST_STACK_KEY).isEmpty() &&
                        (stackMap.get(SRC_STACK_KEY).top() > stackMap.get(DST_STACK_KEY).top())) {
                    log.warn("{}", invalidMoveMsg);
                    status = invalidMoveMsg;
                    return;
                }

                stackMap.get(DST_STACK_KEY).push(stackMap.get(SRC_STACK_KEY).top());
                stackMap.get(SRC_STACK_KEY).pop();
                status = validMoveMsg;
            } catch (Exception z) {
                String emptyStackMsg = "That stack is empty.";
                log.warn("{}", emptyStackMsg);
                status = emptyStackMsg;
            }

            log.info("Stack 1: {}", initialStack.size());
            log.info("Stack 2: {}", yStack.size());
            log.info("Stack 3: {}", zStack.size());
        }
    }

    @Override
    public void graphics(Graphics g) {
        // Canvas background
        g.clearRect(0, 0, getSize().width, getSize().height);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getSize().width, getSize().height);

        drawBase();
        drawStacks();
    }

    private void drawBase() {
        // Pole x/y/z markers
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor( Color.WHITE );
        g.drawString("X", 90, 290);
        g.drawString("Y", 290, 290);
        g.drawString("Z", 490, 290);
        // Base
        g.setColor(Color.ORANGE);
        g.fillRect(10, 240, 575, 15);
        // Poles x/y/z
        g.fillRect(xPointPoleX, yPointPole(), 2, elements * diskHeight);
        g.fillRect(xPointPoleY, yPointPole(), 2, elements * diskHeight);
        g.fillRect(xPointPoleZ, yPointPole(), 2, elements * diskHeight);
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.ITALIC, 18));
        g.drawString(status, 25, 360);
    }

    private void drawStacks() {
        int xPointDisk;
        int yPointDisk;
        int diskWidth;

        int widestDiskWidth = (elements - 1) * 20;
        final int diskArcWidth = 10;
        final int diskArcHeight = 10;

        int[] diskColor = new int[elements];

        if ((initialStack.size() < elements) && !started) { // TODO: Check this!
            started = true;
            timeStarted = System.currentTimeMillis();
        }

        xStackArea = drawStackArea(xPointPoleX, widestDiskWidth);
        yStackArea = drawStackArea(xPointPoleY, widestDiskWidth);
        zStackArea = drawStackArea(xPointPoleZ, widestDiskWidth);

        if (!initialStack.isEmpty()) {
            int stackSize = initialStack.size();
            for (int i = 0; i < stackSize; i++) {
                shiftStack1.push(initialStack.pop());
            }

            stackSize = shiftStack1.size();
            for (int j = stackSize ; j > 0; j--) {
                initialStack.push(shiftStack1.top());
                diskColor[j] = shiftStack1.pop();
                xPointDisk = xPointPoleX - (diskColor[j] * 10);
                yPointDisk = 240 - (initialStack.size() * 20);
                diskWidth = diskColor[j] * diskHeight;
                g.setColor(Color.BLACK);
                g.drawRoundRect(xPointDisk, yPointDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
                g.setColor(diskColor(j));
                g.fillRoundRect(xPointDisk, yPointDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
            }
        }

        if (!yStack.isEmpty()) {
            int stackSize = yStack.size();
            for (int i = 0; i < stackSize; i++) {
                shiftStack2.push(yStack.pop());
            }

            stackSize = shiftStack2.size();
            for (int j = stackSize ; j > 0; j--) {
                yStack.push(shiftStack2.top());
                diskColor[j] = shiftStack2.pop();
                xPointDisk = xPointPoleY - (diskColor[j] * 10);
                yPointDisk = 240 - (yStack.size() * 20);
                diskWidth = diskColor[j] * diskHeight;
                g.setColor(Color.BLACK);
                g.drawRoundRect( xPointDisk, yPointDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight );
                g.setColor(diskColor(j));
                g.fillRoundRect( xPointDisk, yPointDisk, diskWidth, diskHeight, diskArcWidth,diskArcHeight );
            }
        }

        if (!zStack.isEmpty()) {
            int stackSize = zStack.size();
            for (int i = 0; i < stackSize; i++) {
                shiftStack3.push(zStack.pop());
            }

            stackSize = shiftStack3.size();
            for(int j = stackSize ; j > 0; j--) {
                zStack.push(shiftStack3.top());
                diskColor[j] = shiftStack3.pop();
                xPointDisk = xPointPoleZ - (diskColor[j] * 10);
                yPointDisk = 240 - (zStack.size() * 20);
                diskWidth = diskColor[j] * diskHeight;
                g.setColor(Color.BLACK);
                g.drawRoundRect(xPointDisk, yPointDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
                g.setColor(diskColor(j));
                g.fillRoundRect(xPointDisk, yPointDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
            }
        }

        if ((initialStack.isEmpty())
                && (yStack.isEmpty())) {
            gameOverText();
            removeMouseListener(mouseListener);
        }
    }

    private Rectangle drawStackArea(int pointX, int widestDiskWidth) {
        pointX = pointX - (widestDiskWidth / 2);
        Rectangle stackArea = new Rectangle(pointX, yPointPole(), widestDiskWidth, elements * diskHeight);
        g.setColor(Color.BLACK);
        g.drawRect(stackArea.x, stackArea.y, stackArea.width, stackArea.height);
        return stackArea;
    }

    private void gameOverText() {
        status = "";
        long timeElapsed = (System.currentTimeMillis() - timeStarted) / 1000;

        g.setFont(new Font("SansSerif", Font.BOLD+Font.ITALIC, 20));
        g.setColor(Color.YELLOW);
        g.drawString("Game Complete!", 25, 30);
        g.setFont(new Font("SansSerif", Font.ITALIC, 16));
        g.drawString("Elapsed time: " + timeElapsed + " seconds", 25, 400);
    }

    private Color diskColor(int weight) {
        int modifier = (weight + 1) * 10;
        return new Color(0,255 - modifier,0);
    }

    private int yPointPole() {
        return switch (elements - 1) {
            case 8 -> (elements - 6) * diskHeight;
            case 7 -> (elements - 4) * diskHeight;
            case 6 -> (elements - 2) * diskHeight;
            case 5 -> elements * diskHeight;
            default -> (elements + 2) * diskHeight; // 4
        };
    }

}
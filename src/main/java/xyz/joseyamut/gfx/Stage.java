package xyz.joseyamut.gfx;

import lombok.extern.slf4j.Slf4j;
import xyz.joseyamut.util.ClickedStackArea;
import xyz.joseyamut.util.FixedStack;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Stage extends Backstage {

    private int elements;
    // Stacks
    private FixedStack initialStack; // Stack at X location
    private FixedStack yStack;
    private FixedStack zStack;
    private FixedStack topDisk;
    private Map<String, FixedStack> stackMap;
    // Mouse listener, stack clickable areas
    private MouseListener mouseListener;
    private MouseMotionListener mouseMotionListener;
    private Rectangle xStackArea;
    private Rectangle yStackArea;
    private Rectangle zStackArea;
    private int xPointMouse;
    private int yPointMouse;
    private boolean mouseActionWithinBounds;
    // Status text and duration
    private String status;
    private boolean started;
    private long timeStarted;
    private long timeElapsed;
    // Disks, poles and base
    public static final int diskArc = 12;
    public static final int diskHeight = 20;
    private final int xPointPoleX = 98;
    private final int xPointPoleY = 298;
    private final int xPointPoleZ = 498;
    // Mouse clicked stack container
    private final String SRC_STACK_KEY = "SRC";
    private final String DST_STACK_KEY = "DST";

    public Stage(FixedStack initialStack) {
        this.initialStack = initialStack;

        status = "";
        started = false;
        timeStarted = 0;
        mouseActionWithinBounds = false;
        elements = initialStack.size();

        yStack = new FixedStack(elements);
        zStack = new FixedStack(elements);
        topDisk = new FixedStack(1);
        topDisk.push(initialStack.top());
        stackMap = new HashMap<>();

        elements += 1;

        setMouseListener();
        setMouseMotionListener();
    }

    private void setMouseListener() {
        stackMap.put(SRC_STACK_KEY, null);
        stackMap.put(DST_STACK_KEY, null);

        mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                setCursor(new Cursor(Cursor.HAND_CURSOR));

                mouseActionWithinBounds = ClickedStackArea.updateMapIfActionIsValid(SRC_STACK_KEY, stackMap,
                        e.getX(), e.getY(),
                        new ArrayList<>(Arrays.asList(xStackArea, yStackArea, zStackArea)),
                        new ArrayList<>(Arrays.asList(initialStack, yStack, zStack)));

                if (mouseActionWithinBounds) {
                    if (!topDisk.isEmpty()) {
                        topDisk.pop();
                    }

                    if (!stackMap.get(SRC_STACK_KEY).isEmpty()) {
                        topDisk.push(stackMap.get(SRC_STACK_KEY).top());
                        stackMap.get(SRC_STACK_KEY).pop();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                mouseActionWithinBounds = ClickedStackArea.updateMapIfActionIsValid(DST_STACK_KEY, stackMap,
                        e.getX(), e.getY(),
                        new ArrayList<>(Arrays.asList(xStackArea, yStackArea, zStackArea)),
                        new ArrayList<>(Arrays.asList(initialStack, yStack, zStack)));

                if (mouseActionWithinBounds) {
                    moveDisk(stackMap);
                } else {
                    try {
                        stackMap.get(SRC_STACK_KEY).push(topDisk.top());
                        topDisk.pop();
                    } catch (Exception x) {
                        log.error("{}", x.getMessage());
                    }
                }
            }
        };

        addMouseListener(mouseListener);
    }

    private void setMouseMotionListener() {
        mouseMotionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                resetMousePressStartPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                xPointMouse = e.getX();
                yPointMouse = e.getY();
            }
        };

        addMouseMotionListener(mouseMotionListener);
    }

    private void moveDisk(Map<String, FixedStack> stackMap) {
        String validMoveMsg = "Valid move...";
        String invalidMoveMsg = "A bigger disk cannot be placed on top of a smaller one!";

        if (stackMap.get(SRC_STACK_KEY) != null
                && stackMap.get(DST_STACK_KEY) != null) {
            try {
                if (!stackMap.get(DST_STACK_KEY).isEmpty()
                        && (topDisk.top() > stackMap.get(DST_STACK_KEY).top())) {
                    stackMap.get(SRC_STACK_KEY).push(topDisk.top());
                    topDisk.pop();
                    resetMousePressStartPoint();
                    status = invalidMoveMsg;
                    log.warn("{}", invalidMoveMsg);
                    return;
                }

                stackMap.get(DST_STACK_KEY).push(topDisk.top());
                topDisk.pop();
                resetMousePressStartPoint();
                status = validMoveMsg;
            } catch (Exception e) {
                String emptyStackMsg = "That stack is empty.";
                status = emptyStackMsg;
                log.warn("{}", emptyStackMsg);
            }

            log.info("Stack 1: {}", initialStack.size());
            log.info("Stack 2: {}", yStack.size());
            log.info("Stack 3: {}", zStack.size());
        }
    }

    private void resetMousePressStartPoint() {
        xPointMouse = yPointMouse = -10;
    }

    private int floatingDiskWidth() {
        if (!topDisk.isEmpty()) {
            return topDisk.top() * diskHeight;
        }

        if (stackMap.get(SRC_STACK_KEY) == null
                    && stackMap.get(DST_STACK_KEY) == null) {
            return initialStack.top() + diskHeight;
        }

        /*if (stackMap.get(DST_STACK_KEY) != null) {
            return stackMap.get(DST_STACK_KEY).top() * diskHeight;
        }*/

        if ((stackMap.get(SRC_STACK_KEY) != null
                || stackMap.get(DST_STACK_KEY) != null)
                && !stackMap.get(SRC_STACK_KEY).isEmpty()) {
            return stackMap.get(SRC_STACK_KEY).top() * diskHeight;
        }

        return 0;
    }

    @Override
    public void graphics(Graphics g) {
        // Canvas background
        super.graphics(g);

        drawBase();
        drawStacks();
        setStatus();

        int diskWidth = floatingDiskWidth();
        if (mouseActionWithinBounds
                && (xPointMouse >= 0 && yPointMouse >= 0)) {
            g.setColor(Color.GRAY);
            g.fillRoundRect(xPointMouse - 20, yPointMouse - 20,
                    diskWidth, diskHeight,
                    diskArc, diskArc);
        }

        elapsedTime();
        doRepaint();
    }

    private void doRepaint() {
        if (started) {
            repaint();
        }
    }

    private void drawBase() {
        // Pole x/y/z markers
        g.setFont(new Font("Courier New", Font.BOLD, 24));
        g.setColor( Color.WHITE );
        g.drawString("X", 90, 290);
        g.drawString("Y", 290, 290);
        g.drawString("Z", 490, 290);
        // Base
        g.setColor(new Color(182, 103, 19));
        g.fillRect(10, 240, 575, 15);
        // Poles x/y/z
        int y = StackDisplayUpdater.yPointPole(elements, diskHeight);
        g.fillRect(xPointPoleX, y, 2, elements * diskHeight);
        g.fillRect(xPointPoleY, y, 2, elements * diskHeight);
        g.fillRect(xPointPoleZ, y, 2, elements * diskHeight);
    }

    private void drawStacks() {
        if ((initialStack.size() < elements) && !started) {
            started = true;
            status = "Game started.";
            timeStarted = System.currentTimeMillis();
        }

        xStackArea = StackDisplayUpdater.draw(g, initialStack, elements, xPointPoleX);
        yStackArea = StackDisplayUpdater.draw(g, yStack, elements, xPointPoleY);
        zStackArea = StackDisplayUpdater.draw(g, zStack, elements, xPointPoleZ);

        timeElapsed = (System.currentTimeMillis() - timeStarted);

        if (initialStack.isEmpty()
                && yStack.isEmpty()
                && topDisk.isEmpty()) {
            started = false;
            status = "Game completed!";
            resetMousePressStartPoint();
            removeMouseListener(mouseListener);
            removeMouseMotionListener(mouseMotionListener);
        }
    }

    private void setStatus() {
        g.setColor(new Color(241, 216, 111, 255));
        g.setFont(new Font("Courier New", Font.PLAIN, 18));
        g.drawString(this.status, 25, 360);
    }

    private void elapsedTime() {
        long ss = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60;
        long mm = TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60;
        long hh = TimeUnit.MILLISECONDS.toHours(timeElapsed);
        String formattedElapsedTime = String.format("Elapsed time - %02d:%02d:%02d", hh, mm, ss);

        g.setColor(new Color(85, 85, 81, 255));
        g.setFont(new Font("Courier New", Font.BOLD, 14));
        g.drawString(formattedElapsedTime, 25, 400);
    }

}
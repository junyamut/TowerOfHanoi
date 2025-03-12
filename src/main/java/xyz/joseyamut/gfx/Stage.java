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
import java.util.concurrent.TimeUnit;

@Slf4j
public class Stage extends Backstage {
    private int elements;
    // Stacks
    private FixedStack initialStack; // Stack at X location
    private FixedStack yStack;
    private FixedStack zStack;
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
            } catch (Exception e) {
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
        setStatus();
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
        g.fillRect(xPointPoleX, StackDisplayUpdater.yPointPole(elements, diskHeight), 2, elements * diskHeight);
        g.fillRect(xPointPoleY, StackDisplayUpdater.yPointPole(elements, diskHeight), 2, elements * diskHeight);
        g.fillRect(xPointPoleZ, StackDisplayUpdater.yPointPole(elements, diskHeight), 2, elements * diskHeight);
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

        if ((initialStack.isEmpty())
                && (yStack.isEmpty())) {
            status = "Game completed!";
            elapsedTime();
            removeMouseListener(mouseListener);
        }
    }

    private void setStatus() {
        g.setColor(new Color(241, 216, 111, 255));
        g.setFont(new Font("Courier New", Font.PLAIN, 18));
        g.drawString(this.status, 25, 360);
    }

    private void elapsedTime() {
        long timeElapsed = (System.currentTimeMillis() - timeStarted);
        long ss = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60;
        long mm = TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60;
        long hh = TimeUnit.MILLISECONDS.toHours(timeElapsed);
        String formattedElapsedTime = String.format("Elapsed time - %02d:%02d:%02d", hh, mm, ss);

        g.setColor(new Color(241, 216, 111, 255));
        g.setFont(new Font("Courier New", Font.BOLD, 14));
        g.drawString(formattedElapsedTime, 25, 400);
    }

}
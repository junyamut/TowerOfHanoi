package xyz.joseyamut.gfx;

import lombok.extern.slf4j.Slf4j;
import xyz.joseyamut.util.FixedStack;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Stage extends Backstage {

    public int elements;
    // Stacks
    private FixedStack initialStack;
    private FixedStack stack2;
    private FixedStack stack3;
    private FixedStack bufferStack1;
    private FixedStack bufferStack2;
    private FixedStack bufferStack3;
    // Disks, poles and base
    private final int diskHeight = 20;
    private final int xCoordPoleX = 95;
    private final int xCoordPoleY = 295;
    private final int xCoordPoleZ = 495;
    // Mouse listener, stack clickable areas
    private MouseListener mouseListener;
    private Rectangle xStackArea;
    private Rectangle yStackArea;
    private Rectangle zStackArea;
    // Mouse clicked stack container
    private Map<String, FixedStack> stackMap;
    private final String sourceStack = "SRC";
    private final String destinationStack = "DST";
    // Status text and duration
    private String status = "";
    private boolean started = false;
    private long timeStarted = 0;

    public Stage(FixedStack initialStack) {
        this.initialStack = initialStack;

        elements = initialStack.size();

        stack2 = new FixedStack(elements);
        stack3 = new FixedStack(elements);
        bufferStack1 = new FixedStack(elements);
        bufferStack2 = new FixedStack(elements);
        bufferStack3 = new FixedStack(elements);

        elements += 1;

        stackMap = new HashMap<>();
        stackMap.put(sourceStack, null);
        stackMap.put(destinationStack, null);

        setMouseListener();
    }

    private void setMouseListener() {
        mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                if (xStackArea.contains(e.getX(), e.getY())) {
                    log.info("CLICKED @ Stack X: x({}) y({})", e.getX(), e.getY());
                    stackMap.put(sourceStack, initialStack);
                }

                if (yStackArea.contains(e.getX(), e.getY())) {
                    log.info("CLICKED @ Stack Y: x({}) y({})", e.getX(), e.getY());
                    stackMap.put(sourceStack, stack2);
                }

                if (zStackArea.contains(e.getX(), e.getY())) {
                    log.info("CLICKED @ Stack Z: x({}) y({})", e.getX(), e.getY());
                    stackMap.put(sourceStack, stack3);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (xStackArea.contains(e.getX(), e.getY())) {
                    log.info("RELEASED @ Stack X: x({}) y({})", e.getX(), e.getY());
                    stackMap.put(destinationStack, initialStack);
                    moveDisk();
                    repaint();
                }

                if (yStackArea.contains(e.getX(), e.getY())) {
                    log.info("RELEASED @ Stack Y: x({}) y({})", e.getX(), e.getY());
                    stackMap.put(destinationStack, stack2);
                    moveDisk();
                    repaint();
                }

                if (zStackArea.contains(e.getX(), e.getY())) {
                    log.info("RELEASED @ Stack Z: x({}) y({})", e.getX(), e.getY());
                    stackMap.put(destinationStack, stack3);
                    moveDisk();
                    repaint();
                }
            }
        };

        addMouseListener(mouseListener);
    }

    private void moveDisk() {
        String validMoveMsg = "Valid move...";
        String invalidMoveMsg = "A bigger disk cannot be placed on top of a smaller one!";
        if (stackMap.get(sourceStack) != null
                && stackMap.get(destinationStack) != null) {
            try {
                if (stackMap.get(destinationStack).isEmpty()) {
                    stackMap.get(destinationStack).push(stackMap.get(sourceStack).top());
                    stackMap.get(sourceStack).pop();
                    status = validMoveMsg;
                } else if (stackMap.get(sourceStack).top() > stackMap.get(destinationStack).top()) {
                    log.warn("{}", invalidMoveMsg);
                    status = invalidMoveMsg;
                } else {
                    stackMap.get(destinationStack).push(stackMap.get(sourceStack).top());
                    stackMap.get(sourceStack).pop();
                    status = validMoveMsg;
                }
            } catch (Exception z) {
                String emptyStackMsg = "That stack is empty.";
                log.warn("{}", emptyStackMsg);
                status = emptyStackMsg;
            }

            log.info("Stack 1: {}", initialStack.size());
            log.info("Stack 2: {}", stack2.size());
            log.info("Stack 3: {}", stack3.size());
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
        g.fillRect(xCoordPoleX, yCoordPole(), 2, elements * diskHeight);
        g.fillRect(xCoordPoleY, yCoordPole(), 2, elements * diskHeight);
        g.fillRect(xCoordPoleZ, yCoordPole(), 2, elements * diskHeight);
        g.setColor(Color.GRAY);
        g.setFont(new Font("SansSerif", Font.ITALIC, 18));
        g.drawString(status, 25, 360);
    }

    private void drawStacks() {
        int xCoordDisk;
        int yCoordDisk;
        int diskWidth;

        int widestDiskWidth = (elements - 1) * 20;
        final int diskArcWidth = 10;
        final int diskArcHeight = 10;

        int[] diskColor = new int[elements];

        if ((initialStack.size() < elements) && !started) { // TODO: Check this!
            started = true;
            timeStarted = System.currentTimeMillis();
        }

        xStackArea = stackArea(xCoordPoleX, widestDiskWidth);
        yStackArea = stackArea(xCoordPoleY, widestDiskWidth);
        zStackArea = stackArea(xCoordPoleZ, widestDiskWidth);

        if (!initialStack.isEmpty()) {
            int stackSize = initialStack.size();
            for (int i = 0; i < stackSize; i++) {
                bufferStack1.push(initialStack.pop());
            }

            stackSize = bufferStack1.size();
            for (int j = stackSize ; j > 0; j--) {
                initialStack.push(bufferStack1.top());
                diskColor[j] = bufferStack1.pop();
                xCoordDisk = xCoordPoleX - (diskColor[j] * 10);
                yCoordDisk = 240 - (initialStack.size() * 20);
                diskWidth = diskColor[j] * diskHeight;
                g.setColor(Color.BLACK);
                g.drawRoundRect(xCoordDisk, yCoordDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
                g.setColor(diskColor(j));
                g.fillRoundRect(xCoordDisk, yCoordDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
            }
        }

        if (!stack2.isEmpty()) {
            int stackSize = stack2.size();
            for (int i = 0; i < stackSize; i++) {
                bufferStack2.push(stack2.pop());
            }

            stackSize = bufferStack2.size();
            for (int j = stackSize ; j > 0; j--) {
                stack2.push(bufferStack2.top());
                diskColor[j] = bufferStack2.pop();
                xCoordDisk = xCoordPoleY - (diskColor[j] * 10);
                yCoordDisk = 240 - (stack2.size() * 20);
                diskWidth = diskColor[j] * diskHeight;
                g.setColor(Color.BLACK);
                g.drawRoundRect( xCoordDisk, yCoordDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight );
                g.setColor(diskColor(j));
                g.fillRoundRect( xCoordDisk, yCoordDisk, diskWidth, diskHeight, diskArcWidth,diskArcHeight );
            }
        }

        if (!stack3.isEmpty()) {
            int stackSize = stack3.size();
            for (int i = 0; i < stackSize; i++) {
                bufferStack3.push(stack3.pop());
            }

            stackSize = bufferStack3.size();
            for(int j = stackSize ; j > 0; j--) {
                stack3.push(bufferStack3.top());
                diskColor[j] = bufferStack3.pop();
                xCoordDisk = xCoordPoleZ - (diskColor[j] * 10);
                yCoordDisk = 240 - (stack3.size() * 20);
                diskWidth = diskColor[j] * diskHeight;
                g.setColor(Color.BLACK);
                g.drawRoundRect(xCoordDisk, yCoordDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
                g.setColor(diskColor(j));
                g.fillRoundRect(xCoordDisk, yCoordDisk, diskWidth, diskHeight, diskArcWidth, diskArcHeight);
            }
        }

        if ((initialStack.isEmpty())
                && (stack2.isEmpty())) {
            gameOverText();
            removeMouseListener(mouseListener);
        }
    }

    private void gameOverText() {
        status = "";
        long timeElapsed = (System.currentTimeMillis() - timeStarted) / 1000;

        g.setFont(new Font("SansSerif", Font.BOLD+Font.ITALIC, 20));
        g.setColor(Color.YELLOW);
        g.drawString("Game Complete!", 25, 30);
        g.setFont(new Font("SansSerif", Font.ITALIC, 16));
        if (timeElapsed <= 60) {
            g.drawString("Elapsed time: " + timeElapsed + " seconds", 25, 400);
            g.drawString("Awesome!", 25, 420);
        } else {
            g.drawString("Elapsed time: " + timeElapsed + " seconds", 25, 400);
            g.drawString("Good job.", 25, 420);
        }
    }

    private Color diskColor(int weight) {
        int modifier = (weight + 1) * 10;
        return new Color(0,255 - modifier,0);
    }

    private int yCoordPole() {
        return switch (elements - 1) {
            case 8 -> (elements - 6) * diskHeight;
            case 7 -> (elements - 4) * diskHeight;
            case 6 -> (elements - 2) * diskHeight;
            case 5 -> elements * diskHeight;
            default -> (elements + 2) * diskHeight; // 4
        };
    }

    private Rectangle stackArea(int xCoord, int widestDiskWidth) {
        xCoord = xCoord - (widestDiskWidth / 2);
        Rectangle stackArea = new Rectangle(xCoord, yCoordPole(), widestDiskWidth, elements * diskHeight);
        g.setColor(Color.BLACK);
        g.drawRect(stackArea.x, stackArea.y, stackArea.width, stackArea.height);
        return stackArea;
    }

}
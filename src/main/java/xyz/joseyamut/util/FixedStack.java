package xyz.joseyamut.util;

public class FixedStack {

    private int top = 0;
    private int[] elements;

    public FixedStack(int size) {
        elements = new int[size];
    }

    public int size() {
        return top;
    }

    public boolean isEmpty() {
        return (top == 0);
    }

    public int top() {
        return elements[top-1];
    }

    public void push(int n) {
        elements[top] = n;
        top++;
    }

    public int pop() {
        top--;
        return elements[top];
    }

}

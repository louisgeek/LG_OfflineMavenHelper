package com.louisgeek.javaweb;

public class Derived extends Base {
    @Override
    public void methodOne() {
        super.methodOne();
        System.out.print("C");
    }

    @Override
    public void methodTwo() {
        super.methodTwo();
        System.out.print("D");
    }

}

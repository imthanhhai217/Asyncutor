package com.juhalion.asyncutor;

public class DemoAsyncutor2 extends Asyncutor<Integer, Void, Integer> {
    @Override
    protected Integer doInBackground(Integer integer) {
        int input = integer;
        int res = 0;
        for (int i = 0; i < input; i++) {
            res += i;
        }
        return res;
    }
}

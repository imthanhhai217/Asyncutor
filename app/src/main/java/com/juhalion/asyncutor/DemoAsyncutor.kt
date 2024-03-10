package com.juhalion.asyncutor

open class DemoAsyncutor : Asyncutor<Int, Void, Int>() {
    override fun doInBackground(params: Int?): Int {
        val input = params
        var res = 0
        input?.let {
            for (i in 0..input) {
                res += i
            }
        }
        return res
    }
}
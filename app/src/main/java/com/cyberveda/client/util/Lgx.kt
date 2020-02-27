package com.cyberveda.client.util

import android.util.Log

object Lgx {
    /*
     * debug variable enables/disables all log messages to logcat
     * Useful to disable prior to app store submission
     */
    const val debug = true

    /*
     * l method used to log passed string and returns the
     * calling file as the tag, method and line number prior
     * to the string's message
     */
    fun d(s: String) {
        if (debug) {
            val msg =
                trace(Thread.currentThread().stackTrace, 3)
            Log.d(msg!![0], msg[1] + s)
        } else {
            return
        }
    }

    /*
     * l (tag, string)
     * used to pass logging messages as normal but can be disabled
     * when debug == false
     */
    fun d(t: String, s: String) {
        if (debug) {
            val msg =
                trace(Thread.currentThread().stackTrace, 3)
            Log.d(t + " " + msg!![0], "shen-yue-is-beautiful" + " " + msg[1] + " " + s)
        } else {
            return
        }
    }

    /*
     * trace
     * Gathers the calling file, method, and line from the stack
     * returns a string array with element 0 as file name and
     * element 1 as method[line]
     */
    fun trace(
        e: Array<StackTraceElement>?,
        level: Int
    ): Array<String>? {
        if (e != null && e.size >= level) {
            val s = e[level]
            if (s != null) {
                return arrayOf(
                    e[level].fileName,
                    e[level].methodName + "[" + e[level].lineNumber + "]"
                )
            }
        }
        return null
    }
}
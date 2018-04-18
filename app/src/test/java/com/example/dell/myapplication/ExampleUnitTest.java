package com.example.dell.myapplication;

import com.tencent.ilivesdk.view.AVRootView;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

   @Test
    public void test(){
        try {
            Class   av=AVRootView.class;


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
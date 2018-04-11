package com.example.dell.myapplication;

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
            Class c=MyTest.class;
            MyTest myTest= (MyTest) c.newInstance();
           Method method=c.getDeclaredMethod("str",String.class);
           method.setAccessible(true);
           method.invoke(myTest,"strr---");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
package org.jboss.perf;

import java.util.Random;

/**
 * Created by johara on 09/05/17.
 */
public class SimpleBean {
   private Object obj = "myCustomObject";
   private static Object STATIC_OBJECT = "myCustomStaticObject";

   public Object getObj() {
      return obj;
   }

   public void setObj(final Object obj) {
      this.obj = obj;
   }

   public static Object getStaticObj() {
      return STATIC_OBJECT;
   }

   public static void setStaticObj(final Object obj) {
      STATIC_OBJECT = obj;
   }

   public Integer getRandomInt(Integer seed) {
      return new Random().nextInt();
   }

   public void parse(Object random) {
      random = null;
   }

   public static int myMethod(int a, int b)
   {
      return a<b? a: b;
   }

   public Object returnBean(){
      return new ReturnBean();
   } 


   public class ReturnBean {
      private String name = "RetBean";

      public ReturnBean() {
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return "ReturnBean{" +
             "name='" + name + '\'' +
             '}';
      }
   }
}
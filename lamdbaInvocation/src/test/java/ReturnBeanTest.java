import org.jboss.perf.SimpleBean;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

/**
 * Created by johara on 03/11/17.
 */
public class ReturnBeanTest {

    public static void main(String[] args) {

        SimpleBean simpleBean;

        Method reflectedMyMethod;
        Method reflectedGetReturnBeanMethod;
        MethodHandles.Lookup lookup;
        IntBinaryOperator binaryOperator;
        Supplier supplier;

        simpleBean = new SimpleBean();



        try {
            lookup = MethodHandles.lookup();
            reflectedMyMethod = SimpleBean.class.getDeclaredMethod( "myMethod", int.class, int.class );

            MethodHandle myMethodHandle = lookup.unreflect( reflectedMyMethod );
//            MethodHandle getReturnBeanMethodHandle = lookup.unreflect( reflectedGetReturnBeanMethod );


            //Set accesibility of private method
/*
            final Field internal = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            internal.setAccessible(true);
            final MethodHandles.Lookup trustedGetReturnBeanMethod = (MethodHandles.Lookup) internal.get(reflectedGetReturnBeanMethod);
            final MethodHandles.Lookup caller = trustedGetReturnBeanMethod.in(SimpleBean.class);
            final Method parseMethod = SimpleBean.class.getDeclaredMethod("getReturnBean");

            MethodHandle myGetReturnBeanHandle = caller.unreflect( parseMethod );
*/

            CallSite myMethodCallSite = LambdaMetafactory.metafactory(
                lookup
                , "applyAsInt"
                , MethodType.methodType( IntBinaryOperator.class )
                , myMethodHandle.type()
                , myMethodHandle
                , myMethodHandle.type()
            );

            MethodHandle myMethodFactory = myMethodCallSite.getTarget();

            binaryOperator = (IntBinaryOperator) myMethodFactory.invokeExact();


            int result = binaryOperator.applyAsInt(1 ,2  );


            System.out.println(result);

            MethodType returnBeanMethodType = MethodType.methodType(Object.class);
//            reflectedGetReturnBeanMethod = SimpleBean.class.getDeclaredMethod( "returnBean" );
//            MethodHandle getReturnBeanMethodHandle = lookup.unreflect ( reflectedGetReturnBeanMethod );
//            MethodHandle getReturnBeanMethodHandle = lookup.findStatic(SimpleBean.class, "returnBean", returnBeanMethodType);
            MethodHandle getReturnBeanMethodHandle = lookup.findVirtual(SimpleBean.class, "returnBean", returnBeanMethodType);

            MethodHandle suplierTarget = LambdaMetafactory.metafactory(
                lookup
                , "get"
                , MethodType.methodType( Supplier.class )
                , returnBeanMethodType.generic()
                , getReturnBeanMethodHandle
                , returnBeanMethodType
            ).getTarget();
            Supplier<Object> supplier1 = (Supplier<Object>) suplierTarget.invokeExact(  );
//            MethodHandle getReturnBeanFactory = getReturnBeanCallSite.getTarget();
//            supplier = (Supplier) getReturnBeanFactory.invokeExact();
            Object resultBean = supplier1.get();

            System.out.println(resultBean.toString());

            MethodType methodType = MethodType.methodType(Integer.class, String.class);
//            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handle = lookup.findStatic(Integer.class, "valueOf", methodType);
            Function<String,Integer> function = (Function<String,Integer>) LambdaMetafactory.metafactory(
                lookup
                , "apply"
                , MethodType.methodType(Function.class)
                , methodType.generic()
                , handle
                , methodType
            ).getTarget().invokeExact();

            System.out.println(function.apply("123"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (LambdaConversionException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }



    }

}

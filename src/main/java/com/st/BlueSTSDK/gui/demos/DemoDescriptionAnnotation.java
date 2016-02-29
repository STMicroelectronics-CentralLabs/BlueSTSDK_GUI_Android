package com.st.BlueSTSDK.gui.demos;

import android.support.annotation.DrawableRes;

import com.st.BlueSTSDK.Feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * annotation that give a name and a icon to a demo fragment
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DemoDescriptionAnnotation {
    /**
     * Demo name
     * @return demo name
     */
    String name();

    /***
     * icon that can be used for identify the demo if not specify it is a question mark
     * @return the demo icon or a question mark
     */
    @DrawableRes int iconRes() default android.R.drawable.ic_menu_help;

    /**
     * the node must have all this feature for run correctly the demo
     * @return list of feature needed for run the demo
     */
    Class<? extends Feature>[] requareAll() default {};

    /**
     * the node must have almost one of this feature for run correctly the demo
     * @return list of feature, one on these must be present in the node for run the demos
     */
    Class<? extends Feature>[] requareOneOf() default {};

}

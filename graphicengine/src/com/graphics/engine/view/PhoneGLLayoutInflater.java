package com.graphics.engine.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @hide
 */
class PhoneGLLayoutInflater extends GLLayoutInflater {

	private static final String[] CLASS_PREFIX_LIST = {
			//        "android.widget.",
//        "android.webkit."
    	//XXX:如果改了包名这里也要对象修改，最后一个GL是给LinearLayout等布局的前缀，不是包名
    	"com.graphics.engine.view.GL",
    	"com.graphics.engine.widget.GL",
    	"com.graphics.engine.widget.ext.GL",
    	"com.graphics.engine.view.GL",
    	"com.graphics.engine.widget.GL",
    	"com.graphics.engine.widget.ext.GL",
    };
    
    /**
     * Instead of instantiating directly, you should retrieve an instance
     * through {@link Context#getSystemService}
     * 
     * @param context The Context in which in which to find resources and other
     *                application-specific things.
     * 
     * @see Context#getSystemService
     */
    PhoneGLLayoutInflater(Context context) {
        super(context);
    }
    
    protected PhoneGLLayoutInflater(GLLayoutInflater original, Context newContext) {
        super(original, newContext);
    }
    
    /** Override onCreateView to instantiate names that correspond to the
        widgets known to the Widget factory. If we don't find a match,
        call through to our super class.
    */
    @Override
    protected GLView onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        for (String prefix : CLASS_PREFIX_LIST) {
            try {
                GLView view = createView(name, prefix, attrs);
                if (view != null) {
                    return view;
                }
            } catch (ClassNotFoundException e) {
                // In this case we want to let the base class take a crack
                // at it.
            }
        }

        return super.onCreateView(name, attrs);
    }
    
    public GLLayoutInflater cloneInContext(Context newContext) {
        return new PhoneGLLayoutInflater(this, newContext);
    }
}

package scripts;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptManifest {
	
	String name();

	double version() default 1.0;

	String description() default "";

	String[] authors();


}

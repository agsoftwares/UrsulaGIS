package tasks.old.biclustering_code;

import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class PythonUtils {
	public static void doSomePython(){
	PythonInterpreter interpreter = new PythonInterpreter();
	
	
	int number1 = 10;
	int number2 = 32;
	 
	interpreter.set("number1", new PyInteger(number1));
	interpreter.set("number2", new PyInteger(number2));
	interpreter.exec("number3 = number1+number2");
	PyObject number3 = interpreter.get("number3");
	System.out.println("val : "+number3.toString());
	}
	public static void main(String[] args){
		PythonUtils.doSomePython();
	}
}

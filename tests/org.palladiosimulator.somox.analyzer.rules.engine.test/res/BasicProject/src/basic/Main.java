/**
 *
 */
package basic;

import java.io.Serializable;

/**
 * @author dr6817
 */
public final class Main implements Serializable, Runnable {

	/**
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = -2464882524776278403L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (final String arg : args) {
			System.out.println(arg);
		}
	}

	protected final String[] args;

	/**
	 * The constructor.
	 */
	Main() {
		args = new String[] { "abc", "123" };
	}

	@Override
	public void run() {
		Main.main(args);
	}

	void conflictingMethod(int intArg) {
	}

	void conflictingMethod(short intArg) {
	}

	void conflictingMethod(int[] intArray) {
	}

	void conflictingMethod(short[] intArray) {
	}

	void conflictingMethod(int... intVararg) {
	}

	void conflictingMethod(short... intVararg) {
	}
}

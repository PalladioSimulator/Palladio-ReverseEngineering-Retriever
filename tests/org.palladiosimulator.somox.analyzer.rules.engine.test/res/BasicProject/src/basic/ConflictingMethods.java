package basic;

@Converter
public class ConflictingMethods {
	void conflictingMethod(int intArg);

	void conflictingMethod(short shortArg);

	void conflictingMethod(int[] intArray);

	void conflictingMethod(long... longVararg);
}

	/*
	 * an example of an enumeration, which is a series of constants in a list. this restricts the potential values of a variable
	 * declared with that type to only those values within the set, thereby promoting both code safety and readability
	 */
	public enum Direction { DOWN(0), LEFT(1), UP(2), RIGHT(3), STOP(4);
		private int value = 0;
		private Direction(int value) {
			this.value = value; 
		} 
	};

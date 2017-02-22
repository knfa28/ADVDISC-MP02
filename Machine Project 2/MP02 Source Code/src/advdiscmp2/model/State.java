package advdiscmp2.model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/*
 * Wrapper class for String[] so that arrays with the same elements 
 * will be considered equal.
 */
public class State {
	private String[] stateStrings;
	
	public State(String[] stateStrings) {
		this.stateStrings = stateStrings;
	}
	
	public State getToState(String value,
			int stateLength) {
		State followingKey = leftShiftElements();
		String[] followingKeyStrings = followingKey.getStateStrings();
		followingKeyStrings[followingKeyStrings.length - 1] = value;
		return followingKey;
	}
	
	public State leftShiftElements() {
		String[] ret = new String[stateStrings.length];
		for (int i = 0; i < stateStrings.length - 1; i++)  {
			ret[i] = stateStrings[i + 1];
		}
		return new State(ret);
	}
	
	public String get(int i) {
		return stateStrings[i];
	}
	
	public String[] getStateStrings() {
		return stateStrings;
	}
	
	public int size() {
		return stateStrings.length;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		
		State k = (State) o;
		
		if (k.size() != size())
			return false;
		
		for (int i = 0; i < size(); i++) {
			if (!stateStrings[i].equals(k.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		HashFunction hashingFunction = Hashing.md5();
		Hasher hasher = hashingFunction.newHasher();
		for (String s : stateStrings) {
			hasher.putString(s, Charsets.UTF_8);
		}
		HashCode hc = hasher.hash();
		return hc.asInt();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (String s : stateStrings) {
			sb.append(s + ";");
		}
	    return sb.toString().substring(0, sb.length() - 1) + ")";
	}
	
}



package nz.ac.squash.util;

public class Tuple<A, B> {
	private A a;
	private B b;

	public Tuple(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getA() {
		return a;
	}

	public B getB() {
		return b;
	}

	public void setA(A a) {
		this.a = a;
	}

	public void setB(B b) {
		this.b = b;
	}

	@Override
	public int hashCode() {
		return a.hashCode() ^ b.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Tuple<?, ?>) {
			Tuple<?, ?> other = (Tuple<?, ?>) o;
			return Utility.eqOrNull(a, other.getA())
					&& Utility.eqOrNull(b, other.getB());
		}
		return false;
	}

	@Override
	public String toString() {
		return "(" + a + ", " + b + ")";
	}
}
